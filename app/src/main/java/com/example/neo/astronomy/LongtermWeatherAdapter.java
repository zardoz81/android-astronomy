package com.example.neo.astronomy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.TextView;

import com.example.neo.astronomy.model.WeatherInfo;
import com.example.neo.astronomy.parser.ParseWeatherInfo;

import java.util.ArrayList;


public class LongtermWeatherAdapter extends ArrayAdapter<WeatherInfo.LongtermInfo> {
    private ArrayList<WeatherInfo.LongtermInfo> longtermData;
    private Context mContext;

    private static class ViewHolder {
        TextView day;
        TextView date;
        TextView temp;
        TextView text;
    }

    public LongtermWeatherAdapter(ArrayList<WeatherInfo.LongtermInfo> data, Context context) {
        super(context, R.layout.longterm_weather_row, data);
        this.longtermData = data;
        this.mContext = context;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WeatherInfo.LongtermInfo longtermInfo = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.longterm_weather_row, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.day = (TextView) convertView.findViewById(R.id.dayRow);
            viewHolder.date = (TextView) convertView.findViewById(R.id.dateRow);
            viewHolder.temp = (TextView) convertView.findViewById(R.id.tempRow);
            viewHolder.text = (TextView) convertView.findViewById(R.id.textRow);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

            result = convertView;
        }

        viewHolder.day.setText(longtermInfo.getDay());
        viewHolder.date.setText(ParseWeatherInfo.toDateRow(longtermInfo.getDate()));
        viewHolder.temp.setText(ParseWeatherInfo.toTempRow(longtermInfo.getLowTemp(), longtermInfo.getHighTemp()));
        viewHolder.text.setText(longtermInfo.getDescription());

        return convertView;
    }
}
