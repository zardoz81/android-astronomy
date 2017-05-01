package com.example.neo.astronomy.parser;

import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.model.UnitSystem;

import java.util.HashMap;
import java.util.Map;

public class ParseWeatherInfo {
    static final String PERCENT = "%";
    static final String DEGREE  = "\u00b0";
    public static UnitSystem unitSystem;

    public static void setUnitSystem(UnitSystem unitSystem) {
        ParseWeatherInfo.unitSystem = unitSystem;
    }

    public static boolean isMetric() {
        return unitSystem == UnitSystem.METRIC;
    }

    public static String toCoordinates(AstroCalculator.Location location) {
        if(location == null) {
            return "";
        }
        return String.format("%.4f,%.4f", location.getLatitude(), location.getLongitude());
    }

    public static String toPressure(double pressure) {
        //
        final String unit = "hPa";
        return String.format("%.2f%s", pressure, unit);
    }

    public static String toWindPower(double windPower, boolean european) {
        final String unit = isMetric() ? "KPH" : "MPH";
        if(isMetric()) {
            windPower = windPower * 1.609344;
        }
        return String.format("%.2f%s", windPower, unit);
    }

    public static String toHumidity(int humidity) {
        return String.format("%d%s", humidity, PERCENT);
    }

    public static String toVisibility(double visibility, boolean european) {
        final String unit = isMetric() ? "KM" : "MI";
        if(isMetric()) {
            visibility *= 1.609344;
        }
        return String.format("%.2f%s", visibility, unit);
    }

    public static String toDateRow(String date) {
        return String.format("(%s)", date);
    }

    public static String toTempRow(String lowTemp, String highTemp) {
        String TEMP_UNIT = isMetric() ? "C" : "F";
        if(isMetric()) {
            lowTemp = Integer.toString(toCelc(Integer.parseInt(lowTemp)));
            highTemp = Integer.toString(toCelc(Integer.parseInt(highTemp)));
        }
        return String.format("%s%s-%s%s%s", lowTemp, DEGREE, highTemp, DEGREE, TEMP_UNIT);
    }

    private static int toCelc(int i) {
        return (int)((i - 32) * (5.0 / 9.0));
    }

    public static String toWindDirection(String shortDir) {
        int dir;
        try {
            dir = Integer.parseInt(shortDir);
        } catch(Exception e) {
            return "";
        }
        String[] dirs = {"N", "E", "S", "W"};
        String result = "";
        for(int i = 0; i < 8; i++) {
            if(dir < (i+1)*45) {
                int k = i / 2;
                if(i % 2 == 0) {
                    result += dirs[k];
                } else {
                    result = dirs[k-1] + dirs[k];
                }
                break;
            }
        }
        return result;
    }

    public static String toTemperature(int temperature) {
        final String unit = isMetric() ? "C" : "F";
        if(isMetric() && temperature != 0) {
            temperature = toCelc(temperature);
        }
        return String.format("%s%s%s", Integer.toString(temperature), DEGREE, unit);
    }
}
