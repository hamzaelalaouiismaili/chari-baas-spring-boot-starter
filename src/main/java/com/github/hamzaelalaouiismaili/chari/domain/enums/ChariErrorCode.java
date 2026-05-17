package com.github.hamzaelalaouiismaili.chari.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Known Chari BaaS error codes returned in non-success responses.
 */
@Getter
@RequiredArgsConstructor
public enum ChariErrorCode {

        MISSING_PARAMETERS(10001, "Missing Parameters.", "General / validation"),
        INVALID_PHONE_NUMBER_FORMAT(20000, "The phone number format is invalid.", "Customer & registration"),
        USER_NOT_FOUND(20005, "The specified user could not be found.", "Customer & registration"),
        INVALID_INITIAL_PARAMETERS(20006, "The initial parameters provided are incorrect or invalid.",
                        "Customer & registration"),
        INVALID_MCC(20007, "The Merchant Category Code (MCC) provided is incorrect or not recognized.",
                        "Customer & registration"),
        REGISTRATION_TEMPORARILY_LOCKED(20008,
                        "Registration is temporarily locked due to security or policy restrictions.",
                        "Customer & registration"),
        REQUEST_PENDING_CONFIRMATION(20009,
                        "The request is pending confirmation. Please wait for further processing.",
                        "Customer & registration"),
        NO_PENDING_REQUEST_FOR_PHONE_NUMBER(20017,
                        "There is no pending request associated with the provided Phone Number.",
                        "Customer & registration"),
        INCORRECT_PIN(26001, "The entered PIN is incorrect.", "PIN management"),
        PIN_ALREADY_SET(26004, "A PIN has already been set for this wallet.", "PIN management"),
        INVALID_PIN_FORMAT(26005,
                        "The provided PIN does not meet the required format (must be a 4-digit number).",
                        "PIN management"),
        BENEFICIARY_ALREADY_EXISTS(27000, "The Beneficiary already exists with the same phoneNumber.",
                        "Beneficiary management"),
        BENEFICIARY_NOT_FOUND(27001, "The Beneficiary does not exist.", "Beneficiary management"),
        UPGRADE_REQUEST_UNDER_REVIEW(32000, "An upgrade request is already under review for this account.",
                        "KYC / account upgrade"),
        UNAUTHORIZED(401, "Authentication credentials (API KEY) not authorized.", "Authentication"),
        ACCOUNT_LOCKED(423, "The access is locked for the given customer.", "Customer & registration"),
        UNKNOWN(-1, "Unknown Chari error.", "Unknown");

        private final int code;
        private final String defaultMessage;
        private final String domain;

        public static ChariErrorCode fromCode(Integer code) {
                if (code == null) {
                        return UNKNOWN;
                }
                for (ChariErrorCode errorCode : values()) {
                        if (errorCode.code == code) {
                                return errorCode;
                        }
                }
                return UNKNOWN;
        }
}
