package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for bank transfer preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariBankTransferPreviewResponse {

    private BankTransferPreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BankTransferPreviewData {

        private Integer type;

        private BankTransferPreviewOperation operation;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private String checkedAt;

        private Boolean openLoop;

        private BigDecimal amount;

        private BigDecimal fees;

        private String customerPhoneNumber;

        private String beneficiaryRib;

        private String beneficiaryName;

        private String reason;

        private String estimatedDeliveryDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BankTransferPreviewOperation {

        private String customerPhoneNumber;

        private String agentCode;

        private BigDecimal amount;

        private String reason;

        private String rib;

        private String beneficiaryName;

        private Integer beneficiaryId;
    }
}
