package com.example.neo.astronomy;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class AstronomyActivity extends FragmentActivity {
    private ViewPager astronomyPager;
    private PagerAdapter astronomyPagerAdapter;

    private boolean isLand = false;

    private LocationFragment locationFragment;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;

    private String currentLocation = "Lodz";
    private AstroCalculator astroCalculator;

    private int UPDATE_FRAGMENT_MINUTES = 15;
    private Timer clockTimer;
    private Timer updateFragmentTimer;
    private Timer initRefreshTimer;
    private boolean wasChange = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronomy);

        initAstronomyCalculator();

        initOrientation();

        if(isLand) {
            initFragments();
        } else {
            initPageViewer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startInitRefreshTimer();

        startTimers();
    }

    private void startInitRefreshTimer() {
        final int FAST_REFRESH = 100;

        stopTimer(initRefreshTimer);

        initRefreshTimer = new Timer();
        initRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(wasChange) {
                            if(refreshSunFragment() && refreshMoonFragment() && refreshLocationFragment()) {
                                wasChange = false;
                            }
                        }
                    }
                });
            }
        }, 0, FAST_REFRESH);
    }

    private void initPageViewer() {
        astronomyPager = (ViewPager) findViewById(R.id.fragmentPager);
        astronomyPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        astronomyPager.setAdapter(astronomyPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
//        getActionBar().show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeLocationAction:
                showToast("Change location selected");
                changeLocation();
                break;
            case R.id.changeRefreshFragmentTimeAction:
                showToast("Change refresh time selected");
                changeRefreshFragmentTime();
                break;
            case R.id.refreshAction:
                showToast("Refresh selected");
                refreshSunFragment();
                refreshMoonFragment();
                break;
            default:
                break;
        }

        return true;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void changeLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New location [Current: " + currentLocation + "]");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newLocation = input.getText().toString();
                locationChangeProcess(newLocation);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void changeRefreshFragmentTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New refresh time(minutes) [Current: " + UPDATE_FRAGMENT_MINUTES + "]");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);    //InputType.TYPE_CLASS_TEXT |
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTime = input.getText().toString();
                timeChangeProcess(newTime);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void timeChangeProcess(String newTime) {
        try {
            int newTimeInt = Integer.parseInt(newTime);
            UPDATE_FRAGMENT_MINUTES = newTimeInt;
        } catch(Exception exc) {
            showToast(exc.getMessage());
        }
    }

    private void locationChangeProcess(final String newLocation) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + prepareNameLocation(newLocation) +
                "&key=" + getResources().getString(R.string.googleKey);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.get("status").equals("OK")) {
                                AstroCalculator.Location newAstroLocation = parseLatLngFromJSON(response);
                                astroCalculator.setLocation(newAstroLocation);
                                currentLocation = newLocation;

                                locationFragment.refreshLocation(currentLocation);

                                wasChange = true;
                                showToast("Successful changed location");
                            } else {
                                showToast("Response status is not OK");
                            }
                        } catch(Exception exc) {
                            showToast(exc.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast("Return error. Check internet access.");
                    }
                });

        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    private String prepareNameLocation(String newLocation) {
        return newLocation.replace(" ", "+");
    }

    private AstroCalculator.Location parseLatLngFromJSON(JSONObject response) throws Exception {
        JSONObject locationJSON = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
        double lat = locationJSON.getDouble("lat");
        double lng = locationJSON.getDouble("lng");
        return new AstroCalculator.Location(lat, lng);
    }

    private void startTimers() {
        startClockTimer();
        startUpdateFragmentTimer();
    }

    private void startUpdateFragmentTimer() {
        final int MINUTE = 1000 * 60;

        stopTimer(updateFragmentTimer);

        updateFragmentTimer = new Timer();
        updateFragmentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshSunFragment();
                        refreshMoonFragment();
                        refreshLocationFragment();
                    }
                });
            }
            @Override
            public boolean cancel() {
                System.out.println("Wywolano cancel na timerze: " + MINUTE);
                return super.cancel();
            }
        }, 0, MINUTE * UPDATE_FRAGMENT_MINUTES);

    }

    private boolean refreshMoonFragment() {
        if(checkFragment(moonFragment)) {
            moonFragment.refresh(astroCalculator.getMoonInfo());
            return true;
        }
        return false;
    }

    private boolean refreshSunFragment() {
        if(checkFragment(sunFragment)) {
            sunFragment.refresh(astroCalculator.getSunInfo());
            return true;
        }
        return false;
    }

    private boolean refreshLocationFragment() {
        if(checkFragment(locationFragment)) {
            locationFragment.refreshLocation(currentLocation);
            return true;
        }
        return false;
    }

    private void startClockTimer() {
        final int CLOCK_REFRESH_MS = 1000;

        stopTimer(clockTimer);

        clockTimer = new Timer();
        clockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshClockTime();
                    }
                });
            }

            @Override
            public boolean cancel() {
                System.out.println("Wywolano cancel na timerze: " + CLOCK_REFRESH_MS);
                return super.cancel();
            }
        }, 0, CLOCK_REFRESH_MS);
    }

    private void refreshClockTime() {
        if(checkFragment(locationFragment)) {
            locationFragment.refreshTime();
        }
    }

    private boolean checkFragment(Fragment fragment) {
        return fragment != null;// && fragment.isInLayout();
    }

    private void initOrientation() {
        isLand = getResources().getBoolean(R.bool.isLand);
    }

    private void initAstronomyCalculator() {
        AstroCalculator.Location DEFAULT_LOCATION_LODZ = new AstroCalculator.Location(51.7592485, 19.4559833);
        Calendar now = Calendar.getInstance();
        int GMT_OFFSET = 2;
        astroCalculator = new AstroCalculator(new AstroDateTime(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE), now.get(Calendar.SECOND), GMT_OFFSET, false), DEFAULT_LOCATION_LODZ);
    }

    private void initFragments() {
        locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);
        sunFragment = (SunFragment) getSupportFragmentManager().findFragmentById(R.id.sunFragment);
        moonFragment = (MoonFragment) getSupportFragmentManager().findFragmentById(R.id.moonFragment);

        refreshClockTime();
        refreshSunFragment();
        refreshMoonFragment();
    }

    @Override
    public void onPause() {
        stopTimer(clockTimer);
        stopTimer(updateFragmentTimer);
        super.onPause();
    }

    private void stopTimer(Timer timer) {
        if(timer != null) {
            timer.cancel();
        }
    }

    private static int NUM_ITEMS = 3;
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    System.out.println("Tworze locationFragment");
                    locationFragment = new LocationFragment();
                    wasChange = true;
                    return locationFragment;
                case 1:
                    System.out.println("Tworze sunFragment");
                    sunFragment = new SunFragment();
                    wasChange = true;
                    //sunFragment.refresh(astroCalculator.getSunInfo());
                    return sunFragment;
                case 2:
                    System.out.println("Tworze moonFragment");
                    moonFragment = new MoonFragment();
                    wasChange = true;
                    //moonFragment.refresh(astroCalculator.getMoonInfo());
                    return moonFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
