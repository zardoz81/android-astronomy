package com.example.neo.astronomy.database;

import android.provider.BaseColumns;

public final class WeatherContract {

    private WeatherContract() {}

    public static class WeatherInfoTable implements BaseColumns {
        public static final String TABLE_NAME = "weather_info";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_RESPONSE = "response";
        public static final String COLUMN_NAME_LOCATION_ID = "location_id";

        public static final String SQL_CREATE_WEATHER_INFO =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_TIMESTAMP + " TEXT," +
                        COLUMN_NAME_RESPONSE + " TEXT," +
                        COLUMN_NAME_LOCATION_ID + " INTEGER," +
                        " FOREIGN KEY (" + COLUMN_NAME_LOCATION_ID + ") REFERENCES " +
                        LocationsTable.TABLE_NAME + "(" + LocationsTable._ID + "));";

        public static final String SQL_DELETE_WEATHER_INFO =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class LocationsTable implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_WOEID = "woeid";
        public static final String COLUMN_NAME_FAVOURITE = "favourite";
        public static final String COLUMN_NAME_LATLNG = "latlng";

        public static final String SQL_CREATE_LOCATIONS =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_LOCATION + " TEXT," +
                        COLUMN_NAME_WOEID + " INTEGER," +
                        COLUMN_NAME_FAVOURITE + " INTEGER," +
                        COLUMN_NAME_LATLNG + " TEXT)";

        public static final String SQL_DELETE_LOCATIONS =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
