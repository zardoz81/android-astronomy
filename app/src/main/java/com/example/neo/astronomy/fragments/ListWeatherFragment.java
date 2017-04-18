package com.example.neo.astronomy.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.neo.astronomy.LongtermWeatherAdapter;
import com.example.neo.astronomy.R;
import com.example.neo.astronomy.model.WeatherInfo;

import java.util.ArrayList;


public class ListWeatherFragment extends ListFragment {
    private ArrayList<WeatherInfo.LongtermInfo> longtermData;
    private static LongtermWeatherAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_weather, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        longtermData = new ArrayList<>();

        //longtermData.add(new WeatherInfo.LongtermInfo("Tue", "25 Apr 2017", "15", "29", "Sunny"));
        //longtermData.add(new WeatherInfo.LongtermInfo("Wen", "26 Apr 2017", "18", "27", "Sunny"));

        //adapter = new LongtermWeatherAdapter(longtermData, getContext());
        //setListAdapter(adapter);
        refresh(longtermData);
    }

    public void refresh(ArrayList<WeatherInfo.LongtermInfo> longtermData) {
        this.longtermData = longtermData;

        adapter = new LongtermWeatherAdapter(longtermData, getContext());
        setListAdapter(adapter);
    }
}
