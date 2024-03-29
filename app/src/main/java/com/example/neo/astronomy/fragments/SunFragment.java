package com.example.neo.astronomy.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.parser.ParseAstroDate;
import com.example.neo.astronomy.R;


public class SunFragment extends Fragment {
    private AstroCalculator.SunInfo sunInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sun, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(sunInfo != null) {
            refresh(sunInfo);
        }
    }

    public void refresh(AstroCalculator.SunInfo sunInfo) {
        this.sunInfo = sunInfo;

        final String DEGREE  = "\u00b0";
        setText(R.id.sunSunriseTime, ParseAstroDate.toStringTime(sunInfo.getSunrise()));
        setText(R.id.sunSunriseAzimuth, ParseAstroDate.toStringAzimuth(sunInfo.getAzimuthRise()) + DEGREE);

        setText(R.id.sunSunsetTime, ParseAstroDate.toStringTime(sunInfo.getSunset()));
        setText(R.id.sunSunsetAzimuth, ParseAstroDate.toStringAzimuth(sunInfo.getAzimuthSet()) + DEGREE);

        setText(R.id.sunTwilightMorningTime, ParseAstroDate.toStringTime(sunInfo.getTwilightMorning()));
        setText(R.id.sunTwilightEveningTime, ParseAstroDate.toStringTime(sunInfo.getTwilightEvening()));
    }

    private void setText(int id, String value) {
        View v = getView();
        if(v != null) {
            TextView label = (TextView) v.findViewById(id);
            if(label != null) {
                label.setText(value);
            }
        }
    }
}
