package com.github.hamzaelalaouiismaili.chari.model.payload;

import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariDocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Multipart payload for merchant KYC document upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariMerchantKycUploadPayload {

    private String phoneNumber;
    private List<KycDocument> kycDocuments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycDocument {
        private Integer docType;
        private Resource docFront;
        private Resource docBack;

        public static class KycDocumentBuilder {

            public KycDocumentBuilder docType(Integer docType) {
                this.docType = docType;
                return this;
            }

            public KycDocumentBuilder docType(ChariDocumentType docType) {
                this.docType = docType == null ? null : docType.getCode();
                return this;
            }
        }
    }
}
