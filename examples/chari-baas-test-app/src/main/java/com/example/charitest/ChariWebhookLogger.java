package com.example.charitest;

import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChariWebhookLogger implements ChariWebhookHandler {

    private static final Logger log = LoggerFactory.getLogger(ChariWebhookLogger.class);

    @Override
    public void onUnknown(WebhookData data) {
        log.info("Chari webhook received: eventId={}, operationId={}, status={}, amount={}",
                data.getEventId(), data.getOperationId(), data.getOperationStatus(), data.getAmount());
    }

    @Override
    public void onPaymentReceived(WebhookData data) {
        log.info("Payment received: operationId={}, amount={}, primary={}, secondary={}",
                data.getOperationId(), data.getAmount(), data.getPrimaryAccountNumber(),
                data.getSecondaryAccountNumber());
    }

    @Override
    public void onCashInCardAuthorized(WebhookData data) {
        log.info("Cash-in card authorized: operationId={}, orderId={}, trackId={}",
                data.getOperationId(), data.getGatewayOrderId(), data.getGatewayTrackId());
    }
}
