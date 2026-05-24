package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardCashinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariSavedCardCashinPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardFundingExecutionResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardFundingPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariSavedCardCashinResponse;
import com.github.hamzaelalaouiismaili.chari.util.NumericIdentifierUtil;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Cash-in card operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariCashInCardClient {

    private final ChariHttpClient httpClient;
    private final ChariBaasProperties properties;

    public ChariCardFundingPreviewResponse previewByPhone(String customerPhoneNumber, BigDecimal amount) {
        String normalizedPhone = PhoneNumberUtil.normalize(customerPhoneNumber);
        log.debug("Previewing card funding for phone: {}, amount: {}",
                PhoneNumberUtil.mask(normalizedPhone), amount);

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amount);

        String endpoint = UriComponentsBuilder.fromPath("/api/operations/cashin/card/preview")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.post(endpoint, payload,
                ChariCardFundingPreviewResponse.class, "PREVIEW_CARD_FUNDING");
    }

    public ChariCardFundingPreviewResponse previewByAgent(String code, BigDecimal amount) {
        String normalizedCode = NumericIdentifierUtil.normalize(code);
        log.debug("Previewing card funding for agent code: {}, amount: {}", code, amount);

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amount);

        String endpoint = UriComponentsBuilder.fromPath("/api/operations/cashin/card/agent/preview")
                .queryParam("code", normalizedCode)
                .toUriString();
        return httpClient.post(endpoint, payload,
                ChariCardFundingPreviewResponse.class, "PREVIEW_CARD_FUNDING_BY_AGENT");
    }

    public ChariCardFundingExecutionResponse executeByPhone(
            String customerPhoneNumber, ChariCardCashinPayload payload) {
        String normalizedPhone = PhoneNumberUtil.normalize(customerPhoneNumber);
        log.info("Executing card funding for phone: {}, amount: {}",
                PhoneNumberUtil.mask(normalizedPhone), payload.getAmount());

        String endpoint = UriComponentsBuilder.fromPath("/api/operations/cashin/card")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.post(endpoint, buildCardFundingExecutionPayload(payload),
                ChariCardFundingExecutionResponse.class, "EXECUTE_CARD_FUNDING");
    }

    public ChariCardFundingExecutionResponse executeByAgent(String code, ChariCardCashinPayload payload) {
        String normalizedCode = NumericIdentifierUtil.normalize(code);
        log.info("Executing card funding for agent code: {}, amount: {}", code, payload.getAmount());

        String endpoint = UriComponentsBuilder.fromPath("/api/operations/cashin/card/agent")
                .queryParam("code", normalizedCode)
                .toUriString();
        return httpClient.post(endpoint, buildCardFundingExecutionPayload(payload),
                ChariCardFundingExecutionResponse.class, "EXECUTE_CARD_FUNDING_BY_AGENT");
    }

    public ChariSavedCardCashinResponse executeWithSavedCard(
            Integer cardId, String phoneNumber, ChariSavedCardCashinPayload payload) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.info("Initiating cashin with saved card {} for customer: {}, amount: {}",
                cardId, PhoneNumberUtil.mask(normalizedPhone), payload.getAmount());

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("cvv", payload.getCvv());
        requestPayload.put("amount", payload.getAmount());
        addRedirectUrls(requestPayload, payload.getAcceptUrl(), payload.getDeclineUrl());

        String url = UriComponentsBuilder.fromPath("/api/operations/cashin/card/{cardId}")
                .queryParam("phoneNumber", normalizedPhone)
                .buildAndExpand(cardId)
                .toUriString();

        return httpClient.post(url, requestPayload, ChariSavedCardCashinResponse.class, "CASHIN_WITH_SAVED_CARD");
    }

    private Map<String, Object> buildCardFundingExecutionPayload(ChariCardCashinPayload payload) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("firstName", payload.getFirstName());
        requestPayload.put("lastName", payload.getLastName());
        requestPayload.put("pan", payload.getPan());
        requestPayload.put("expiryDate", formatExpiryDate(payload.getExpiryDate()));
        requestPayload.put("cvv", payload.getCvv());
        requestPayload.put("amount", payload.getAmount());
        requestPayload.put("currency", payload.getCurrency() != null ? payload.getCurrency() : "MAD");
        requestPayload.put("keepAlive", payload.getKeepAlive() != null ? payload.getKeepAlive() : false);
        addRedirectUrls(requestPayload, payload.getAcceptUrl(), payload.getDeclineUrl());
        if (payload.getCardName() != null) {
            requestPayload.put("cardName", payload.getCardName());
        }
        return requestPayload;
    }

    private void addRedirectUrls(Map<String, Object> requestPayload, String payloadAcceptUrl,
            String payloadDeclineUrl) {
        String acceptUrl = payloadAcceptUrl != null ? payloadAcceptUrl : properties.getCardFunding().getAcceptUrl();
        String declineUrl = payloadDeclineUrl != null ? payloadDeclineUrl : properties.getCardFunding().getDeclineUrl();
        if (acceptUrl != null) {
            requestPayload.put("acceptURL", acceptUrl);
        }
        if (declineUrl != null) {
            requestPayload.put("declineURL", declineUrl);
        }
    }

    private String formatExpiryDate(String expiryDate) {
        if (expiryDate == null) {
            return null;
        }
        if (expiryDate.contains("/")) {
            String[] parts = expiryDate.split("/");
            if (parts.length == 2) {
                return parts[1] + parts[0];
            }
        }
        return expiryDate;
    }
}
