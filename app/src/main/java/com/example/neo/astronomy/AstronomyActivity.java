package com.example.neo.astronomy;

import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;
import com.example.neo.astronomy.fragments.AdditionalFragment;
import com.example.neo.astronomy.fragments.LocationFragment;
import com.example.neo.astronomy.fragments.MoonFragment;
import com.example.neo.astronomy.fragments.SunFragment;
import com.example.neo.astronomy.model.WeatherInfo;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AstronomyActivity extends FragmentActivity {
    private ViewPager astronomyPager;
    private PagerAdapter astronomyPagerAdapter;

    private boolean isLand = false;

    private LocationFragment locationFragment;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;
    private AdditionalFragment additionalFragment;

    private final String DEFAULT_LOCATION = "Lodz";
    private final double DEFAULT_LAT = 51.7592485;
    private final double DEFAULT_LNG = 19.4559833;
    private String currentLocation = DEFAULT_LOCATION;
    private AstroCalculator astroCalculator;
    private WeatherInfo weatherInfo;

    private int UPDATE_FRAGMENT_MINUTES = 15;
    private Timer clockTimer;
    private Timer updateFragmentTimer;
    private Timer initRefreshTimer;
    private boolean wasChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronomy);

        initAstronomyCalculator();
        initWeatherInfo();

        initOrientation();

        if(isLand) {
            initFragments();
        } else {
            initPageViewer();
        }
    }

    private void initWeatherInfo() {
        weatherInfo = new WeatherInfo(currentLocation, astroCalculator.getLocation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        wasChange = true;

        startInitRefreshTimer();

        startTimers();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString("location", currentLocation);
        outState.putDouble("lat", astroCalculator.getLocation().getLatitude());
        outState.putDouble("lng", astroCalculator.getLocation().getLongitude());
        outState.putString("lastResponse", weatherInfo.getLastResponse().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        String location = savedInstanceState.getString("location", DEFAULT_LOCATION);

        double lat = savedInstanceState.getDouble("lat", DEFAULT_LAT);
        double lng = savedInstanceState.getDouble("lng", DEFAULT_LNG);
        setNewLocation(new AstroCalculator.Location(lat, lng), location);

        try {
            JSONObject lastResponse = new JSONObject(savedInstanceState.getString("lastResponse"));
            weatherInfo.parseWeatherInfo(lastResponse);
        } catch(Exception exc) {
            showToast(exc.getMessage());
        }
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
                            boolean timeResult = refreshClockTime();
                            boolean sunResult = refreshSunFragment();
                            boolean moonResult = refreshMoonFragment();
                            boolean locationResult = refreshLocationFragment();
                            boolean additionalFragment = isLand || refreshAdditionalFragment();
                            if(timeResult && sunResult && moonResult && locationResult && additionalFragment) {
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
            case R.id.actionFavorite:
                System.out.println(weatherInfo.printWeather());
                break;
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
            showToast("Successful changed refresh time");
        } catch(Exception exc) {
            showToast(exc.getMessage());
        }
    }

    private void locationChangeProcess(final String newLocation) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + prepareNameLocation(newLocation) +
                "&key=" + getResources().getString(R.string.googleKey);

        Response.Listener<JSONObject> responseAction = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.get("status").equals("OK")) {
                        AstroCalculator.Location newAstroLocation = parseLatLngFromJSON(response);
                        setNewLocation(newAstroLocation, newLocation);
                        showToast("Successful changed location");
                    } else {
                        showToast("Response status is not OK");
                    }
                } catch(Exception exc) {
                    showToast(exc.getMessage());
                }
            }
        };

        Response.ErrorListener errorAction = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToast("Return error. Check internet access.");
            }
        };

        MySingleton.sendRequest(url, responseAction, errorAction, getApplicationContext());
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
            locationFragment.refreshWeather(weatherInfo.getBasicInfo());
            return true;
        }
        return false;
    }

    private boolean refreshAdditionalFragment() {
        if(checkFragment(additionalFragment)) {
            additionalFragment.refreshData(weatherInfo.getAdditionalInfo());
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

    private boolean refreshClockTime() {
        if(checkFragment(locationFragment)) {
            locationFragment.refreshTime();
            return true;
        }
        return false;
    }

    private boolean checkFragment(Fragment fragment) {
        return fragment != null;// && fragment.isInLayout();
    }

    private void initOrientation() {
        isLand = getResources().getBoolean(R.bool.isLand);
    }

    private void initAstronomyCalculator() {
        AstroCalculator.Location DEFAULT_LOCATION_LODZ = new AstroCalculator.Location(DEFAULT_LAT, DEFAULT_LNG);
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
        //additionalFragment = (AdditionalFragment) getSupportFragmentManager().findFragmentById(R.);

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

    public void setNewLocation(AstroCalculator.Location newAstroLocation, String locationName) {
        astroCalculator.setLocation(newAstroLocation);
        currentLocation = locationName;

        weatherInfo.changeLocation(newAstroLocation, locationName);
        weatherInfo.checkWeather(getResources().getString(R.string.aerisClientId),
                getResources().getString(R.string.aerisClientSecretKey), getApplicationContext());

        refreshLocationFragment();

        wasChange = true;
    }

    private static int NUM_ITEMS = 4;

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            wasChange = true;

            switch (position) {
                case 0:
                    System.out.println("Tworze locationFragment");
                    locationFragment = new LocationFragment();
                    return locationFragment;
                case 1:
                    System.out.println("Tworze additionalFragment");
                    additionalFragment = new AdditionalFragment();
                    return additionalFragment;
                case 2:
                    System.out.println("Tworze sunFragment");
                    sunFragment = new SunFragment();
                    //sunFragment.refresh(astroCalculator.getSunInfo());
                    return sunFragment;
                case 3:
                    System.out.println("Tworze moonFragment");
                    moonFragment = new MoonFragment();
                    //moonFragment.refresh(astroCalculator.getMoonInfo());
                    return moonFragment;
                default:
                    wasChange = false;
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}
