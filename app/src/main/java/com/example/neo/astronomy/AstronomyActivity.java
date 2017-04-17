package com.example.neo.astronomy;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;

import java.util.Timer;
import java.util.TimerTask;


public class AstronomyActivity extends FragmentActivity {
    private static final int NUM_PAGES = 2;

    private ViewPager astronomyPager;
    private PagerAdapter astronomyPagerAdapter;

    private boolean isLand = false;
    private AstronomyCalculator astronomyCalculator;

    private LocationFragment locationFragment;
    private SunFragment sunFragment;
    private MoonFragment moonFragment;

    private AstroCalculator astroCalculator;

    private Timer clockTimer;
    private Timer updateFragmentTimer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronomy);

        initAstronomyCalculator();
        initFragments();

        initOrientation();

        startTimers();

        if(isLand) {

        } else {
            /*
            astronomyPager = (ViewPager) findViewById(R.id.fragmentPager);
            astronomyPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

            astronomyPager.setAdapter(astronomyPagerAdapter);
            */
        }
    }

    private void startTimers() {
        startClockTimer();
        startUpdateFragmentTimer();
    }

    private void startUpdateFragmentTimer() {
        final int UPDATE_FRAGMENT_REFRESH_MS = 5000;

        updateFragmentTimer = new Timer();
        updateFragmentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Wejscie w updateFragmentTimer");
                        astroCalculator.setLocation(new AstroCalculator.Location(30, 30));
                        refreshSunFragment();
                        refreshMoonFragment();
                    }
                });
            }
        }, 0, UPDATE_FRAGMENT_REFRESH_MS);

    }

    private void refreshMoonFragment() {
        if(checkFragment(moonFragment)) {
            moonFragment.refresh(astroCalculator.getMoonInfo());
        }
    }

    private void refreshSunFragment() {
        if(checkFragment(sunFragment)) {
            sunFragment.refresh(astroCalculator.getSunInfo());
        }
    }

    private void startClockTimer() {
        final int CLOCK_REFRESH_MS = 1000;

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
        }, 0, CLOCK_REFRESH_MS);
    }

    private void refreshClockTime() {
        if(checkFragment(locationFragment)) {
            locationFragment.refreshTime();
        }
    }

    private boolean checkFragment(Fragment fragment) {
        return fragment != null && fragment.isInLayout();
    }

    private void initOrientation() {
        isLand = getResources().getBoolean(R.bool.isLand);
    }

    private void initAstronomyCalculator() {
        astronomyCalculator = new AstronomyCalculator();
        astroCalculator = new AstroCalculator(new AstroDateTime(2017, 4, 17, 9, 19, 50, 2, false), new AstroCalculator.Location(51.3687535, 19.3564248));
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

    private static int index = 1;

    private class AstronomyCalculator extends AsyncTask<String, Void, String> {

        private String sunrise;

        @Override
        protected String doInBackground(String... params) {
            System.out.println("Wywolany doInBackground");
            sunrise = "Wschod slonca: " + index++;
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println("Wywolany onPostExecute");
            TextView txt = (TextView) findViewById(R.id.sunSunriseTime);
            if(txt != null) {
                txt.setText(sunrise);
            }
        }

        @Override
        protected void onPreExecute() {
            System.out.println("Wywolany onPreExecute");}

        @Override
        protected void onProgressUpdate(Void... values) {
            System.out.println("Wywolany onProgressUpdate");}
    }
/*
    private void startFragmentTimer() {
        TimerTask timer = new TimerTask(){

            @Override
            public void run() {
                astronomyCalculator.execute("");
            }
        };

        timer.sch
    }
    */

    /*
        @Override
        public void onBackPressed() {
            if(!isLand) {
                if (astronomyPager.getCurrentItem() == 0) {
                    super.onBackPressed();
                } else {
                    astronomyPager.setCurrentItem(astronomyPager.getCurrentItem() - 1);
                }
            }
        }
    */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
