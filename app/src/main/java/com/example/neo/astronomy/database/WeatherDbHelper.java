package com.example.neo.astronomy.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.model.WeatherInfo;
import com.example.neo.astronomy.model.YahooLocation;
import com.example.neo.astronomy.parser.ParseAstroDate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WeatherDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeatherContract.LocationsTable.SQL_CREATE_LOCATIONS);
        db.execSQL(WeatherContract.WeatherInfoTable.SQL_CREATE_WEATHER_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeatherContract.WeatherInfoTable.SQL_DELETE_WEATHER_INFO);
        db.execSQL(WeatherContract.LocationsTable.SQL_DELETE_LOCATIONS);
        onCreate(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void insert(WeatherInfo weatherInfo, int locationId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP, weatherInfo.getLastTimestamp());
        value.put(WeatherContract.WeatherInfoTable.COLUMN_NAME_LOCATION_ID, locationId);
        JSONObject response = weatherInfo.getLastResponse();
        if(response != null) {
            value.put(WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE, response.toString());
        } else {
            return;
        }

        //null - nie wstawi jesli value nie bedzie wypelnione
        long newRowId = db.insert(WeatherContract.WeatherInfoTable.TABLE_NAME, null, value);
    }

    public long insert(YahooLocation l, int fav) {
        return insert(l.getName(), l.getWoeid(), fav, ParseAstroDate.latlngToDatabase(l.getLatlng()));
    }

    public long insert(String location, int woeid, int favourite, String latlng) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(WeatherContract.LocationsTable.COLUMN_NAME_LOCATION, location);
        value.put(WeatherContract.LocationsTable.COLUMN_NAME_WOEID, woeid);
        value.put(WeatherContract.LocationsTable.COLUMN_NAME_FAVOURITE, favourite);
        value.put(WeatherContract.LocationsTable.COLUMN_NAME_LATLNG, latlng);

        return db.insert(WeatherContract.LocationsTable.TABLE_NAME, null, value);
    }


    private String[] projection = {
        WeatherContract.WeatherInfoTable._ID,
        WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP,
        WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE
    };

    public ArrayList<WeatherInfo> selectWeatherInfo(int howMany, long locationId) {
        String select = "select * from " + WeatherContract.WeatherInfoTable.TABLE_NAME + " where "
                + WeatherContract.WeatherInfoTable.COLUMN_NAME_LOCATION_ID + " = "
                + locationId + " order by " + WeatherContract.WeatherInfoTable._ID + " desc limit " + howMany;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(select, null);

        ArrayList<WeatherInfo> result =  new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                JSONObject response = null;
                try {
                    WeatherInfo weatherInfo = new WeatherInfo();
                    weatherInfo.setLastTimestamp(Long.parseLong(c.getString(c.getColumnIndex(WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP))));

                    response = new JSONObject(c.getString(c.getColumnIndex(WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE)));
                    weatherInfo.setLastResponse(response);

                    result.add(weatherInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } while(c.moveToNext());
        }

        c.close();
        db.close();

        return result;
    }

    public ArrayList<WeatherInfo> selectWeatherInfo(int howMany) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                WeatherContract.WeatherInfoTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null,
                Integer.toString(howMany)
        );

        ArrayList<WeatherInfo> result = new ArrayList<>();
        while(cursor.moveToNext()) {
            WeatherInfo weatherInfo = new WeatherInfo();
            weatherInfo.setLastTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP))));

            JSONObject response = new JSONObject(cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE)));
            weatherInfo.setLastResponse(response);

            result.add(weatherInfo);
        }

        return result;
    }

    public ArrayList<YahooLocation> selectYahooLocation(int howMany, int favourite) {
        String select = "SELECT * FROM " + WeatherContract.LocationsTable.TABLE_NAME + " WHERE " + WeatherContract.LocationsTable.COLUMN_NAME_FAVOURITE + " == " + favourite;
        return selectYahooLocation(select);
    }

    public ArrayList<YahooLocation> selectYahooLocation(int woeid) {
        String select = "SELECT * FROM " + WeatherContract.LocationsTable.TABLE_NAME + " WHERE " + WeatherContract.LocationsTable.COLUMN_NAME_WOEID + " == " + woeid;
        return selectYahooLocation(select);
    }

    public ArrayList<YahooLocation> selectYahooLocation(int howMany, String name) {
        String select = "SELECT * FROM " + WeatherContract.LocationsTable.TABLE_NAME + " WHERE " + WeatherContract.LocationsTable.COLUMN_NAME_LOCATION + " = '" + name + "'";
        return selectYahooLocation(select);
    }

    private ArrayList<YahooLocation> selectYahooLocation(String select) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(select, null);

        ArrayList<YahooLocation> result =  new ArrayList<>();
        if(c.moveToFirst()) {
            do {
                long id = c.getLong(0);
                String location = c.getString(1);
                int woeid = c.getInt(2);
                AstroCalculator.Location latlng = ParseAstroDate.latlngFromDatabase(c.getString(4));
                System.out.println("ID: " + c.getString(0) + " - " + location + "/ " + woeid );

                result.add(new YahooLocation(id, location, woeid, latlng));
            } while(c.moveToNext());
        }

        c.close();
        db.close();

        return result;
    }
}
