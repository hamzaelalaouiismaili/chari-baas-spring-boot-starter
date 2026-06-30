package com.github.hamzaelalaouiismaili.chari.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariWebhookEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO matching the actual Chari BaaS webhook payload structure.
 * <p>
 * Chari delivers the event fields <strong>flat</strong> at the top level (no
 * {@code data} wrapper). The dispatcher also accepts a legacy {@code data}-wrapped
 * body for backward compatibility. Live payload example:
 *
 * <pre>
 * {
 *   "WebhookEventId": "webhook-2ad1112b76",
 *   "CRequestId": "feced5e5-9fb7-44f2-97f8-37f8cc8540a9",
 *   "OperationId": 13662,
 *   "OperationType": 1,
 *   "OperationStatus": 2,
 *   "CreatedAt": "2026-06-30T22:02:01.127087",
 *   "ExecutedAt": "2026-06-30T23:02:13.3321866Z",
 *   "Amount": 10.0,
 *   "FeeAmount": 0.0,
 *   "CustomData": null,
 *   "ExternalId": null,
 *   "PrimaryAccountNumber": "+212608814003",
 *   "SecondaryAccountNumber": "+212608814003",
 *   "Description": "6286",
 *   "Method": "card",
 *   "GatewayTrackId": "938029614288",
 *   "GatewayOrderId": "CHf3cca1626e04",
 *   "GatewayReferenceId": "938029614288"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChariWebhookEvent {

    private WebhookData data;

    /**
     * Inner DTO matching the Chari webhook "data" object.
     * Uses PascalCase @JsonProperty to match Chari's C#-style naming.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookData {

        @JsonProperty("WebhookId")
        private String webhookId;

        /**
         * Unique id of the webhook delivery, e.g. {@code "webhook-2ad1112b76"}.
         * Sent by Chari as {@code WebhookEventId} on the live payload.
         */
        @JsonProperty("WebhookEventId")
        private String webhookEventId;

        @JsonProperty("EventId")
        private String eventId;

        @JsonProperty("ExternalId")
        private String externalId;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("CRequestId")
        private String cRequestId;

        @JsonProperty("OperationId")
        private Long operationId;

        @JsonProperty("TransactionId")
        private Long transactionId;

        @JsonProperty("OperationType")
        private Integer operationType;

        @JsonProperty("OperationStatus")
        private Integer operationStatus;

        @JsonProperty("CreatedAt")
        @JsonDeserialize(using = LenientInstantDeserializer.class)
        private Instant createdAt;

        @JsonProperty("ExecutedAt")
        @JsonDeserialize(using = LenientInstantDeserializer.class)
        private Instant executedAt;

        @JsonProperty("Amount")
        private BigDecimal amount;

        @JsonProperty("FeeAmount")
        private BigDecimal feeAmount;

        @JsonProperty("CustomData")
        private String customData;

        @JsonProperty("PrimaryAccountNumber")
        private String primaryAccountNumber;

        @JsonProperty("SecondaryAccountNumber")
        private String secondaryAccountNumber;

        @JsonProperty("Method")
        private String method;

        @JsonProperty("Reference")
        private String reference;

        @JsonProperty("BankTransferBeneficiaryName")
        private String bankTransferBeneficiaryName;

        @JsonProperty("GatewayTrackId")
        private String gatewayTrackId;

        @JsonProperty("GatewayOrderId")
        private String gatewayOrderId;

        @JsonProperty("GatewayReferenceId")
        private String gatewayReferenceId;

        @JsonProperty("NetworkName")
        private String networkName;

        @JsonIgnore
        public ChariWebhookEventType getEventType() {
            return ChariWebhookEventType.fromValue(eventId);
        }

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(operationType);
        }

        @JsonIgnore
        public ChariOperationStatus getTypedOperationStatus() {
            return ChariOperationStatus.fromCode(operationStatus);
        }
    }
}
