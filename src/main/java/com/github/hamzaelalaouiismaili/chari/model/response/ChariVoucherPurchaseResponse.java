package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Confirmed voucher purchase including the redeemable voucher code. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherPurchaseResponse {

    private VoucherPurchaseData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherPurchaseData {

        private Integer operationType;
        private String voucherName;
        private BigDecimal amount;
        private BigDecimal cashBack;
        private BigDecimal totalAmount;
        private String reason;
        private String recipientPhoneNumber;
        private String checkedAt;
        private String urlActivateCard;
        private String destinationPhoneNumber;
        private String beneficiaryName;
        private String code;
        private String description;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(operationType);
        }
    }
}
