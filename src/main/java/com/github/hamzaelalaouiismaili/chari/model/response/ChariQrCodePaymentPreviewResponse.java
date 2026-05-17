package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for QR code payment preview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariQrCodePaymentPreviewResponse {

    private QrPaymentPreviewData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QrPaymentPreviewData {

        private Integer type;

        private BigDecimal amount;

        private BigDecimal fees;

        private BigDecimal feesAmount;

        private BigDecimal totalAmount;

        private String merchantName;

        private String merchantId;

        private String terminalId;

        private String checkedAt;

        private String qrReference;

        private Boolean openLoop;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(type);
        }
    }
}
