package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBankTransferPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBankTransferPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBankTransferResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Bank transfer operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariBankTransferClient {

    private final ChariHttpClient httpClient;
    private final ChariBaasProperties properties;

    public ChariBankTransferPreviewResponse previewBankTransfer(ChariBankTransferPayload payload, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Previewing bank transfer from {} to RIB {}",
                PhoneNumberUtil.mask(normalizedPhone),
                maskRib(payload.getRib()));

        return httpClient.post("/api/operations/bank-transfer/preview", buildCustomerPayload(payload, normalizedPhone),
                ChariBankTransferPreviewResponse.class, "PREVIEW_BANK_TRANSFER");
    }

    public ChariBankTransferResponse executeBankTransfer(ChariBankTransferPayload payload, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.info("Executing bank transfer from {} to RIB {}, amount: {}",
                PhoneNumberUtil.mask(normalizedPhone),
                maskRib(payload.getRib()),
                payload.getAmount());

        return httpClient.post("/api/operations/bank-transfer", buildCustomerPayload(payload, normalizedPhone),
                ChariBankTransferResponse.class, "EXECUTE_BANK_TRANSFER");
    }

    public ChariBankTransferPreviewResponse previewBankTransferFromAP(ChariBankTransferPayload payload) {
        log.debug("Previewing bank transfer from AP to RIB {}", maskRib(payload.getRib()));
        return httpClient.post("/api/operations/bank-transfer/preview", buildPrincipalAgentPayload(payload),
                ChariBankTransferPreviewResponse.class, "PREVIEW_BANK_TRANSFER_FROM_AP");
    }

    public ChariBankTransferResponse executeBankTransferFromAP(ChariBankTransferPayload payload) {
        log.info("Executing bank transfer from AP to RIB {}, amount: {}",
                maskRib(payload.getRib()), payload.getAmount());
        return httpClient.post("/api/operations/bank-transfer", buildPrincipalAgentPayload(payload),
                ChariBankTransferResponse.class, "EXECUTE_BANK_TRANSFER_FROM_AP");
    }

    private Map<String, Object> buildCustomerPayload(ChariBankTransferPayload payload, String normalizedPhone) {
        Map<String, Object> normalizedPayload = basePayload(payload, payload.getRib());
        normalizedPayload.put("customerPhoneNumber", normalizedPhone);
        return normalizedPayload;
    }

    private Map<String, Object> buildPrincipalAgentPayload(ChariBankTransferPayload payload) {
        Map<String, Object> normalizedPayload = basePayload(payload,
                payload.getRib() != null ? payload.getRib() : properties.getPrincipalAgentRib());
        normalizedPayload.put("AgentCode", properties.getPrincipalAgentId());
        return normalizedPayload;
    }

    private Map<String, Object> basePayload(ChariBankTransferPayload payload, String rib) {
        Map<String, Object> normalizedPayload = new HashMap<>();
        normalizedPayload.put("amount", payload.getAmount());
        normalizedPayload.put("reason", payload.getReason());
        normalizedPayload.put("rib", rib);
        String beneficiaryName = payload.getBeneficiaryName() != null ? payload.getBeneficiaryName() : "";
        normalizedPayload.put("BeneficiaryName", beneficiaryName);
        normalizedPayload.put("ForceCustomerName",
                payload.getForceCustomerName() != null ? payload.getForceCustomerName() : false);
        normalizedPayload.put("OriginatorName",
                payload.getOriginatorName() != null ? payload.getOriginatorName() : beneficiaryName);
        if (payload.getBeneficiaryId() != null) {
            normalizedPayload.put("beneficiaryId", payload.getBeneficiaryId());
        }
        return normalizedPayload;
    }

    private String maskRib(String rib) {
        if (rib == null || rib.length() <= 4) {
            return "****";
        }
        return "*".repeat(rib.length() - 4) + rib.substring(rib.length() - 4);
    }
}
