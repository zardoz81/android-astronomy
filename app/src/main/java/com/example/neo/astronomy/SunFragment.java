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

    }

    public void setText(String text) {
        TextView t = (TextView) getView().findViewById(R.id.sunSunriseAzimuth);
        if(t != null) {
            t.setText(text);
        }
    }
}
