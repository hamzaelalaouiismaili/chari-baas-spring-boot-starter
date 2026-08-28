package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariSens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Paginated cash-in / cash-out request operations response.
 * GET /api/operations/requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariRequestOperationsResponse {

    private RequestOperationsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestOperationsData {

        private List<RequestOperationItem> collection;

        /** Total number of request operations matching the filters, not the page size. */
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestOperationItem {

        private Long operationRequestId;

        private String createdAt;

        /** Set once the request has been closed (executed or canceled); null while still open. */
        private String closedAt;

        private String reference;

        private String phoneNumber;

        private String code;

        private Long accountId;

        private Integer operationType;

        private Integer operationStatus;

        private Integer partnerId;

        private BigDecimal amount;

        private String description;

        private Integer networkId;

        /** Executing network entity, e.g. {@code Partner(121)-Agent(11210550)}. */
        private String entity;

        private Long operationId;

        /** Raw JSON string supplied at request time, e.g. {@code {"externalReference":"..."}}. */
        private String customData;

        /** The executed operation; null while the request is still open. */
        private ExecutedOperation operation;

        /**
         * Cash-in or cash-out. Chari leaves the request-level field at 0 and only fills
         * the type on the executed operation, so this falls back to {@link #getOperation()}.
         */
        @JsonIgnore
        public ChariRequestOperationType getTypedOperationType() {
            return ChariRequestOperationType.fromCode(getEffectiveOperationType());
        }

        /**
         * Request status. Chari leaves the request-level field at 0 and only fills the
         * status on the executed operation, so this falls back to {@link #getOperation()}.
         */
        @JsonIgnore
        public ChariRequestOperationStatus getTypedOperationStatus() {
            return ChariRequestOperationStatus.fromCode(getEffectiveOperationStatus());
        }

        /** True when the request has not been executed yet. */
        @JsonIgnore
        public boolean isOpen() {
            return closedAt == null && operation == null;
        }

        @JsonIgnore
        private Integer getEffectiveOperationType() {
            if (operationType != null && operationType != 0) {
                return operationType;
            }
            return operation == null ? operationType : operation.getOperationType();
        }

        @JsonIgnore
        private Integer getEffectiveOperationStatus() {
            if (operationStatus != null && operationStatus != 0) {
                return operationStatus;
            }
            return operation == null ? operationStatus : operation.getOperationStatus();
        }
    }

    /**
     * Operation created when an agent executes the request.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutedOperation {

        private Long operationId;

        private Long transactionId;

        private Long accountId;

        private String primaryAccountNumber;

        private String secondaryAccountNumber;

        private BigDecimal amount;

        private BigDecimal feesAmount;

        private String operationDate;

        private Boolean openLoop;

        private Boolean nonExistentUser;

        private Integer operationType;

        private Integer operationStatus;

        private Integer transactionType;

        private Integer sens;

        private Integer partnerId;

        private String method;

        private String description;

        private String note;

        private List<JsonNode> transactions;

        private JsonNode images;

        private OperationRequestSummary operationRequest;

        @JsonIgnore
        public ChariRequestOperationType getTypedOperationType() {
            return ChariRequestOperationType.fromCode(operationType);
        }

        @JsonIgnore
        public ChariRequestOperationStatus getTypedOperationStatus() {
            return ChariRequestOperationStatus.fromCode(operationStatus);
        }

        @JsonIgnore
        public ChariSens getTypedSens() {
            return ChariSens.fromCode(sens);
        }
    }

    /**
     * Back-reference to the originating request carried inside the executed operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationRequestSummary {

        private Long operationRequestId;

        private String createdAt;

        private String closedAt;

        private String reference;

        private Integer networkId;

        private String entity;

        private String networkName;
    }
}
