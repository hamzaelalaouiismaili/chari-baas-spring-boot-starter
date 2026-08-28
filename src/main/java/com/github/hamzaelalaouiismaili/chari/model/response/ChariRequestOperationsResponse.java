package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationType;
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

        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestOperationItem {

        private String reference;

        private String entity;

        private String createdAt;

        private String closedAt;

        private String executedAt;

        private Long accountId;

        private Integer partnerId;

        private String phoneNumber;

        private String code;

        private BigDecimal amount;

        private String description;

        private String partner;

        private Integer status;

        private Integer type;

        private Integer operationStatus;

        private Integer operationType;

        private String qrCode;

        private List<String> channels;

        @JsonIgnore
        public ChariRequestOperationStatus getTypedStatus() {
            return ChariRequestOperationStatus.fromCode(getEffectiveStatus());
        }

        @JsonIgnore
        public ChariRequestOperationType getTypedType() {
            return ChariRequestOperationType.fromCode(getEffectiveType());
        }

        @JsonIgnore
        public ChariRequestOperationStatus getTypedOperationStatus() {
            return getTypedStatus();
        }

        @JsonIgnore
        public ChariRequestOperationType getTypedOperationType() {
            return getTypedType();
        }

        @JsonIgnore
        private Integer getEffectiveStatus() {
            return operationStatus != null ? operationStatus : status;
        }

        @JsonIgnore
        private Integer getEffectiveType() {
            return operationType != null ? operationType : type;
        }
    }
}
