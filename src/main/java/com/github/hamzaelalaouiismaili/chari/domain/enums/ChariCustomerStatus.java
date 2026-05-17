package com.github.hamzaelalaouiismaili.chari.domain.enums;

import lombok.Getter;

/**
 * Enum representing the status of a Chari customer account.
 * Maps to the status codes returned by Chari BaaS API.
 */
@Getter
public enum ChariCustomerStatus {
    /**
     * Number does not exist with ChariMoney.
     */
    NOT_EXISTS(0, "Number does not exist at ChariMoney"),

    /**
     * Number exists with ChariMoney but is not yet enrolled with Switch.
     */
    NOT_CONFIRMED(1, "Exists but not confirmed (OTP not entered)"),

    /**
     * Number exists and is registered with Switch.
     */
    CONFIRMED(2, "Confirmed and registered with Switch"),

    /**
     * Registered with Switch and active with ChariMoney.
     */
    ACTIVE(3, "Registered, active, and PIN created"),

    /**
     * Number is temporarily blocked after max attempts.
     */
    LOCKED_TEMPORARY(4, "Temporarily locked (excessive attempts)"),

    /**
     * Number is blocked.
     */
    LOCKED(5, "Blocked");

    private final int code;
    private final String description;

    ChariCustomerStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get ChariCustomerStatus from numeric code.
     *
     * @param code the status code from Chari API
     * @return the corresponding ChariCustomerStatus, or NOT_EXISTS if unknown
     */
    public static ChariCustomerStatus fromCode(Integer code) {
        if (code == null) {
            return NOT_EXISTS;
        }
        for (ChariCustomerStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return NOT_EXISTS;
    }

    /**
     * Check if the customer can perform transactions.
     */
    public boolean canTransact() {
        return this == ACTIVE;
    }

    /**
     * Check if the customer needs OTP confirmation.
     */
    public boolean needsOtpConfirmation() {
        return this == NOT_CONFIRMED;
    }

    /**
     * Check if the customer account is locked.
     */
    public boolean isLocked() {
        return this == LOCKED_TEMPORARY || this == LOCKED;
    }

    /**
     * Check if the customer needs to register.
     */
    public boolean needsRegistration() {
        return this == NOT_EXISTS;
    }
}
