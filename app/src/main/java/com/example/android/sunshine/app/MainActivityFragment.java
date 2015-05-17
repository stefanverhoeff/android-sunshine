package com.example.android.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> weatherList = new ArrayList<String>();
        weatherList.add("Monday - sunny - 7 / 15 ");
        weatherList.add("Tuesday - rainy - 5 / 16 ");
        weatherList.add("Wednesday - cloudy - 8 / 18 ");
        weatherList.add("Thursday - sunny - 14 / 21 ");
        weatherList.add("Friday - stormy - 7 / 12 ");
        weatherList.add("Saturday - greyish - 12 / 15 ");
        weatherList.add("Sunday - hot - 17 / 25 ");

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherList);

        ListView listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(mForecastAdapter);

        return rootView;
    }
}
