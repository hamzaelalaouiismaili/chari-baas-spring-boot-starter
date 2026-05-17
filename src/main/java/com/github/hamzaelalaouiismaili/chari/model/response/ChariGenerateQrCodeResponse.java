package com.github.hamzaelalaouiismaili.chari.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response for QR code generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariGenerateQrCodeResponse {

    private QrCodeData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QrCodeData {

        private String qrContent;

        private String qrCodeReference;

        private String qrCodeContent;

        private String qrCodeImage; // Base64 encoded image

        private BigDecimal amount;

        private String expiresAt;

        public String getQrCodeContent() {
            return qrCodeContent != null ? qrCodeContent : qrContent;
        }
    }
}
