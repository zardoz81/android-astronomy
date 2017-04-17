package com.example.neo.astronomy;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ScreenSlidePageFragment extends Fragment {
    /*
    private AstronomyFragmentActivityListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        if (activity instanceof AstronomyFragmentActivityListener) {
            listener = (AstronomyFragmentActivityListener) activity;
        } else {
            throw new ClassCastException( activity.toString() + " musi implementować interfejs: " +
                    ScreenSlidePageFragment.AstronomyFragmentActivityListener.class);
        }
    }
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_astronomy, container, false);

        return rootView;
    }

    public void setText(String txt) {
        TextView view = (TextView) getView().findViewById(R.id.simpleText);
        view.setText(txt);
    }
/*
    public interface AstronomyFragmentActivityListener {
        public void onItemSelected(String msg);
    }
    */
}
