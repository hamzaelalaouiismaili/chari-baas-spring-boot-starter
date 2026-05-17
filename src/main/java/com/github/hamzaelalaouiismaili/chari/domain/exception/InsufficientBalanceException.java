package com.github.hamzaelalaouiismaili.chari.domain.exception;

import java.math.BigDecimal;
import lombok.Getter;

/**
 * Raised when a wallet cannot cover the requested amount.
 */
@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final BigDecimal availableBalance;
    private final BigDecimal requiredAmount;

    public InsufficientBalanceException(BigDecimal availableBalance, BigDecimal requiredAmount) {
        super("Insufficient balance: available=" + availableBalance + ", required=" + requiredAmount);
        this.availableBalance = availableBalance;
        this.requiredAmount = requiredAmount;
    }
}
