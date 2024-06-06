package com.hypenet.realestaterehman.utils;

public class NumberFormatter {
    public static String formatNumber(long number) {
        if (number >= 1_000_000) {
            return format(number, 1_000_000, "M");
        } else if (number >= 1_000) {
            return format(number, 1_000, "K");
        } else {
            return String.valueOf(number);
        }
    }

    private static String format(long number, long divisor, String suffix) {
        double value = (double) number / divisor;
        return (value == (long) value) ? String.format("%d%s", (long) value, suffix)
                : String.format("%.1f%s", value, suffix);
    }
}
