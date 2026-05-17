package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariSens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for a single operation lookup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariOperationResponse {

    private OperationData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationData {

        private Long operationId;

        private Long transactionId;

        private String transactionReference;

        private BigDecimal amount;

        private String reason;

        private Integer operationType;

        private String transactionDate;

        private Integer sens;

        private Integer transactionStatus;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private Long transactionFeesId;

        private String sender;

        private String receiver;

        private JsonNode beneficiary;

        private JsonNode voucherDetails;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(operationType);
        }

        @JsonIgnore
        public ChariOperationStatus getTypedTransactionStatus() {
            return ChariOperationStatus.fromCode(transactionStatus);
        }

        @JsonIgnore
        public ChariSens getTypedSens() {
            return ChariSens.fromCode(sens);
        }
    }
}
