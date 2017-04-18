package com.example.neo.astronomy.model;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astrocalculator.AstroCalculator;
import com.example.neo.astronomy.MySingleton;
import com.example.neo.astronomy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class WeatherInfo {
    private final int REFRESH_TIMESTAMP = 60 * 60;  //1 HOUR // 60 minutes * 60 seconds

    private BasicInfo basicInfo;
    private AdditionalInfo additionalInfo;
    private ArrayList<LongtermInfo> longtermData;

    private long lastTimestamp;
    private JSONObject lastResponse;

    private boolean wasChangeLocation;

    private boolean useYahoo = true;

    public ArrayList<LongtermInfo> getLongtermData() {
        return longtermData;
    }

    public BasicInfo getBasicInfo() {
        //refresh();
        return basicInfo;
    }

    public void refresh() {
        if(lastResponse != null) {
            parseWeatherInfoByAeris(lastResponse);
        }
    }

    public AdditionalInfo getAdditionalInfo() {
        //refresh();
        return additionalInfo;
    }

    public JSONObject getLastResponse() {
        return lastResponse;
    }

    public WeatherInfo(String location) {
        initBasicInfo();
        initAdditionalInfo();
        initLongtermData();

        changeLocation(location);
    }

    public WeatherInfo(String location, AstroCalculator.Location latLng) {
        initBasicInfo();
        initAdditionalInfo();
        initLongtermData();

        changeLocation(latLng, location);
    }

    private void initLongtermData() {
        longtermData = new ArrayList<>();
    }

    public void checkWeather(Context context) {
        if(wasChangeLocation || timeToRefresh()) {
            sendResponse(context);
        }
    }

    private boolean timeToRefresh() {
        return System.currentTimeMillis() - lastTimestamp >= REFRESH_TIMESTAMP;
    }

    private void sendResponse(Context context) {
        String url = getUrl(context);
        Response.Listener<JSONObject> responseAction = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(useYahoo) {
                    parseWeatherInfoByYahoo(response);
                } else {
                    parseWeatherInfoByAeris(response);
                }
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

    private void parseWeatherInfoByYahoo(JSONObject response) {
        try {
            lastTimestamp = System.currentTimeMillis();
            JSONObject info = response.getJSONObject("query").getJSONObject("results").getJSONObject("channel");
            JSONArray forecast = info.getJSONObject("item").getJSONArray("forecast");

            basicInfo.setTemperature( Integer.parseInt(info.getJSONObject("item").getJSONObject("condition").getString("temp")));
            basicInfo.setPressure( Double.parseDouble(info.getJSONObject("atmosphere").getString("pressure")));
            basicInfo.setDescription( forecast.getJSONObject(0).getString("text"));

            additionalInfo.setHumidity( Integer.parseInt(info.getJSONObject("atmosphere").getString("humidity")));
            additionalInfo.setWindPower( Double.parseDouble(info.getJSONObject("wind").getString("speed")));
            additionalInfo.setWindDirection( info.getJSONObject("wind").getString("direction"));
            additionalInfo.setVisibility( Double.parseDouble(info.getJSONObject("atmosphere").getString("visibility")));

            longtermData.clear();
            for(int i = 0; i < forecast.length(); i++) {
                String day = forecast.getJSONObject(i).getString("day");
                String date = forecast.getJSONObject(i).getString("date");
                String low = forecast.getJSONObject(i).getString("low");
                String high = forecast.getJSONObject(i).getString("high");
                String desc = forecast.getJSONObject(i).getString("text");
                longtermData.add(new LongtermInfo(day, date, low, high, desc));
            }

            lastResponse = response;
        } catch(JSONException exc) {
            System.out.println("Nieudane parsowanie. Wyjatek: " + exc.toString());
        }
    }

    public String printWeather() {
        return String.format("Timestamp: %d\nbasicInfo: %s\nadditionalInfo: %s", lastTimestamp, basicInfo.toString(), additionalInfo.toString());
    }

    public void parseWeatherInfoByAeris(JSONObject response) {
        try {
            if(response.getBoolean("success")) {
                JSONObject info = response.getJSONObject("response").getJSONObject("ob");
                lastTimestamp = info.getLong("timestamp");

                basicInfo.setTemperature(info.getInt("tempC"));
                basicInfo.setPressure(info.getDouble("pressureMB"));
                basicInfo.setDescription(info.getString("weather"));

                additionalInfo.setHumidity(info.getInt("humidity"));
                additionalInfo.setWindPower(info.getDouble("windKPH"));
                additionalInfo.setWindDirection(info.getString("windDir"));
                additionalInfo.setVisibility(info.getDouble("visibilityKM"));

                lastResponse = response;
            }
        } catch(JSONException exc) {
            System.out.println("Nieudane parsowanie. Wyjatek: " + exc.toString());
        }
    }

    private String getUrl(Context context) {
        if(useYahoo) {
            return  "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20" +
                    "where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" +
                    prepareName(basicInfo.location) +
                    "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        } else {
            String clientId = context.getResources().getString(R.string.aerisClientId);
            String clientSecretKey = context.getResources().getString(R.string.aerisClientSecretKey);
            double lat = basicInfo.getLatLng().getLatitude();
            double lng = basicInfo.getLatLng().getLongitude();
            return "http://api.aerisapi.com/observations/" + lat + "," + lng + "?client_id=" + clientId + "&client_secret=" + clientSecretKey;
        }
    }

    private String prepareName(String location) {
        String test = location;
        String nfdNormalizedString = Normalizer.normalize(test, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        System.out.println("Prepare name dal z: " + location + " = " + pattern.matcher(nfdNormalizedString).replaceAll(""));

        return location.replace(" ", "%20");
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

    public void changeLocation(String newLocation) {
        basicInfo.setLocation(newLocation);

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

        public LongtermInfo() {

        }

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
