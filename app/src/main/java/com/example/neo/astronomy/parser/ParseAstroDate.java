package com.example.neo.astronomy.parser;

import com.astrocalculator.AstroDateTime;

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
}
