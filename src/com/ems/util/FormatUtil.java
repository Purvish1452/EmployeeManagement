package com.ems.util;

public final class FormatUtil {
    private FormatUtil() {
    }

    public static String currency(double amount) {
        return "$" + String.format("%.2f", amount);
    }

    public static String line(int length, String character) {
        return character.repeat(length);
    }
}
