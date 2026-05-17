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

    public boolean hasErrorCode(ChariErrorCode errorCode) {
        return this.knownErrorCode == errorCode;
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
