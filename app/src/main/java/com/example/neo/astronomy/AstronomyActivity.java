package com.example.neo.astronomy;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;
import com.example.neo.astronomy.database.WeatherDbHelper;
import com.example.neo.astronomy.fragments.AdditionalFragment;
import com.example.neo.astronomy.fragments.ListWeatherFragment;
import com.example.neo.astronomy.fragments.LocationFragment;
import com.example.neo.astronomy.fragments.MoonFragment;
import com.example.neo.astronomy.fragments.SunFragment;
import com.example.neo.astronomy.model.UnitSystem;
import com.example.neo.astronomy.model.WeatherInfo;
import com.example.neo.astronomy.model.YahooLocation;
import com.example.neo.astronomy.parser.ParseAstroDate;
import com.example.neo.astronomy.parser.ParseWeatherInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class AstronomyActivity extends AppCompatActivity {
    private ViewPager astronomyPager;
    private PagerAdapter astronomyPagerAdapter;

    private boolean isLand = false;

    private UnitSystem unitSystem;
    private YahooLocation yahooLocation;

    private LocationFragment locationFragment;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;
    private AdditionalFragment additionalFragment;
    private ListWeatherFragment listWeatherFragment;

    //private final String DEFAULT_LOCATION = "Lodz";
    //private final double DEFAULT_LAT = 51.7592485;
    //private final double DEFAULT_LNG = 19.4559833;
    //private String currentLocation = DEFAULT_LOCATION;
    private AstroCalculator astroCalculator;
    private WeatherInfo weatherInfo;

    private int UPDATE_FRAGMENT_MINUTES = 15;
    private Timer clockTimer;
    private Timer updateFragmentTimer;
    private Timer initRefreshTimer;
    private boolean wasChange;

    private WeatherDbHelper weatherDbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronomy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadExtras();

        initDatabase();

        initAstronomyCalculator();
        initWeatherInfo();

        initOrientation();
        if(isLand) {
            initFragments();
        } else {
            initPageViewer();
        }
    }



    private void loadExtras() {
        YahooLocation loc = YahooLocation.fromCsv(getIntent().getStringExtra("yahooLocationCSV"));
        if(loc != null) {
            yahooLocation = loc;
        }

        UnitSystem sys = FavouriteLocationActivity.parseUnitSystem(getIntent().getStringExtra("unitSystem"));
        if(sys != null) {
            unitSystem = sys;
            ParseWeatherInfo.setUnitSystem(unitSystem);
        }
    }

    private void initDatabase() {
        weatherDbHelper = new WeatherDbHelper(getBaseContext());
    }

    private void initWeatherInfo() {
        ArrayList<WeatherInfo> result = weatherDbHelper.selectWeatherInfo(1, yahooLocation.getId());
        WeatherInfo fromDatabase = result.size() > 0 ? result.get(0) : null;
        if(fromDatabase == null || (fromDatabase != null && WeatherInfo.timeToRefresh(fromDatabase.getLastTimestamp()))) {
            if(fromDatabase == null) {
                weatherInfo = new WeatherInfo(yahooLocation.getName(), yahooLocation.getLatlng());
            } else {
                weatherInfo = fromDatabase;
            }

            if(isOnline()) {
                checkWeatherByYahoo();
                showToast("Checking from yahoo");
            } else {
                weatherInfo.refresh();
                if(fromDatabase == null) {
                    showToast("No internet access.");
                } else {
                    showToast("No internet access. Old values are shown.");
                }
            }
        } else {
            weatherInfo = fromDatabase;
            weatherInfo.refresh();
            showToast("Forecast taken from database");
        }
    }

    private void checkWeatherByYahoo() {
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                weatherInfo.parseWeatherInfoByYahoo(response);
                refreshLocationFragment();
                refreshAdditionalFragment();
                weatherDbHelper.insertOrUpdate(weatherInfo, yahooLocation.getId());
            }
        };

        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20%3D%20"
                + yahooLocation.getWoeid() + "&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        weatherInfo.sendResponse(getBaseContext(), url, responseListener);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();

        wasChange = true;

        //startInitRefreshTimer();

        startTimers();
    }

    @Override
    protected void onDestroy() {
        weatherDbHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        stopTimer(clockTimer);
        stopTimer(updateFragmentTimer);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString("yahooLocationCSV", yahooLocation.toCsv());
        outState.putString("unitSystem", unitSystem.name());

        if(weatherInfo.getLastResponse() != null) {
            outState.putString("lastResponse", weatherInfo.getLastResponse().toString());
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        yahooLocation = YahooLocation.fromCsv(savedInstanceState.getString("yahooLocationCSV"));
        unitSystem = FavouriteLocationActivity.parseUnitSystem(savedInstanceState.getString("unitSystem"));

        //setNewLocation(new AstroCalculator.Location(lat, lng), location);

        try {
            JSONObject lastResponse = new JSONObject(savedInstanceState.getString("lastResponse"));
            if(lastResponse != null) {
                weatherInfo.parseWeatherInfoByAeris(lastResponse);
            }
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
                                showToast("Zmiana wasChange");
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
        getMenuInflater().inflate(R.menu.astronomy_menu_bar, menu);
//        getActionBar().show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ViewPager v = (ViewPager) findViewById(R.id.fragmentPager);
        switch (item.getItemId()) {
            case R.id.changeLocationAction:
                showToast("Change location selected");
                changeLocation();
                break;
            case R.id.changeRefreshFragmentTimeAction:
                showToast("Change refresh time selected");
                changeRefreshFragmentTime();
                break;
            case R.id.changeUnitSystem:
                changeUnitSystem();
                //refreshFragment(v.getCurrentItem());
                refreshAll();
                break;
            case R.id.refreshAction:
                wasChange = true;
                //refreshFragment(v.getCurrentItem());
                refreshAll();
                showToast("Refreshed");
                break;
            default:
                break;
        }

        return true;
    }

    private void refreshAll() {
        refreshSunFragment();
        refreshMoonFragment();
        refreshLocationFragment();
        refreshAdditionalFragment();
        refreshListWeatherFragment();
    }

    private void refreshFragment(int currentItem) {
        switch(currentItem) {
            case 0:
                refreshLocationFragment();
                break;
            case 1:
                refreshAdditionalFragment();
                break;
            case 2:
                refreshListWeatherFragment();
                break;
            case 3:
                refreshSunFragment();
                break;
            case 4:
                refreshMoonFragment();
                break;

        }
    }

    private void changeUnitSystem() {
        unitSystem = unitSystem == UnitSystem.METRIC ? UnitSystem.IMPERIAL : UnitSystem.METRIC;
        ParseWeatherInfo.setUnitSystem(unitSystem);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void changeLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New location [Current: " + yahooLocation.getName() + "]");

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
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
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
            stopTimer(updateFragmentTimer);
            startUpdateFragmentTimer();
            showToast("Successful changed refresh time");
        } catch(Exception exc) {
            showToast(exc.getMessage());
        }
    }

    private void locationChangeProcess(final String newLocation) {
        if(isOnline()) {
            String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places(1)" +
                    "%20where%20text%3D%22" + ParseAstroDate.prepareName(newLocation) + "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

            Response.Listener<JSONObject> responseAction = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject query = response.getJSONObject("query");
                        if(query.getInt("count") > 0) {
                            JSONObject place = query.getJSONObject("results").getJSONObject("place");
                            final int DEFAULT_NO_INFO = -1;
                            String name = place.getString("name");
                            String woeid = place.getString("woeid");
                            double lat = Double.parseDouble(place.getJSONObject("centroid").getString("latitude"));
                            double lng = Double.parseDouble(place.getJSONObject("centroid").getString("longitude"));

                            YahooLocation location = new YahooLocation(name, Integer.parseInt(woeid), new AstroCalculator.Location(lat ,lng));

                            //sprawdza po woeid czy jest w bazie
                            ArrayList<YahooLocation> result = weatherDbHelper.selectYahooLocation(location.getWoeid());
                            if(result.size() == 0 ) {
                                long rowId = weatherDbHelper.insert(location, 0);
                                location.setId(rowId);
                            } else { //jest wiec tylko pobiera rowId
                                location = result.get(0);
                            }
                            yahooLocation = location;
                            initWeatherInfo();
                            astroCalculator.setLocation(yahooLocation.getLatlng());
                            weatherDbHelper.insertOrUpdate(weatherInfo, location.getId());
                            refreshAll();
                        } else {
                            showToast("Error. Check location name.");
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
        } else {
            showToast("No internet access.");
        }
    }

/*
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
*/


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
                        refreshAdditionalFragment();
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
            locationFragment.refreshLocation(yahooLocation.getName());
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
        final int CLOCK_REFRESH_MS = 500;

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
        return fragment != null;
    }

    private void initOrientation() {
        isLand = getResources().getBoolean(R.bool.isLand);
    }

    private void initAstronomyCalculator() {
        Calendar now = Calendar.getInstance();
        int GMT_OFFSET = 2;
        astroCalculator = new AstroCalculator(new AstroDateTime(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE), now.get(Calendar.SECOND), GMT_OFFSET, false), yahooLocation.getLatlng());
    }

    private void initFragments() {
        locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);
        sunFragment = (SunFragment) getSupportFragmentManager().findFragmentById(R.id.sunFragment);
        moonFragment = (MoonFragment) getSupportFragmentManager().findFragmentById(R.id.moonFragment);

        refreshClockTime();
        refreshSunFragment();
        refreshMoonFragment();
    }

    private void stopTimer(Timer timer) {
        if(timer != null) {
            timer.cancel();
        }
    }

    public void setNewLocation(AstroCalculator.Location newAstroLocation, String locationName) {
        astroCalculator.setLocation(newAstroLocation);
        //currentLocation = locationName;

        weatherInfo.changeLocation(newAstroLocation, locationName);
        weatherInfo.checkWeather(getApplicationContext());

        insertToDatabase(weatherInfo);

        refreshLocationFragment();

        wasChange = true;
    }

    private void insertToDatabase(WeatherInfo weatherInfo) {
        //weatherDbHelper.insert(weatherInfo);
    }

    private static int NUM_ITEMS = 5;

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            //wasChange = true;
            switch (position) {
                case 0:
                    if(locationFragment == null) {
                        System.out.println("Tworze locationFragment");
                        locationFragment = new LocationFragment();

                    }
                    refreshLocationFragment();
                    return locationFragment;
                case 1:
                    if(additionalFragment == null) {
                        System.out.println("Tworze additionalFragment");
                        additionalFragment = new AdditionalFragment();
                    }
                    refreshAdditionalFragment();
                    return additionalFragment;
                case 2:
                    if(listWeatherFragment == null) {
                        System.out.println("Tworze listWeatherFragment");
                        listWeatherFragment = new ListWeatherFragment();
                    }
                    refreshListWeatherFragment();
                    return listWeatherFragment;
                case 3:
                    if(sunFragment == null) {
                        System.out.println("Tworze sunFragment");
                        sunFragment = new SunFragment();
                    }
                    refreshSunFragment();
                    return sunFragment;
                case 4:
                    if(moonFragment == null) {
                        System.out.println("Tworze moonFragment");
                        moonFragment = new MoonFragment();
                    }
                    refreshMoonFragment();
                    return moonFragment;
                default:
                    wasChange = false;
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            super.registerDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

    private void refreshListWeatherFragment() {
        if(checkFragment(listWeatherFragment)) {
            listWeatherFragment.refresh(weatherInfo.getLongtermData(), getBaseContext());
        }
    }
}
