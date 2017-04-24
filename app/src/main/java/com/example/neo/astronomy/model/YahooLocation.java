package com.example.neo.astronomy.model;

import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.parser.ParseAstroDate;

public class YahooLocation {
    private long id;
    private String name;
    private int woeid;
    private AstroCalculator.Location latlng;

    public YahooLocation(long id, String name, int woeid, AstroCalculator.Location latlng) {
        this.id = id;
        this.name = name;
        this.woeid = woeid;
        this.latlng = latlng;
    }

    public YahooLocation(String name, int woeid, AstroCalculator.Location latlng) {
        this.name = name;
        this.woeid = woeid;
        this.latlng = latlng;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getWoeid() {
        return woeid;
    }

    public AstroCalculator.Location getLatlng() {
        return latlng;
    }

    public String toCsv() {
        return id + ";" + name + ";" + woeid + ";" + ParseAstroDate.latlngToDatabase(latlng);
    }

    public static YahooLocation fromCsv(String csv) {
        String[] p = csv.split(";");
        return new YahooLocation(Long.parseLong(p[0]), p[1], Integer.parseInt(p[2]), ParseAstroDate.latlngFromDatabase(p[3]));
    }
}
