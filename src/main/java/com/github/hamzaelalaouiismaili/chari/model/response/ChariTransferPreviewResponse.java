package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for transfer preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariTransferPreviewResponse {

    private TransferPreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransferPreviewData {

        private Integer type;

        private TransferOperation operation;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private String checkedAt;

        private Boolean openLoop;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransferOperation {

        private String customerPhoneNumber;

        private BigDecimal amount;

        private String reason;

        private String recipientPhoneNumber;

        private String beneficiaryId;
    }
}
