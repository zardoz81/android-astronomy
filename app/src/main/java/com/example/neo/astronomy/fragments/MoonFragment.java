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


public class MoonFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_moon, container, false);
    }

    public void refresh(AstroCalculator.MoonInfo moonInfo) {
        final String PERCENT  = "%";
        setText(R.id.moonMoonriseTime, ParseAstroDate.toStringTime(moonInfo.getMoonrise()));
        setText(R.id.moonMoonsetTime, ParseAstroDate.toStringTime(moonInfo.getMoonset()));

        setText(R.id.moonNewMoonData, ParseAstroDate.toStringData(moonInfo.getNextNewMoon()));
        setText(R.id.moonFullMoonData, ParseAstroDate.toStringData(moonInfo.getNextFullMoon()));

        setText(R.id.moonMoonPhasePercent, ParseAstroDate.toStringPercent(moonInfo.getIllumination()) + PERCENT);

        setText(R.id.moonLunarMonthDay, ParseAstroDate.toStringLunar(moonInfo.getAge()));
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
