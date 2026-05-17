package com.github.hamzaelalaouiismaili.chari.domain.enums;

import lombok.Getter;

/**
 * Enum representing the type of transfer operation.
 */
@Getter
public enum TransferOperationType {
    /**
     * Wallet to wallet transfer.
     */
    WALLET_TO_WALLET(1, "Wallet to Wallet Transfer"),

    /**
     * Bank transfer from wallet.
     */
    BANK_TRANSFER(2, "Bank Transfer"),

    /**
     * QR code payment.
     */
    QR_PAYMENT(3, "QR Code Payment"),

    /**
     * Card cash-in (funding via card).
     */
    CARD_CASHIN(4, "Card Cash-in"),

    /**
     * Agent to wallet transfer.
     */
    AGENT_TO_WALLET(5, "Agent to Wallet Transfer");

    private final int code;
    private final String description;

    TransferOperationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get TransferOperationType from numeric code.
     */
    public static TransferOperationType fromCode(int code) {
        for (TransferOperationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transfer operation type code: " + code);
    }
}
