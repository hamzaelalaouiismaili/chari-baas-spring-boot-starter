package com.github.hamzaelalaouiismaili.chari.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariWebhookSignatureException;
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookDispatcher;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class ChariWebhookDispatcherTest {

    @Test
    void dispatchesBankTransferToHandler() {
        RecordingHandler handler = new RecordingHandler();
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties("secret"),
                objectMapper(),
                List.of(handler));
        String body = "{\"data\":{\"WebhookId\":123,\"EventId\":\"bank-transfer.initiated\",\"OperationType\":9,\"OperationId\":456}}";
        String timestamp = Long.toString(System.currentTimeMillis());

        ChariWebhookResponse response = dispatcher.dispatch(signature("secret", timestamp, body), timestamp, body);

        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(handler.bankTransferOperationId).isEqualTo(456L);
        assertThat(handler.bankTransferInitiatedOperationId).isEqualTo(456L);
        assertThat(handler.eventId).isEqualTo("bank-transfer.initiated");
    }

    @Test
    void dispatchesPaymentReceivedByEventId() {
        RecordingHandler handler = new RecordingHandler();
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties(null),
                objectMapper(),
                List.of(handler));
        String body = """
                {
                  "data": {
                    "WebhookId": 123,
                    "EventId": "payment.received",
                    "CRequestId": "a4d1e0b5-9f6a-4c1d-bc7b-2d0a7f4b9b12",
                    "OperationId": 456,
                    "TransactionId": 789,
                    "OperationType": 5,
                    "OperationStatus": 2,
                    "GatewayTrackId": "track-1"
                  }
                }
                """;

        ChariWebhookResponse response = dispatcher.dispatch(null, null, body);

        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(handler.paymentReceivedOperationId).isEqualTo(456L);
        assertThat(handler.transactionId).isEqualTo(789L);
        assertThat(handler.gatewayTrackId).isEqualTo("track-1");
        assertThat(handler.operationType).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
        assertThat(handler.operationStatus).isEqualTo(ChariOperationStatus.COMPLETED);
    }

    @Test
    void dispatchesCashinCardAuthorizedWithOfficialEventBodyProperties() {
        RecordingHandler handler = new RecordingHandler();
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties(null),
                objectMapper(),
                List.of(handler));
        String body = """
                {
                  "data": {
                    "WebhookId": "wh_123",
                    "EventId": "cashin.card.authorized",
                    "CRequestId": "req-123",
                    "OperationId": 456,
                    "TransactionId": 789,
                    "OperationType": 1,
                    "OperationStatus": 2,
                    "CreatedAt": "2025-11-05T10:12:00Z",
                    "ExecutedAt": "2025-11-05T10:12:22Z",
                    "Amount": 250.00,
                    "FeeAmount": 3.50,
                    "PrimaryAccountNumber": "+212611111111",
                    "SecondaryAccountNumber": "+212622222222",
                    "Method": "Card",
                    "CustomData": "merchant-data",
                    "GatewayTrackId": "track-1",
                    "GatewayOrderId": "order-1",
                    "GatewayReferenceId": "ref-1",
                    "NetworkName": "Network",
                    "Reference": "1122334455"
                  }
                }
                """;

        ChariWebhookResponse response = dispatcher.dispatch(null, null, body);

        assertThat(response.getStatus()).isEqualTo("accepted");
        assertThat(handler.cashInCardWebhookId).isEqualTo("wh_123");
        assertThat(handler.cashInCardCRequestId).isEqualTo("req-123");
        assertThat(handler.cashInCardGatewayOrderId).isEqualTo("order-1");
        assertThat(handler.cashInCardGatewayReferenceId).isEqualTo("ref-1");
        assertThat(handler.cashInCardReference).isEqualTo("1122334455");
        assertThat(handler.cashInCardNetworkName).isEqualTo("Network");
        assertThat(handler.cashInCardFeeAmount).isEqualByComparingTo("3.50");
        assertThat(handler.operationType).isEqualTo(ChariOperationType.CASHIN);
        assertThat(handler.operationStatus).isEqualTo(ChariOperationStatus.COMPLETED);
    }

    @Test
    void dispatchesFlatLivePayloadByOperationType() {
        RecordingHandler handler = new RecordingHandler();
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties(null),
                objectMapper(),
                List.of(handler));
        // Exact live payload shape from Chari: flat (no "data" wrapper),
        // WebhookEventId, offset-less CreatedAt, escaped + in account numbers.
        String body = "{\"WebhookEventId\":\"webhook-2ad1112b76\","
                + "\"CRequestId\":\"feced5e5-9fb7-44f2-97f8-37f8cc8540a9\","
                + "\"OperationId\":13662,\"OperationType\":1,\"OperationStatus\":2,"
                + "\"CreatedAt\":\"2026-06-30T22:02:01.127087\","
                + "\"ExecutedAt\":\"2026-06-30T23:02:13.3321866Z\","
                + "\"Amount\":10.0,\"FeeAmount\":0.0,\"CustomData\":null,\"ExternalId\":null,"
                + "\"PrimaryAccountNumber\":\"\\u002B212608814003\","
                + "\"SecondaryAccountNumber\":\"\\u002B212608814003\","
                + "\"Description\":\"6286\",\"Method\":\"card\","
                + "\"GatewayTrackId\":\"938029614288\",\"GatewayOrderId\":\"CHf3cca1626e04\","
                + "\"GatewayReferenceId\":\"938029614288\"}";

        ChariWebhookResponse response = dispatcher.dispatch(null, null, body);

        assertThat(response.getStatus()).isEqualTo("accepted");
        // OperationType 1 -> CASHIN
        assertThat(handler.cashInOperationId).isEqualTo(13662L);
        assertThat(handler.cashInWebhookEventId).isEqualTo("webhook-2ad1112b76");
        assertThat(handler.cashInPrimaryAccount).isEqualTo("+212608814003");
        assertThat(handler.cashInDescription).isEqualTo("6286");
        // offset-less CreatedAt parsed as UTC
        assertThat(handler.cashInCreatedAt).isEqualTo(java.time.Instant.parse("2026-06-30T22:02:01.127087Z"));
        assertThat(handler.cashInExecutedAt).isEqualTo(java.time.Instant.parse("2026-06-30T23:02:13.3321866Z"));
    }

    @Test
    void rejectsInvalidSignature() {
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties("secret"),
                objectMapper(),
                List.of());
        String body = "{\"data\":{\"WebhookId\":123,\"OperationType\":9}}";

        assertThatThrownBy(() -> dispatcher.dispatch("bad", Long.toString(System.currentTimeMillis()), body))
                .isInstanceOf(ChariWebhookSignatureException.class)
                .hasMessage("Invalid webhook signature");
    }

    @Test
    void acceptsWithoutHandlersWhenSignatureValid() {
        ChariWebhookDispatcher dispatcher = new ChariWebhookDispatcher(
                properties(null),
                objectMapper(),
                List.of());

        ChariWebhookResponse response = dispatcher.dispatch(null, null, "{\"data\":{\"WebhookId\":123}}");

        assertThat(response.getStatus()).isEqualTo("accepted");
    }

    private static ChariBaasProperties properties(String secret) {
        ChariBaasProperties properties = new ChariBaasProperties();
        properties.setBaseUrl("https://sandbox.charimoney.com");
        properties.setApiKey("test-key");
        properties.setWebhookSecret(secret);
        return properties;
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private static String signature(String secret, String timestamp, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class RecordingHandler implements ChariWebhookHandler {
        private Long bankTransferOperationId;
        private Long bankTransferInitiatedOperationId;
        private Long paymentReceivedOperationId;
        private Long transactionId;
        private String gatewayTrackId;
        private String eventId;
        private ChariOperationType operationType;
        private ChariOperationStatus operationStatus;
        private String cashInCardWebhookId;
        private String cashInCardCRequestId;
        private String cashInCardGatewayOrderId;
        private String cashInCardGatewayReferenceId;
        private String cashInCardReference;
        private String cashInCardNetworkName;
        private java.math.BigDecimal cashInCardFeeAmount;
        private Long cashInOperationId;
        private String cashInWebhookEventId;
        private String cashInPrimaryAccount;
        private String cashInDescription;
        private java.time.Instant cashInCreatedAt;
        private java.time.Instant cashInExecutedAt;

        @Override
        public void onCashIn(WebhookData data) {
            this.cashInOperationId = data.getOperationId();
            this.cashInWebhookEventId = data.getWebhookEventId();
            this.cashInPrimaryAccount = data.getPrimaryAccountNumber();
            this.cashInDescription = data.getDescription();
            this.cashInCreatedAt = data.getCreatedAt();
            this.cashInExecutedAt = data.getExecutedAt();
        }

        @Override
        public void onBankTransfer(WebhookData data) {
            this.bankTransferOperationId = data.getOperationId();
        }

        @Override
        public void onBankTransferInitiated(WebhookData data) {
            ChariWebhookHandler.super.onBankTransferInitiated(data);
            this.bankTransferInitiatedOperationId = data.getOperationId();
            this.eventId = data.getEventId();
        }

        @Override
        public void onPaymentReceived(WebhookData data) {
            this.paymentReceivedOperationId = data.getOperationId();
            this.transactionId = data.getTransactionId();
            this.gatewayTrackId = data.getGatewayTrackId();
            this.operationType = data.getTypedOperationType();
            this.operationStatus = data.getTypedOperationStatus();
        }

        @Override
        public void onCashInCardAuthorized(WebhookData data) {
            this.cashInCardWebhookId = data.getWebhookId();
            this.cashInCardCRequestId = data.getCRequestId();
            this.cashInCardGatewayOrderId = data.getGatewayOrderId();
            this.cashInCardGatewayReferenceId = data.getGatewayReferenceId();
            this.cashInCardReference = data.getReference();
            this.cashInCardNetworkName = data.getNetworkName();
            this.cashInCardFeeAmount = data.getFeeAmount();
            this.operationType = data.getTypedOperationType();
            this.operationStatus = data.getTypedOperationStatus();
        }
    }
}
