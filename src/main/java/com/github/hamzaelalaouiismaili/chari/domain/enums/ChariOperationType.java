package com.github.hamzaelalaouiismaili.chari.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Chari BaaS operation types.
 * Integer codes used in webhook payloads to identify the operation category.
 */
@Getter
@RequiredArgsConstructor
public enum ChariOperationType {

    CASHIN(1, "CashIn"),
    CASHOUT(2, "CashOut"),
    TRANSFER(3, "Transfer"),
    MOBILE_PAYMENT(5, "Merchant mobile payment"),
    PAYMENT_REFUND(7, "Payment Refund"),
    BANK_TRANSFER(9, "Bank Transfer"),
    RECHARGE(10, "Mobile top-up / recharge"),
    CHARGEBACK(12, "Chargeback"),
    VOUCHER(23, "Voucher purchase"),
    CARD_PAYMENT(24, "Merchant card payment"),
    BILL_PAYMENT(25, "Bill payment"),

    UNKNOWN(-1, "Unknown");

    @JsonValue
    private final int code;
    private final String label;

    @JsonCreator
    public static ChariOperationType fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (ChariOperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    /**
     * Whether this operation involves a bank transfer.
     */
    public boolean isBankTransfer() {
        return this == BANK_TRANSFER;
    }

    /**
     * Whether this operation is a card-based operation.
     */
    public boolean isCardOperation() {
        return this == CARD_PAYMENT || this == CASHIN;
    }

    /**
     * Whether this operation is a debit (money leaving the wallet).
     */
    public boolean isDebit() {
        return this == CASHOUT || this == MOBILE_PAYMENT || this == BANK_TRANSFER
                || this == VOUCHER || this == CARD_PAYMENT || this == RECHARGE || this == BILL_PAYMENT;
    }

    /**
     * Whether this operation is a credit (money entering the wallet).
     */
    public boolean isCredit() {
        return this == CASHIN || this == PAYMENT_REFUND;
    }
}
