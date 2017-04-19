package com.example.neo.astronomy.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.neo.astronomy.model.WeatherInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WeatherDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Weather.db";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeatherContract.WeatherInfoTable.SQL_CREATE_WEATHER_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeatherContract.WeatherInfoTable.SQL_DELETE_WEATHER_INFO);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void insert(WeatherInfo weatherInfo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP, weatherInfo.getLastTimestamp());
        JSONObject response = weatherInfo.getLastResponse();
        if(response != null) {
            value.put(WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE, response.toString());
        } else {
            return;
        }

        //null - nie wstawi jesli value nie bedzie wypelnione
        long newRowId = db.insert(WeatherContract.WeatherInfoTable.TABLE_NAME, null, value);
    }

    private String[] projection = {
        WeatherContract.WeatherInfoTable._ID,
        WeatherContract.WeatherInfoTable.COLUMN_NAME_TIMESTAMP,
        WeatherContract.WeatherInfoTable.COLUMN_NAME_RESPONSE
    };

    public ArrayList<WeatherInfo> select(int howMany) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();

        //String orderBy = WeatherContract.WeatherInfoTable._ID + " DESC";

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
}
