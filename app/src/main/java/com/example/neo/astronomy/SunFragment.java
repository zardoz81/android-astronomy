package com.example.neo.astronomy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astrocalculator.AstroCalculator;


public class SunFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sun, container, false);
    }

    public void refresh(AstroCalculator.SunInfo sunInfo) {
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
