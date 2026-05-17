package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Chari BaaS transaction types.
 * Integer codes used in webhook payloads to categorize the transaction.
 */
@Getter
@RequiredArgsConstructor
public enum ChariTransactionType {

    CASHIN(1, "CashIn"),
    CASHOUT(2, "CashOut"),
    TRANSFER(3, "Transfer"),
    MOBILE_PAYMENT(5, "Merchant mobile payment transaction"),
    TRANSACTION_FEES(6, "Transaction Fees"),
    PAYMENT_REFUND(7, "Payment Refund"),
    CHARGEBACK(9, "Chargeback"),
    CHARGEBACK_CANCELLATION(10, "Chargeback Cancellation"),
    BANK_TRANSFER(16, "Bank Transfer"),
    RECHARGE(17, "Mobile top-up / recharge transaction"),
    CASHBACK(18, "Cashback"),
    CARD_PAYMENT(24, "Merchant card payment transaction"),
    BILL_PAYMENT(25, "Bill payment transaction"),

    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String label;

    @JsonCreator
    public static ChariTransactionType fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariTransactionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
