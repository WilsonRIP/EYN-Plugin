package com.example.eynplugin;

/**
 * Utility class for converting integers to their Roman numeral representations.
 * Supports numbers in the range 1 to 3999.
 */
public final class RomanNumber {

    // Roman numeral symbols corresponding to their integer values.
    private static final String[] ROMAN_NUMERALS = {
        "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    // The integer values corresponding to the Roman numeral symbols.
    private static final int[] VALUES = {
        1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    // Private constructor to prevent instantiation.
    private RomanNumber() {
        throw new UnsupportedOperationException("RomanNumber is a utility class and cannot be instantiated");
    }

    /**
     * Converts a given integer to its Roman numeral representation.
     * <p>
     * Valid for numbers from 1 to 3999; if the number is out of range,
     * its string representation is returned.
     * </p>
     *
     * @param number the integer to convert.
     * @return the Roman numeral representation of the number, or its string value if out of range.
     */
    public static String toRoman(final int number) {
        if (number < 1 || number > 3999) {
            return String.valueOf(number);
        }
        int remaining = number; // Mutable copy for calculation
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (remaining >= VALUES[i]) {
                remaining -= VALUES[i];
                sb.append(ROMAN_NUMERALS[i]);
            }
        }
        return sb.toString();
    }
}
