package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariSaveCardPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariListSavedCardsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariSavedCardResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Tokenized card lifecycle operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariTokenizedCardClient {

    private final ChariHttpClient httpClient;

    public ChariBooleanResponse saveCard(String phoneNumber, ChariSaveCardPayload payload) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.info("Saving card for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("FirstName", payload.getFirstName());
        requestPayload.put("LastName", payload.getLastName());
        requestPayload.put("Pan", payload.getPan());
        requestPayload.put("ExpiryDate", payload.getExpiryDate());
        requestPayload.put("Cvv", payload.getCvv());
        if (payload.getCardName() != null) {
            requestPayload.put("CardName", payload.getCardName());
        }

        String url = UriComponentsBuilder.fromPath("/api/customers/tokenized/cards")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        return httpClient.post(url, requestPayload, ChariBooleanResponse.class, "SAVE_CARD");
    }

    public ChariListSavedCardsResponse listSavedCards(String phoneNumber) {
        return listSavedCards(phoneNumber, null, null);
    }

    public ChariListSavedCardsResponse listSavedCards(String phoneNumber, Integer pageSize, Integer pageNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Listing saved cards for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/customers/tokenized/cards")
                .queryParam("phoneNumber", normalizedPhone)
                .queryParamIfPresent("pageSize", java.util.Optional.ofNullable(pageSize))
                .queryParamIfPresent("pageNumber", java.util.Optional.ofNullable(pageNumber));
        return httpClient.get(builder.toUriString(), ChariListSavedCardsResponse.class, "LIST_SAVED_CARDS");
    }

    public ChariSavedCardResponse getSavedCard(Integer cardId, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Getting saved card {} for customer: {}", cardId, PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customers/tokenized/cards/{cardId}")
                .queryParam("phoneNumber", normalizedPhone)
                .buildAndExpand(cardId)
                .toUriString();
        return httpClient.get(url, ChariSavedCardResponse.class, "GET_SAVED_CARD");
    }

    public ChariBooleanResponse deleteSavedCard(Integer cardId, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.info("Deleting saved card {} for customer: {}", cardId, PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customers/tokenized/cards/{cardId}")
                .queryParam("phoneNumber", normalizedPhone)
                .buildAndExpand(cardId)
                .toUriString();
        return httpClient.delete(url, ChariBooleanResponse.class, "DELETE_SAVED_CARD");
    }
}
