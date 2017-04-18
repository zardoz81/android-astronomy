package com.example.neo.astronomy.parser;

import com.astrocalculator.AstroCalculator;

public class ParseWeatherInfo {
    static final String PERCENT = "%";

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
}
