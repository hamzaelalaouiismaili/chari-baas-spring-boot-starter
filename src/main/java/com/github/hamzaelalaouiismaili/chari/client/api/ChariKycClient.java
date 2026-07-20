package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariAccountLevel;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariDocumentType;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantKycUploadPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantKycUploadPayload.KycDocument;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariShareIdAuthResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * KYC and account upgrade operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariKycClient {

    private final ChariHttpClient httpClient;

    public ChariShareIdAuthResponse authenticateShareId(String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Authenticating ShareID for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/kyc/shareid/auth")
                .queryParam("PhoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.get(url, ChariShareIdAuthResponse.class, "AUTHENTICATE_SHAREID");
    }

    public ChariBooleanResponse confirmKyc(String phoneNumber, Integer accountLevel) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Confirming KYC for phone: {}, account level: {}",
                PhoneNumberUtil.mask(normalizedPhone), accountLevel);

        String url = UriComponentsBuilder.fromPath("/api/customers/upgrade/request")
                .queryParam("PhoneNumber", normalizedPhone)
                .queryParam("AccountLevel", accountLevel)
                .toUriString();
        return httpClient.post(url, null, ChariBooleanResponse.class, "CONFIRM_KYC");
    }

    public ChariBooleanResponse confirmKyc(String phoneNumber, ChariAccountLevel accountLevel) {
        return confirmKyc(phoneNumber, accountLevel == null ? null : accountLevel.getCode());
    }

    public ChariBooleanResponse uploadMerchantKycDocuments(ChariMerchantKycUploadPayload payload) {
        return requestKyc(payload);
    }

    public ChariBooleanResponse uploadMerchantKycDocuments(String phoneNumber, List<KycDocument> kycDocuments) {
        return requestKyc(phoneNumber, kycDocuments);
    }

    public ChariBooleanResponse requestKyc(ChariMerchantKycUploadPayload payload) {
        return requestKyc(payload.getPhoneNumber(), payload.getKycDocuments());
    }

    public ChariBooleanResponse requestKyc(String phoneNumber, List<KycDocument> kycDocuments) {
        return requestMerchantVerification(
                phoneNumber,
                kycDocuments,
                "/api/customers/merchant/kyc/request",
                "REQUEST_KYC",
                "KYC");
    }

    public ChariBooleanResponse requestKyb(ChariMerchantKycUploadPayload payload) {
        return requestKyb(payload.getPhoneNumber(), payload.getKycDocuments());
    }

    public ChariBooleanResponse requestKyb(String phoneNumber, List<KycDocument> kycDocuments) {
        return requestMerchantVerification(
                phoneNumber,
                kycDocuments,
                "/api/customers/merchant/kyb/request",
                "REQUEST_KYB",
                "KYB");
    }

    private ChariBooleanResponse requestMerchantVerification(
            String phoneNumber,
            List<KycDocument> kycDocuments,
            String path,
            String operation,
            String requestType) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Submitting merchant {} documents for phone: {}",
                requestType, PhoneNumberUtil.mask(normalizedPhone));
        validateMerchantKycDocuments(kycDocuments);

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        for (int index = 0; index < kycDocuments.size(); index++) {
            KycDocument document = kycDocuments.get(index);
            String prefix = "KycDocuments[" + index + "].";
            multipartBody.add(prefix + "DocType", document.getDocType());
            addMultipartFile(multipartBody, prefix + "DocFront", document.getDocFront());
            addMultipartFile(multipartBody, prefix + "DocBack", document.getDocBack());
        }

        String url = UriComponentsBuilder.fromPath(path)
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.postMultipart(url, multipartBody, ChariBooleanResponse.class, operation);
    }

    private void validateMerchantKycDocuments(List<KycDocument> kycDocuments) {
        if (kycDocuments == null || kycDocuments.isEmpty()) {
            throw new IllegalArgumentException("At least one merchant KYC document is required");
        }
        for (int index = 0; index < kycDocuments.size(); index++) {
            KycDocument document = kycDocuments.get(index);
            if (document == null) {
                throw new IllegalArgumentException("KycDocuments[" + index + "] is required");
            }
            if (document.getDocType() == null) {
                throw new IllegalArgumentException("KycDocuments[" + index + "].DocType is required");
            }
            if (document.getDocFront() == null) {
                throw new IllegalArgumentException("KycDocuments[" + index + "].DocFront is required");
            }
            ChariDocumentType documentType = ChariDocumentType.fromCode(document.getDocType());
            if (documentType.requiresBackImage() && document.getDocBack() == null) {
                throw new IllegalArgumentException(
                        "KycDocuments[" + index + "].DocBack is required for " + documentType.name());
            }
        }
    }

    private void addMultipartFile(MultiValueMap<String, Object> body, String fieldName, Resource file) {
        if (file != null) {
            body.add(fieldName, file);
        }
    }
}
