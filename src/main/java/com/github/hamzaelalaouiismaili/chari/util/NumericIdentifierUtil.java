package com.github.hamzaelalaouiismaili.chari.util;

/**
 * Normalizes numeric identifiers that Chari APIs match exactly.
 */
public final class NumericIdentifierUtil {

    private NumericIdentifierUtil() {
    }

    /**
     * Removes whitespace from numeric identifiers such as agent codes and RIBs.
     *
     * @param value identifier value
     * @return identifier without whitespace, or {@code null} when the input is null
     */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\s+", "");
    }
}
