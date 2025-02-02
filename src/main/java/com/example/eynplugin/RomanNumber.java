package com.example.eynplugin;

public class RomanNumber {
    private static final String[] ROMAN_NUMERALS = {
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };
    private static final int[] VALUES = {
        1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    public static String toRoman(int number) {
        if (number < 1 || number > 3999) return String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (number >= VALUES[i]) {
                number -= VALUES[i];
                sb.append(ROMAN_NUMERALS[i]);
            }
        }
        return sb.toString();
    }
}