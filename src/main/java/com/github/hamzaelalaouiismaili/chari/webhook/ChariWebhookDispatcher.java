package com.github.hamzaelalaouiismaili.chari.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariWebhookEventType;
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariWebhookSignatureException;
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies, parses, and dispatches Chari webhook events to application
 * handlers.
 */
@Slf4j
public class ChariWebhookDispatcher {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long ALLOWED_DRIFT_MS = 5 * 60 * 1_000L;

    private final ChariBaasProperties properties;
    private final ObjectMapper objectMapper;
    private final List<ChariWebhookHandler> handlers;

    public ChariWebhookDispatcher(
            ChariBaasProperties properties,
            ObjectMapper objectMapper,
            List<ChariWebhookHandler> handlers) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.handlers = List.copyOf(handlers);
    }

    public ChariWebhookResponse dispatch(String signature, String timestamp, String rawBody) {
        verifySignature(signature, timestamp, rawBody);

        WebhookData data;
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            // Chari sends the event fields flat at the top level. Older/wrapped
            // payloads nest them under "data"; support both.
            JsonNode dataNode = root.has("data") && root.get("data").isObject()
                    ? root.get("data")
                    : root;
            data = objectMapper.treeToValue(dataNode, WebhookData.class);
        } catch (JsonProcessingException e) {
            log.warn("Rejected Chari webhook with invalid JSON: {}", e.getMessage());
            return ChariWebhookResponse.rejected("Invalid JSON payload");
        }

        if (data == null) {
            return ChariWebhookResponse.rejected("Empty webhook payload");
        }

        ChariOperationType operationType = data.getOperationType() == null
                ? ChariOperationType.UNKNOWN
                : ChariOperationType.fromCode(data.getOperationType());
        ChariWebhookEventType eventType = data.getEventType();

        if (handlers.isEmpty()) {
            log.info("Accepted Chari webhook {} with no registered ChariWebhookHandler", webhookId(data));
            return ChariWebhookResponse.accepted();
        }

        for (ChariWebhookHandler handler : handlers) {
            if (eventType != ChariWebhookEventType.UNKNOWN) {
                dispatchByEventType(handler, eventType, data);
            } else {
                dispatchByOperationType(handler, operationType, data);
            }
        }
        return ChariWebhookResponse.accepted();
    }

    public void verifySignature(String signature, String timestamp, String rawBody) {
        if (!properties.getWebhook().isVerify()) {
            log.debug("chari.baas.webhook.verify is false; bypassing Chari webhook signature verification");
            return;
        }
        String secret = properties.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            log.warn("chari.baas.webhook-secret is blank; skipping Chari webhook signature verification");
            return;
        }
        if (signature == null || signature.isBlank() || timestamp == null || timestamp.isBlank()) {
            throw new ChariWebhookSignatureException("Missing webhook signature headers");
        }
        if (!isTimestampRecent(timestamp)) {
            throw new ChariWebhookSignatureException("Webhook timestamp is too old or invalid");
        }

        String expected = hmacSha256(secret, timestamp + "." + rawBody);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new ChariWebhookSignatureException("Invalid webhook signature");
        }
    }

    private void dispatchByEventType(
            ChariWebhookHandler handler,
            ChariWebhookEventType eventType,
            WebhookData data) {
        switch (eventType) {
            case CUSTOMER_KYC -> handler.onCustomerKyc(data);
            case CUSTOMER_LEVEL_UPDATED -> handler.onCustomerLevelUpdated(data);
            case OPERATION_CREATED -> handler.onOperationCreated(data);
            case OPERATION_UPDATED -> handler.onOperationUpdated(data);
            case CASHIN_CARD_AUTHORIZED -> handler.onCashInCardAuthorized(data);
            case PAYMENT_CARD_AUTHORIZED -> handler.onPaymentCardAuthorized(data);
            case PAYMENT_RECEIVED -> handler.onPaymentReceived(data);
            case BANK_TRANSFER_INITIATED -> handler.onBankTransferInitiated(data);
            case BANK_TRANSFER_COMPLETED -> handler.onBankTransferCompleted(data);
            case BANK_TRANSFER_FAILED -> handler.onBankTransferFailed(data);
            case BANK_TRANSFER_RECEIVED -> handler.onBankTransferReceived(data);
            case TRANSFER_RECEIVED -> handler.onTransferReceived(data);
            case CASHIN_NETWORK_EXECUTED -> handler.onCashInNetworkExecuted(data);
            case CASHOUT_NETWORK_EXECUTED -> handler.onCashOutNetworkExecuted(data);
            default -> handler.onUnknown(data);
        }
    }

    private void dispatchByOperationType(
            ChariWebhookHandler handler,
            ChariOperationType operationType,
            WebhookData data) {
        switch (operationType) {
            case CASHIN -> handler.onCashIn(data);
            case CASHOUT -> handler.onCashOut(data);
            case TRANSFER -> handler.onTransfer(data);
            case MOBILE_PAYMENT -> handler.onPaymentPush(data);
            case BANK_TRANSFER -> handler.onBankTransfer(data);
            case CARD_PAYMENT -> handler.onPaymentCard(data);
            case PAYMENT_REFUND -> handler.onPaymentRefund(data);
            case CHARGEBACK -> handler.onChargeback(data);
            default -> handler.onUnknown(data);
        }
    }

    private static String webhookId(WebhookData data) {
        return data.getWebhookEventId() != null ? data.getWebhookEventId() : data.getWebhookId();
    }

    private boolean isTimestampRecent(String timestamp) {
        try {
            long receivedAt = Long.parseLong(timestamp);
            return Math.abs(System.currentTimeMillis() - receivedAt) <= ALLOWED_DRIFT_MS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute Chari webhook signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
