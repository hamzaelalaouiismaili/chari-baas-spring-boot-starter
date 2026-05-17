package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBeneficiaryPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBeneficiariesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBeneficiaryResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Beneficiary APIs.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariBeneficiaryClient {

    private final ChariHttpClient httpClient;

    public ChariBeneficiariesResponse getBeneficiaries(String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Listing beneficiaries for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customer/beneficiaries")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.get(url, ChariBeneficiariesResponse.class, "GET_BENEFICIARIES");
    }

    public ChariBeneficiaryResponse addBeneficiary(String phoneNumber, ChariBeneficiaryPayload payload) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Adding beneficiary for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customer/beneficiaries")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.post(url, buildBeneficiaryPayload(payload),
                ChariBeneficiaryResponse.class, "ADD_BENEFICIARY");
    }

    public ChariBooleanResponse deleteBeneficiary(Long beneficiaryId, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Deleting beneficiary {} for customer: {}", beneficiaryId, PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customer/beneficiaries/{beneficiaryId}")
                .queryParam("phoneNumber", normalizedPhone)
                .buildAndExpand(beneficiaryId)
                .toUriString();
        return httpClient.delete(url, ChariBooleanResponse.class, "DELETE_BENEFICIARY");
    }

    private Map<String, Object> buildBeneficiaryPayload(ChariBeneficiaryPayload payload) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("name", payload.getName());
        if (payload.getPhoneNumber() != null) {
            requestPayload.put("PhoneNumber", PhoneNumberUtil.normalize(payload.getPhoneNumber()));
        }
        if (payload.getRib() != null) {
            requestPayload.put("Rib", payload.getRib());
        }
        if (payload.getEmail() != null) {
            requestPayload.put("Email", payload.getEmail());
        }
        return requestPayload;
    }
}
