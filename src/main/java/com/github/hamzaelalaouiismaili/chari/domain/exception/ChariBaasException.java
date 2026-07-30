package com.github.hamzaelalaouiismaili.chari.domain.exception;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariErrorCode;
import lombok.Getter;

/**
 * Runtime exception used for Chari API failures.
 */
@Getter
public class ChariBaasException extends RuntimeException {

    private final String stage;
    private final Integer httpStatusCode;
    private final Integer errorCode;
    private final String errorDescription;
    private final ChariErrorCode knownErrorCode;

    public ChariBaasException(String message, String stage) {
        super(message);
        this.stage = stage;
        this.httpStatusCode = null;
        this.errorCode = null;
        this.errorDescription = null;
        this.knownErrorCode = ChariErrorCode.UNKNOWN;
    }

    public ChariBaasException(String message, String stage, Integer httpStatusCode) {
        this(message, stage, httpStatusCode, null, null);
    }

    public ChariBaasException(
            String message,
            String stage,
            Integer httpStatusCode,
            Integer errorCode,
            String errorDescription) {
        super(message);
        this.stage = stage;
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.knownErrorCode = ChariErrorCode.fromCode(errorCode);
    }

    public ChariBaasException(String message, String stage, Throwable cause) {
        super(message, cause);
        this.stage = stage;
        this.httpStatusCode = null;
        this.errorCode = null;
        this.errorDescription = null;
        this.knownErrorCode = ChariErrorCode.UNKNOWN;
    }

    public static ChariBaasException timeout(String stage) {
        return new ChariBaasException("Chari API request timed out", stage, 504);
    }

    public static ChariBaasException connectionError(String stage, Throwable cause) {
        return new ChariBaasException("Failed to connect to Chari API: " + cause.getMessage(), stage, cause);
    }

    public static ChariBaasException apiError(String stage, String message, Integer statusCode) {
        return new ChariBaasException(message, stage, statusCode);
    }

    public static ChariBaasException apiError(
            String stage,
            String message,
            Integer statusCode,
            Integer errorCode,
            String errorDescription) {
        return new ChariBaasException(message, stage, statusCode, errorCode, errorDescription);
    }

    /**
     * Builds an exception from a Chari {@code errorCode}/{@code errorDescription}
     * envelope, with a self-explanatory message: the failing stage, the HTTP
     * status, the numeric code, the documented meaning of that code when it is
     * known, and the raw provider text.
     *
     * @param fallbackMessage used when Chari returned neither a known code nor a
     *                        description (typically the transport error message)
     */
    public static ChariBaasException fromErrorResponse(
            String stage,
            Integer statusCode,
            Integer errorCode,
            String errorDescription,
            String fallbackMessage) {
        ChariErrorCode known = ChariErrorCode.fromCode(errorCode);
        StringBuilder message = new StringBuilder()
                .append('[').append(stage == null ? "UNKNOWN_STAGE" : stage).append("] Chari API error");
        if (errorCode != null) {
            message.append(' ').append(errorCode);
        }
        if (statusCode != null) {
            message.append(" (HTTP ").append(statusCode).append(')');
        }
        message.append(": ");

        if (known != ChariErrorCode.UNKNOWN) {
            message.append(known.getDefaultMessage());
            if (hasText(errorDescription) && !known.getDefaultMessage().equalsIgnoreCase(errorDescription.trim())) {
                message.append(" Chari reported: \"").append(errorDescription.trim()).append("\".");
            }
        } else if (hasText(errorDescription)) {
            message.append(errorDescription.trim());
        } else if (hasText(fallbackMessage)) {
            message.append(fallbackMessage.trim());
        } else {
            message.append("no error description returned by Chari.");
        }

        return new ChariBaasException(message.toString(), stage, statusCode, errorCode, errorDescription);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public boolean hasErrorCode(ChariErrorCode errorCode) {
        return this.knownErrorCode == errorCode;
    }

    public boolean hasAnyErrorCode(ChariErrorCode... errorCodes) {
        for (ChariErrorCode candidate : errorCodes) {
            if (this.knownErrorCode == candidate) {
                return true;
            }
        }
        return false;
    }

    /** True when Fatourati reported that the account has nothing outstanding (35008). */
    public boolean isNoBillToPay() {
        return knownErrorCode == ChariErrorCode.BILL_NO_BILL_TO_PAY;
    }

    /**
     * True when the bill lookup failed because the identification values do not
     * resolve to a payable account (35008 / 35026) — a user-fixable input error
     * rather than an outage.
     */
    public boolean isBillLookupFailure() {
        return hasAnyErrorCode(ChariErrorCode.BILL_NO_BILL_TO_PAY, ChariErrorCode.BILL_SYSTEM_ERROR);
    }

    public boolean isAuthenticationFailure() {
        return Integer.valueOf(401).equals(httpStatusCode) || knownErrorCode == ChariErrorCode.UNAUTHORIZED;
    }

    public boolean isValidationError() {
        return Integer.valueOf(422).equals(httpStatusCode) || knownErrorCode == ChariErrorCode.MISSING_PARAMETERS;
    }

    public boolean isBusinessError() {
        return Integer.valueOf(400).equals(httpStatusCode);
    }

    public boolean isAccountLocked() {
        return Integer.valueOf(423).equals(httpStatusCode) || knownErrorCode == ChariErrorCode.ACCOUNT_LOCKED;
    }
}
