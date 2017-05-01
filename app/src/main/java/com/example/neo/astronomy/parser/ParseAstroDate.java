package com.example.neo.astronomy.parser;

import com.astrocalculator.AstroCalculator;
import com.astrocalculator.AstroDateTime;
import com.example.neo.astronomy.model.UnitSystem;

public class ParseAstroDate {

    public static String toStringData(AstroDateTime date) {
        return String.format("%02d.%02d.%04d", date.getDay(), date.getMonth(), date.getYear());
    }

    public static String toStringTime(AstroDateTime date) {
        return String.format("%02d:%02d:%02d", date.getHour(), date.getMinute(), date.getSecond());
    }

    public static String toStringAzimuth(double azimuth) {
        return String.format("%.2f", azimuth);
    }

    public static String toStringPercent(double percent) {
        return String.format("%d", (int) (100 * percent));
    }

    public static String toStringLunar(double age) {
        return String.format("%.3f", age);
    }

    public static AstroCalculator.Location latlngFromDatabase(String latlng) {
        String[] splitted = latlng.split(",");
        double lat = Double.parseDouble(splitted[0]);
        double lng = Double.parseDouble(splitted[1]);
        return new AstroCalculator.Location(lat, lng);
    }

    public static String latlngToDatabase(AstroCalculator.Location latlng) {
        return latlng.getLatitude() + "," + latlng.getLongitude();
    }

    public static String prepareName(String newLocation) {
        return newLocation.replace(" ", "%20");
    }
}
