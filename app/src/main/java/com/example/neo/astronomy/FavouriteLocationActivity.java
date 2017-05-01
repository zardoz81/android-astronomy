package com.example.neo.astronomy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.database.WeatherDbHelper;
import com.example.neo.astronomy.model.UnitSystem;
import com.example.neo.astronomy.model.YahooLocation;
import com.example.neo.astronomy.parser.ParseAstroDate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavouriteLocationActivity extends AppCompatActivity {
    private WeatherDbHelper weatherDbHelper;
    private LayoutInflater layoutInflater;

    private List<String> favouriteLocationsNames;
    private ArrayAdapter<String> adapter;

    private List<YahooLocation> favouriteLocations;
    private YahooLocation location;

    private UnitSystem unitSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_location);

        initLayoutInflater();

        initDatabase();

        initFavouriteLocations();

        initFavouriteList();

        initUnitSystem();
    }

    private void initLayoutInflater() {
        layoutInflater = LayoutInflater.from(this);
    }

    private void initUnitSystem() {
        unitSystem = UnitSystem.METRIC;
        changeUnitSystem();
    }

    private void changeUnitSystem() {
        setText(R.id.unitSystem, unitSystem.name());
    }

    private void setText(int id, String text) {
        TextView textView = (TextView) findViewById(id);
        if(textView != null) {
            textView.setText(text);
        }
    }

    private void initFavouriteList() {
        favouriteLocationsNames = new ArrayList<>();
        for(YahooLocation y: favouriteLocations) {
            favouriteLocationsNames.add(y.getName());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favouriteLocationsNames);
        ListView favouritesList = (ListView) findViewById(R.id.favouriteList);
        favouritesList.setAdapter(adapter);
        favouritesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                location = favouriteLocations.get(position);
                startAstronomyActivity();
            }
        });
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void initDatabase() {
        weatherDbHelper = new WeatherDbHelper(getBaseContext());
        //weatherDbHelper.insert("Belchatow", 485813, 1, ParseAstroDate.latlngToDatabase(new AstroCalculator.Location(51.366699, 19.383301)));
    }

    private void initFavouriteLocations() {
        favouriteLocations = new ArrayList<>();
        favouriteLocations = weatherDbHelper.selectYahooLocation(5, 1);
    }

    public void onClickChangeUnitSystem(View view) {
        final View inflator = layoutInflater.inflate(R.layout.change_unit_dialog, null);

        final RadioGroup radioGroup = (RadioGroup) inflator.findViewById(R.id.unitRadioGroup);
        radioGroup.check(unitSystem == UnitSystem.METRIC ? R.id.radioMetic : R.id.radioImperial);

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = (RadioButton) inflator.findViewById(radioButtonID);
                unitSystem = parseUnitSystem(radioButton.getText().toString());
                changeUnitSystem();
            }
        };

        showDialog("Change unit system", inflator, onClickListener);
    }

    public static UnitSystem parseUnitSystem(String text) {
        if(text.toUpperCase().equals(UnitSystem.METRIC.name())) {
            return UnitSystem.METRIC;
        } else {
            return UnitSystem.IMPERIAL;
        }
    }

    int FAVOURITE = 0;

    public void onClickCheckNewLocation(View view) {
        final View inflator = layoutInflater.inflate(R.layout.new_location_dialog, null);

        final EditText newLocationText = (EditText) inflator.findViewById(R.id.newLocationText);
        final CheckBox addToFavourites = (CheckBox) inflator.findViewById(R.id.newLocationCheckBox);

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newLocation =  newLocationText.getText().toString();
                FAVOURITE = 0;
                if(addToFavourites.isChecked()) {
                    FAVOURITE = 1;
                }

                changeLocation(newLocation, FAVOURITE);
            }
        };

        showDialog("Check new location", inflator, onClickListener);
    }

    private boolean changeLocation(String newLocation, int favourite) {
        if(newLocation != null) {
            ArrayList<YahooLocation> result;
            if(isOnline()) {
                //pobiera z yahoo
                checkLocation(newLocation);
            } else {
                //sprawdza offline po nazwie w bazie
                result = weatherDbHelper.selectYahooLocation(1, newLocation);
                if(result.size() == 0) {
                    showToast("No internet access. Can't get WOEID.");
                    return false;
                } else {
                    location = result.get(0);
                    showToast("No internet access. Result was loaded from database.");
                    startAstronomyActivity();
                }
            }
            return true;
        }
        return false;
    }

    private void startAstronomyActivity() {
        Intent astronomy = new Intent(this, AstronomyActivity.class);
        astronomy.putExtra("yahooLocationCSV", location.toCsv());
        astronomy.putExtra("unitSystem", unitSystem.name());

        startActivity(astronomy);
    }

    private void checkLocation(final String newLocation) {
        String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places(1)" +
        "%20where%20text%3D%22" + ParseAstroDate.prepareName(newLocation) + "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        Response.Listener<JSONObject> responseAction = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject query = response.getJSONObject("query");
                    if(query.getInt("count") > 0) {
                        JSONObject place = query.getJSONObject("results").getJSONObject("place");
                        final int DEFAULT_NO_INFO = -1;
                        String name = place.getString("name");
                        String woeid = place.getString("woeid");
                        double lat = Double.parseDouble(place.getJSONObject("centroid").getString("latitude"));
                        double lng = Double.parseDouble(place.getJSONObject("centroid").getString("longitude"));

                        location = new YahooLocation(name, Integer.parseInt(woeid), new AstroCalculator.Location(lat ,lng));

                        //sprawdza po woeid czy jest w bazie
                        ArrayList<YahooLocation> result = weatherDbHelper.selectYahooLocation(location.getWoeid());
                        if(result.size() == 0 ) {    //nie ma
                            long rowId = weatherDbHelper.insert(location, FAVOURITE);
                            location.setId(rowId);
                            if(FAVOURITE == 1) {
                                favouriteLocations.add(location);
                                favouriteLocationsNames.add(location.getName());
                                adapter.notifyDataSetChanged();
                            }
                            showToast("Successful checked location WOEID from yahoo");
                        } else { //jest wiec tylko pobiera rowId
                            location.setId(result.get(0).getId());
                            showToast("Successful loaded WOEID from database");
                        }

                        startAstronomyActivity();
                    } else {
                        showToast("Response status is not OK");
                    }
                } catch(Exception exc) {
                    showToast(exc.getMessage());
                }
            }
        };

        Response.ErrorListener errorAction = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToast("Error. Check internet access.");
            }
        };

        MySingleton.sendRequest(url, responseAction, errorAction, getApplicationContext());
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private boolean isExists(int woeid) {
        for(YahooLocation l: favouriteLocations) {
            if(l.getWoeid() == woeid) {
                return true;
            }
        }
        return false;
    }

    private void showDialog(String title, View inflator, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setView(inflator);

        builder.setPositiveButton("OK", onClickListener);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
