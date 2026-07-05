package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Chari BaaS webhook event types.
 * These are the event names sent in the webhook callback URL registration.
 */
@Getter
@RequiredArgsConstructor
public enum ChariWebhookEventType {

    // ==================== Customer Events ====================
    CUSTOMER_KYC("customer.kyc", "KYC process started"),
    CUSTOMER_LEVEL_UPDATED("customer.level.updated", "Customer account level updated"),

    // ==================== Operation Events ====================
    OPERATION_CREATED("operation.created", "Operation is created (pending)"),
    OPERATION_UPDATED("operation.updated", "Operation status changes"),

    // ==================== Card Events ====================
    CASHIN_CARD_AUTHORIZED("cashin.card.authorized", "CashIn by Card accepted"),
    PAYMENT_CARD_AUTHORIZED("payment.card.authorized", "Payment by Card accepted"),
    PAYMENT_RECEIVED("payment.received", "Merchant payment received"),
    PAYMENT_CONFIRMED("payment.confirmed", "Bill payment confirmed"),
    PAYMENT_CANCELLED("payment.cancelled", "Bill payment cancelled"),
    PAYMENT_REFUNDED("payment.refunded", "Bill payment refunded"),
    PAYMENT_FAILED("payment.failed", "Bill payment failed"),

    // ==================== Bank Transfer Events ====================
    BANK_TRANSFER_INITIATED("bank-transfer.initiated", "Bank transfer sent"),
    BANK_TRANSFER_COMPLETED("bank-transfer.completed", "Bank transfer settled"),
    BANK_TRANSFER_FAILED("bank-transfer.failed", "Bank transfer rejected/returned"),
    BANK_TRANSFER_RECEIVED("bank-transfer.received", "Bank transfer received"),

    // ==================== Transfer Events ====================
    TRANSFER_RECEIVED("transfer.received", "Transfer received"),

    // ==================== Network Events ====================
    CASHIN_NETWORK_EXECUTED("cashin.network.executed", "CashIn by reference executed"),
    CASHOUT_NETWORK_EXECUTED("cashout.network.executed", "CashOut by reference executed"),

    // ==================== Unknown ====================
    UNKNOWN("unknown", "Unknown event type");

    @JsonValue
    private final String value;
    private final String description;

    @JsonCreator
    public static ChariWebhookEventType fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (ChariWebhookEventType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
