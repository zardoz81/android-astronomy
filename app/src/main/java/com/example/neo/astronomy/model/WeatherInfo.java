package com.example.neo.astronomy.model;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherInfo {
    private BasicInfo basicInfo;
    private AdditionalInfo additionalInfo;

    private long lastTimestamp;
    private JSONObject lastResponse;

    private boolean wasChangeLocation;

    public BasicInfo getBasicInfo() {
        //refresh();
        return basicInfo;
    }

    private void refresh() {
        if(lastResponse != null) {
            //parseWeatherInfo(lastResponse);
        }
    }

    public AdditionalInfo getAdditionalInfo() {
        //refresh();
        return additionalInfo;
    }

    public JSONObject getLastResponse() {
        return lastResponse;
    }

    public WeatherInfo(String location, AstroCalculator.Location latLng) {
        initBasicInfo();
        initAdditionalInfo();

        changeLocation(latLng, location);
    }

    public void checkWeather(String clientId, String clientSecretKey, Context context) {
        if(!wasChangeLocation) {
            return;
        }
        sendResponse(clientId, clientSecretKey, context);
    }

    private void sendResponse(String clientId, String clientSecretKey, Context context) {
        String url = getUrl(clientId, clientSecretKey);
        Response.Listener<JSONObject> responseAction = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseWeatherInfo(response);
            }
        };
        Response.ErrorListener errorAction = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Rzucony wyjatek: " + error.toString());
            }
        };

        MySingleton.sendRequest(url, responseAction, errorAction, context);
    }

    public String printWeather() {
        return String.format("Timestamp: %d\nbasicInfo: %s\nadditionalInfo: %s", lastTimestamp, basicInfo.toString(), additionalInfo.toString());
    }

    public void parseWeatherInfo(JSONObject response) {
        if(response == null) {
            return;
        }
        try {
            if(response.getBoolean("success")) {
                lastResponse = response;

                JSONObject info = response.getJSONObject("response").getJSONObject("ob");
                lastTimestamp = info.getLong("timestamp");

                basicInfo.setTemperature(info.getInt("tempC"));
                basicInfo.setPressure(info.getDouble("pressureMB"));
                basicInfo.setDescription(info.getString("weather"));

                additionalInfo.setHumidity(info.getInt("humidity"));
                additionalInfo.setWindPower(info.getDouble("windKPH"));
                additionalInfo.setWindDirection(info.getString("windDir"));
                additionalInfo.setVisibility(info.getDouble("visibilityKM"));
            }
        } catch(JSONException exc) {
            System.out.println("Nieudane parsowanie. Wyjatek: " + exc.toString());
        }
    }

    private String getUrl(String clientId, String clientSecretKey) {
        double lat = basicInfo.getLatLng().getLatitude();
        double lng = basicInfo.getLatLng().getLongitude();
        return "http://api.aerisapi.com/observations/" + lat + "," + lng + "?client_id=" + clientId + "&client_secret=" + clientSecretKey;
    }

    private void initAdditionalInfo() {
        additionalInfo = new AdditionalInfo();
    }

    private void initBasicInfo() {
        basicInfo = new BasicInfo();
    }

    public void changeLocation(AstroCalculator.Location newAstroLocation, String newLocation) {
        basicInfo.setLocation(newLocation);
        basicInfo.setLatLng(newAstroLocation);

        wasChangeLocation = true;
    }


    public class BasicInfo {
        private String location;
        private AstroCalculator.Location latLng;
        private int temperature;
        private double pressure;
        private String description;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public AstroCalculator.Location getLatLng() {
            return latLng;
        }

        public void setLatLng(AstroCalculator.Location latLng) {
            this.latLng = latLng;
        }

        public int getTemperature() {
            return temperature;
        }

        public void setTemperature(int temperature) {
            this.temperature = temperature;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("Loc: %s, latLng: %f / %f, t: %d, pr: %f, desc: %s", location, latLng.getLatitude(), latLng.getLongitude(), temperature, pressure, description);
        }
    }

    public class AdditionalInfo {
        private double windPower;
        private String windDirection;
        private int humidity;
        private double visibility;

        public double getWindPower() {
            return windPower;
        }

        public void setWindPower(double windPower) {
            this.windPower = windPower;
        }

        public String getWindDirection() {
            return windDirection;
        }

        public void setWindDirection(String windDirection) {
            this.windDirection = windDirection;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public double getVisibility() {
            return visibility;
        }

        public void setVisibility(double visibility) {
            this.visibility = visibility;
        }

        @Override
        public String toString() {
            return String.format("WP: %f, WD: %s, H: %d, V: %f", windPower, windDirection, humidity, visibility);
        }
    }

    public static class LongtermInfo {
        private String day;
        private String date;
        private String lowTemp;
        private String highTemp;
        private String description;

        public LongtermInfo(String day, String date, String lowTemp, String highTemp, String description) {
            this.day = day;
            this.date = date;
            this.lowTemp = lowTemp;
            this.highTemp = highTemp;
            this.description = description;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getLowTemp() {
            return lowTemp;
        }

        public void setLowTemp(String lowTemp) {
            this.lowTemp = lowTemp;
        }

        public String getHighTemp() {
            return highTemp;
        }

        public void setHighTemp(String highTemp) {
            this.highTemp = highTemp;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
