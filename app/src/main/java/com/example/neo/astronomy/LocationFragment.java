package com.example.neo.astronomy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.DateTimeKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class LocationFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    public void refreshTime() {
        setText(R.id.currentTime, new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
    }

    private void setText(int id, String value) {
        TextView label = (TextView) getView().findViewById(id);
        if(label != null) {
            label.setText(value);
        }
    }
}
