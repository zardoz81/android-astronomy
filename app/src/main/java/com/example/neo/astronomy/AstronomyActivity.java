package com.example.neo.astronomy;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
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

    private Timer updateTimer;

    private AstroCalculator astro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astronomy);

        astronomyCalculator = new AstronomyCalculator();

        isLand = getResources().getBoolean(R.bool.isLand);

        if(isLand) {
            //startFragmentTimer();

            /*
            updateTimer = new Timer();
            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    new AstronomyCalculator().execute("");
                }
            }, 0, 3000);
            */
            astro = new AstroCalculator(new AstroDateTime(2017, 4, 17, 9, 19, 50, 2, false), new AstroCalculator.Location(51.3687535, 19.3564248));
            AstroCalculator.MoonInfo moonInfo= astro.getMoonInfo();
            System.out.println(moonInfo.getMoonrise());

            SunFragment sunFragment = (SunFragment) getSupportFragmentManager().findFragmentById(R.id.sunFragment);
            sunFragment.refresh(astro.getSunInfo());

            MoonFragment moonFragment = (MoonFragment) getSupportFragmentManager().findFragmentById(R.id.moonFragment);
            moonFragment.refresh(astro.getMoonInfo());
        } else {
            /*
            astronomyPager = (ViewPager) findViewById(R.id.fragmentPager);
            astronomyPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

            astronomyPager.setAdapter(astronomyPagerAdapter);
            */
        }
    }

    @Override
    public void onPause() {
        if(updateTimer != null) {
            updateTimer.cancel();
        }
        super.onPause();
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
