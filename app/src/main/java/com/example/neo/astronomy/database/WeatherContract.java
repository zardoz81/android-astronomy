package com.example.neo.astronomy.database;

import android.provider.BaseColumns;

public final class WeatherContract {

    private WeatherContract() {}

    //try do more simple save - this one requires too much work(need other tables for basicInfo etc.)
    /*
    public static class WeatherInfoTable implements BaseColumns {
        public static final String TABLE_NAME = "weather_info";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_LOW_TEMP = "low_temp";
        public static final String COLUMN_NAME_HIGH_TEMP = "high_temp";
        public static final String COLUMN_NAME_DESCRIPTION = "description";

        public static final String SQL_CREATE_WEATHER_INFO =
                "CREATE_TABLE" + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_DAY + " TEXT," +
                        COLUMN_NAME_DATE + " TEXT," +
                        COLUMN_NAME_LOW_TEMP + " TEXT," +
                        COLUMN_NAME_HIGH_TEMP + " TEXT," +
                        COLUMN_NAME_DESCRIPTION + " TEXT)";

        public static final String SQL_DELETE_WEATHER_INFO =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
    */
    public static class WeatherInfoTable implements BaseColumns {
        public static final String TABLE_NAME = "weather_info";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_RESPONSE = "response";

        public static final String SQL_CREATE_WEATHER_INFO =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_TIMESTAMP + " TEXT," +
                        COLUMN_NAME_RESPONSE + " TEXT)";

        public static final String SQL_DELETE_WEATHER_INFO =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
