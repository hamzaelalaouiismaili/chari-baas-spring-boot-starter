package com.github.hamzaelalaouiismaili.chari.util;

/**
 * Helpers for OTP codes accepted by the Chari API.
 */
public final class OtpCodeUtil {

    private static final int CONFIRMATION_CODE_LENGTH = 6;
    private static final int CONFIRMATION_CODE_GROUP_SIZE = 3;

    private OtpCodeUtil() {
    }

    /**
     * Formats customer confirmation OTPs as XXX-XXX.
     *
     * @param code OTP code as entered by the caller
     * @return formatted OTP code, or {@code null} when the input is null
     */
    public static String normalizeConfirmationCode(String code) {
        if (code == null) {
            return null;
        }

        String cleaned = code.trim().replaceAll("[\\s-]+", "");
        if (cleaned.length() != CONFIRMATION_CODE_LENGTH) {
            return code.trim();
        }
        return cleaned.substring(0, CONFIRMATION_CODE_GROUP_SIZE)
                + "-"
                + cleaned.substring(CONFIRMATION_CODE_GROUP_SIZE);
    }
}
