package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Voucher purchase feasibility and fee preview. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherPreviewResponse {

    private VoucherPreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherPreviewData {

        private Integer type;
        private VoucherPreviewOperation operation;
        private BigDecimal feesAmount;
        private BigDecimal totalAmount;
        private String checkedAt;
        private Boolean openLoop;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(type);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherPreviewOperation {

        private String customerPhoneNumber;
        private BigDecimal amount;
        private String reason;
        private Integer beneficiaryId;
        private String recipientPhoneNumber;
    }
}
