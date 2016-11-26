package com.dranitix.railtime;

/**
 * Created by Kenta Iwasaki on 11/27/2016.
 */
public class Storage {
    public static float MONEY = 0f;
    public static float MULTIPLIER = 0f;
    public static String[] STATION_NAMES = {
        "Whampoa",
            "Ho Man Tin",
            "Yau Ma Tei",
            "Mong Kok",
            "Prince Edward",
            "Shep Kip Mei",
            "Kowloon Tong",
            "Lok Fu",
            "Wong Tai Sin",
            "Diamond Hill",
            "Choi Hung",
            "Kowloon Bay"
    };
    private static int STATION_INDEX = 0;

    public static void incrementStation() {
        STATION_INDEX++;
        if (STATION_INDEX >= STATION_NAMES.length) STATION_INDEX = 0;
    }

    public static String getStation() {
        return STATION_NAMES[STATION_INDEX];
    }

    public static void addCash() {
        MONEY += 0.1f * (1f + MULTIPLIER);
    }
}
