package com.scrapium.utils;

public class TimeFormatter {

    public static String timeToString(int seconds) {
        if (seconds < 60) {
            return "in " + seconds + " seconds";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return "in " + minutes + " minute" + (minutes == 1 ? "" : "s");
        } else if (seconds < 86400) {
            int hours = seconds / 3600;
            return "in " + hours + " hour" + (hours == 1 ? "" : "s");
        } else {
            int days = seconds / 86400;
            return "in " + days + " day" + (days == 1 ? "" : "s");
        }
    }
}