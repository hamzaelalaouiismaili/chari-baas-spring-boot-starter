package com.github.hamzaelalaouiismaili.chari.webhook;

import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;

/**
 * Application extension point for Chari webhook events.
 * <p>
 * Implement this interface as a Spring bean and override only the events your
 * application needs.
 */
public interface ChariWebhookHandler {

    default void onCustomerKyc(WebhookData data) {
        onUnknown(data);
    }

    default void onCustomerLevelUpdated(WebhookData data) {
        onUnknown(data);
    }

    default void onOperationCreated(WebhookData data) {
        onUnknown(data);
    }

    default void onOperationUpdated(WebhookData data) {
        onUnknown(data);
    }

    default void onCashInCardAuthorized(WebhookData data) {
        onCashIn(data);
    }

    default void onPaymentCardAuthorized(WebhookData data) {
        onPaymentCard(data);
    }

    default void onBankTransferInitiated(WebhookData data) {
        onBankTransfer(data);
    }

    default void onBankTransferCompleted(WebhookData data) {
        onBankTransfer(data);
    }

    default void onBankTransferFailed(WebhookData data) {
        onBankTransfer(data);
    }

    default void onBankTransferReceived(WebhookData data) {
        onBankTransfer(data);
    }

    default void onTransferReceived(WebhookData data) {
        onTransfer(data);
    }

    default void onPaymentReceived(WebhookData data) {
        onPaymentPush(data);
    }

    default void onBillPaymentConfirmed(WebhookData data) {
        onBillPayment(data);
    }

    default void onBillPaymentCancelled(WebhookData data) {
        onBillPayment(data);
    }

    default void onBillPaymentRefunded(WebhookData data) {
        onBillPayment(data);
    }

    default void onBillPaymentFailed(WebhookData data) {
        onBillPayment(data);
    }

    default void onCashInNetworkExecuted(WebhookData data) {
        onCashIn(data);
    }

    default void onCashOutNetworkExecuted(WebhookData data) {
        onCashOut(data);
    }

    default void onCashIn(WebhookData data) {
    }

    default void onCashOut(WebhookData data) {
    }

    default void onTransfer(WebhookData data) {
    }

    default void onPaymentPush(WebhookData data) {
    }

    default void onBankTransfer(WebhookData data) {
    }

    default void onPaymentCard(WebhookData data) {
    }

    default void onBillPayment(WebhookData data) {
    }

    default void onPaymentRefund(WebhookData data) {
    }

    default void onChargeback(WebhookData data) {
    }

    default void onUnknown(WebhookData data) {
    }
}
