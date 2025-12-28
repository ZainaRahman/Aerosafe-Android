package com.example.aerotutorial.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirPollutionResponse {
    @SerializedName("coord")
    private Coord coord;

    @SerializedName("list")
    private List<AirData> list;

    public Coord getCoord() {
        return coord;
    }

    public List<AirData> getList() {
        return list;
    }

    public static class Coord {
        @SerializedName("lon")
        private double lon;

        @SerializedName("lat")
        private double lat;

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }

    public static class AirData {
        @SerializedName("dt")
        private long dt;

        @SerializedName("main")
        private Main main;

        @SerializedName("components")
        private Components components;

        public long getDt() {
            return dt;
        }

        public Main getMain() {
            return main;
        }

        public Components getComponents() {
            return components;
        }
    }

    public static class Main {
        @SerializedName("aqi")
        private int aqi;

        public int getAqi() {
            return aqi;
        }
    }

    public static class Components {
        @SerializedName("co")
        private double co;

        @SerializedName("no")
        private double no;

        @SerializedName("no2")
        private double no2;

        @SerializedName("o3")
        private double o3;

        @SerializedName("so2")
        private double so2;

        @SerializedName("pm2_5")
        private double pm25;

        @SerializedName("pm10")
        private double pm10;

        @SerializedName("nh3")
        private double nh3;

        public double getCo() {
            return co;
        }

        public double getNo() {
            return no;
        }

        public double getNo2() {
            return no2;
        }

        public double getO3() {
            return o3;
        }

        public double getSo2() {
            return so2;
        }

        public double getPm25() {
            return pm25;
        }

        public double getPm10() {
            return pm10;
        }

        public double getNh3() {
            return nh3;
        }
    }
}

