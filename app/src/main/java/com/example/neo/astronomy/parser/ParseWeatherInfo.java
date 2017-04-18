package com.example.neo.astronomy.parser;

import com.astrocalculator.AstroCalculator;

import java.util.HashMap;
import java.util.Map;

public class ParseWeatherInfo {
    static final String PERCENT = "%";
    static final String DEGREE  = "\u00b0";

    public static String toCoordinates(AstroCalculator.Location location) {
        return String.format("%.4f,%.4f", location.getLatitude(), location.getLongitude());
    }

    public static String toPressure(double pressure, boolean european) {
        //
        final String unit = european ? "hPa" : "mbar";
        return String.format("%.2f%s", pressure, unit);
    }

    public static String toWindPower(double windPower, boolean european) {
        final String unit = european ? "KPH" : "MPH";
        return String.format("%.2f%s", windPower, unit);
    }

    public static String toHumidity(int humidity) {
        return String.format("%d%s", humidity, PERCENT);
    }

    public static String toVisibility(double visibility, boolean european) {
        final String unit = european ? "KPH" : "MPH";
        return String.format("%.2f%s", visibility, unit);
    }

    public static String toDateRow(String date) {
        return String.format("(%s)", date);
    }

    public static String toTempRow(String lowTemp, String highTemp) {
        return String.format("%s%s-%s%s", lowTemp, DEGREE, highTemp, DEGREE);
    }

    public static String toWindDirection(String shortDir) {
        if(shortDir == null) {
            return "";
        }
        Map<String, String> directionsMap = new HashMap<String, String>();
        directionsMap.put("S", "South");
        directionsMap.put("N", "North");
        directionsMap.put("W", "West");
        directionsMap.put("E", "East");
        StringBuilder result = new StringBuilder();
        for(String key: directionsMap.keySet()) {
            if(shortDir.contains(key)) {
                result.append(directionsMap.get(key) + " ");
            }
        }
        return result.toString();
    }

}
