package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariSens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Paginated operations response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariOperationsResponse {

    private OperationsData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationsData {

        private List<OperationItem> collection;

        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationItem {

        private Long operationId;

        private Long transactionId;

        private BigDecimal amount;

        private String reason;

        private String transactionDate;

        private Integer operationType;

        private String accountNumber;

        private String method;

        private String beneficiaryName;

        private Integer transactionStatus;

        private Integer sens;

        private Boolean openLoop;

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
