package com.github.hamzaelalaouiismaili.chari.domain.exception;

/**
 * Raised when a Chari webhook signature or replay timestamp is invalid.
 */
public class ChariWebhookSignatureException extends RuntimeException {

    public ChariWebhookSignatureException(String message) {
        super(message);
    }
}
