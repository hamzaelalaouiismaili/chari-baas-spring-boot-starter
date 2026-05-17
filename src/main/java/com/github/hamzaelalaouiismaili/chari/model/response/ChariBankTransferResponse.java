package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for executed bank transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariBankTransferResponse {

    private BankTransferData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BankTransferData {

        private Integer type;

        private Long operationId;

        @JsonProperty("Amount")
        private BigDecimal amount;

        private BigDecimal fees;

        private BigDecimal feesAmount;

        @JsonProperty("TotalAmount")
        private BigDecimal totalAmount;

        @JsonProperty("RecipientRib")
        private String recipientRib;

        private String beneficiaryRib;

        private String beneficiaryName;

        private String reason;

        private String checkedAt;

        private String executedAt;

        private String reference;

        private String status;

        private String estimatedDeliveryDate;
    }
}
