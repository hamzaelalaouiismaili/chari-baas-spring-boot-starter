package com.github.hamzaelalaouiismaili.chari.util;

import java.util.regex.Pattern;

/**
 * Phone number helpers used by the Chari BaaS client.
 */
public final class PhoneNumberUtil {

    private static final Pattern MOROCCO_PATTERN = Pattern.compile("^\\+212[5-7]\\d{8}$");
    private static final Pattern NINE_DIGIT_NUMBER_PATTERN = Pattern.compile("^\\d{9}$");
    private static final String MOROCCO_PREFIX = "+212";

    private PhoneNumberUtil() {
    }

    /**
     * Normalizes a phone number to international format.
     *
     * @param phone phone number in local or international format
     * @return normalized phone number, for example {@code +212612345678}
     */
    public static String normalize(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        String cleaned = phone.trim().replaceAll("[\\s\\-().]", "");
        if (cleaned.startsWith("00")) {
            cleaned = "+" + cleaned.substring(2);
        } else if (cleaned.startsWith("0")) {
            cleaned = MOROCCO_PREFIX + cleaned.substring(1);
        } else if (NINE_DIGIT_NUMBER_PATTERN.matcher(cleaned).matches()) {
            cleaned = MOROCCO_PREFIX + cleaned;
        } else if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        return cleaned;
    }

    /**
     * Checks whether the phone number is a valid Moroccan mobile number.
     */
    public static boolean isValidMoroccanNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return MOROCCO_PATTERN.matcher(normalize(phone)).matches();
    }

    /**
     * Converts an international Moroccan number back to local display format.
     */
    public static String getLocalNumber(String phone) {
        String normalized = normalize(phone);
        if (normalized.startsWith(MOROCCO_PREFIX)) {
            return "0" + normalized.substring(MOROCCO_PREFIX.length());
        }
        return normalized;
    }

    /**
     * Masks a phone number for logs, preserving only the last four characters.
     */
    public static String mask(String phone) {
        String normalized = normalize(phone);
        if (normalized.length() <= 4) {
            return "****";
        }
        return "*".repeat(normalized.length() - 4) + normalized.substring(normalized.length() - 4);
    }
}
