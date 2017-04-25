package com.example.neo.astronomy.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.neo.astronomy.R;
import com.example.neo.astronomy.model.WeatherInfo;
import com.example.neo.astronomy.parser.ParseWeatherInfo;


public class AdditionalFragment extends Fragment {
    private WeatherInfo.AdditionalInfo additionalInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_additional, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(additionalInfo != null) {
            refreshData(additionalInfo);
        }
    }

    public void refreshData(WeatherInfo.AdditionalInfo additionalInfo) {
        this.additionalInfo = additionalInfo;

        boolean isEuropean = true;
        setText(R.id.windPower, ParseWeatherInfo.toWindPower(additionalInfo.getWindPower(), isEuropean));
        setText(R.id.windDirection, ParseWeatherInfo.toWindDirection(additionalInfo.getWindDirection()));
        setText(R.id.humidity, ParseWeatherInfo.toHumidity(additionalInfo.getHumidity()));
        setText(R.id.visibility, ParseWeatherInfo.toVisibility(additionalInfo.getVisibility(), isEuropean));
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
