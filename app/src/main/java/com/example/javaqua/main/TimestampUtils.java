package com.example.javaqua.main;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimestampUtils {

    public static String formatRelativeTimestamp(Date timestamp) {
        if (timestamp == null) {
            return "";
        }
        long timeInMillis = timestamp.getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        if (diff < 0) {
            return "In the future";
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return seconds + "s ago";
        } else if (minutes < 60) {
            return minutes + "m ago";
        } else if (hours < 24) {
            return hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + "w ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + "mo ago";
        } else {
            long years = days / 365;
            return years + "y ago";
        }
    }
}
