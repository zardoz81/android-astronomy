package com.example.neo.astronomy.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.neo.astronomy.R;
import com.example.neo.astronomy.model.WeatherInfo;
import com.example.neo.astronomy.parser.ParseWeatherInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class LocationFragment extends Fragment {
    private String locationName;
    private WeatherInfo.BasicInfo basicInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(locationName != null) {
            refreshLocation(locationName);
        }
        if(basicInfo != null) {
            refreshWeather(basicInfo);
        }
    }

    public void refreshTime() {
        setText(R.id.currentTime, new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
    }

    public void refreshLocation(String locationName) {
        this.locationName = locationName;

        setText(R.id.location, locationName);
    }

    public void refreshWeather(WeatherInfo.BasicInfo basicInfo) {
        this.basicInfo = basicInfo;

        setText(R.id.coordinates, ParseWeatherInfo.toCoordinates(basicInfo.getLatLng()));
        setText(R.id.temperature, ParseWeatherInfo.toTemperature(basicInfo.getTemperature()));
        setText(R.id.pressure, ParseWeatherInfo.toPressure(basicInfo.getPressure()));
        setText(R.id.description, basicInfo.getDescription());
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
