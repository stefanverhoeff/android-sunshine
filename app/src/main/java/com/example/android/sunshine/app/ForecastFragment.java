package com.example.android.sunshine.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private final String initialPostCode = "10245,Berlin";

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        updateTitle();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            new FetchWeatherDataTask().execute(initialPostCode);
            updateTitle();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTitle() {
        Calendar calendar = new GregorianCalendar();
        String appTitle = String.format("%s @ %s (%02d:%02d:%02d)", getString(R.string.app_name), initialPostCode, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        getActivity().setTitle(appTitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        new FetchWeatherDataTask().execute(initialPostCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.forecastfragment_main, container, false);

        ArrayList<String> weatherList = new ArrayList<>();

        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weatherList);

        final ListView listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(mForecastAdapter);
        listViewForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = rootView.getContext();

                CharSequence text = listViewForecast.getItemAtPosition(position).toString();
                int duration = Toast.LENGTH_SHORT;

                Toast.makeText(context, text, duration)
                        .show();
            }
        });

        return rootView;
    }

    private class FetchWeatherDataTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherDataTask.class.getSimpleName();

        private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&units=metric&cnt=15";

        protected void onPostExecute(String[] weatherData) {
            // Add weather to UI
            mForecastAdapter.clear();
            mForecastAdapter.addAll(Arrays.asList(weatherData));
            mForecastAdapter.notifyDataSetChanged();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String postCode = params[0];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri weatherUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter("q", postCode)
                        .build();

                URL url = new URL(weatherUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            String[] weatherDataFromJson = new String[0];
            try {
                weatherDataFromJson = new WeatherDataParser().getWeatherDataFromJson(forecastJsonStr, 15);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Failed to parse weather JSON", e);
            }

            return weatherDataFromJson;
        }
    }


}
