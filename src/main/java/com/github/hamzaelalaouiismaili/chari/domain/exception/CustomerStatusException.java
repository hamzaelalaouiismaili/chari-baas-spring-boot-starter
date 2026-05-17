package com.github.hamzaelalaouiismaili.chari.domain.exception;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCustomerStatus;
import lombok.Getter;

/**
 * Raised when a customer is not in the status required for an operation.
 */
@Getter
public class CustomerStatusException extends RuntimeException {

    private final String phoneNumber;
    private final ChariCustomerStatus currentStatus;
    private final ChariCustomerStatus requiredStatus;

    public CustomerStatusException(String phoneNumber, ChariCustomerStatus currentStatus) {
        this(phoneNumber, currentStatus, ChariCustomerStatus.ACTIVE);
    }

    public CustomerStatusException(
            String phoneNumber,
            ChariCustomerStatus currentStatus,
            ChariCustomerStatus requiredStatus) {
        super("Customer " + phoneNumber + " has status " + currentStatus + " but requires " + requiredStatus);
        this.phoneNumber = phoneNumber;
        this.currentStatus = currentStatus;
        this.requiredStatus = requiredStatus;
    }

    public static CustomerStatusException locked(String phoneNumber, ChariCustomerStatus status) {
        return new CustomerStatusException(phoneNumber, status, ChariCustomerStatus.ACTIVE);
    }

    public static CustomerStatusException notConfirmed(String phoneNumber) {
        return new CustomerStatusException(phoneNumber, ChariCustomerStatus.NOT_CONFIRMED, ChariCustomerStatus.ACTIVE);
    }

    public static CustomerStatusException notRegistered(String phoneNumber) {
        return new CustomerStatusException(phoneNumber, ChariCustomerStatus.NOT_EXISTS, ChariCustomerStatus.ACTIVE);
    }
}
