package com.github.hamzaelalaouiismaili.chari.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Example payload from Chari:
 * 
 * <pre>
 * {
 *   "data": {
 *     "WebhookId": 12345,
 *     "CRequestId": "a4d1e0b5-9f6a-4c1d-bc7b-2d0a7f4b9b12",
 *     "OperationId": 924381,
 *     "OperationType": 5,
 *     "OperationStatus": 2,
 *     "CreatedAt": "2025-11-05T10:12:00Z",
 *     "ExecutedAt": "2025-11-05T10:12:22Z",
 *     "Amount": 25000.00,
 *     "FeeAmount": 350.00,
 *     "CustomData": "{\"note\":\"Salary for October\"}",
 *     "PrimaryAccountNumber": "+212711111111",
 *     "SecondaryAccountNumber": "+212722222222",
 *     "Method": "BankTransfer",
 *     "Reference": "BANK-REF-9FJ2X7",
 *     "BankTransferBeneficiaryName": "Aminata Diop"
 *   }
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

        @JsonProperty("EventId")
        private String eventId;

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
        private Instant createdAt;

        @JsonProperty("ExecutedAt")
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
