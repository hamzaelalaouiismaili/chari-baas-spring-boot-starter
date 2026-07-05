package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardAction;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardApplicationStatus;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardApplicationsQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardTransactionsQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardUsageControlPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariManagedCardsQuery;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardApplicationCreatedResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardApplicationResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardApplicationsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardProgramsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardTransactionsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariManagedCardResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariManagedCardsResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/** Card program, application, issued-card, control, and transaction APIs. */
@Slf4j
@RequiredArgsConstructor
public class ChariCardManagementClient {

    private final ChariHttpClient httpClient;

    public ChariCardProgramsResponse getPrograms(Integer pageSize, Integer pageNumber) {
        validatePagination(pageSize, pageNumber);
        String path = paginationPath("/api/cards/programs", pageSize, pageNumber).toUriString();
        return httpClient.get(path, ChariCardProgramsResponse.class, "GET_CARD_PROGRAMS");
    }

    public ChariCardApplicationCreatedResponse addApplication(String phoneNumber, Long cardProgramId) {
        validatePhone(phoneNumber);
        validatePositiveId(cardProgramId, "Card program ID");
        String path = UriComponentsBuilder.fromPath("/api/cards/applications")
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(phoneNumber))
                .queryParam("cardProgramId", cardProgramId)
                .toUriString();
        return httpClient.post(path, null, ChariCardApplicationCreatedResponse.class, "ADD_CARD_APPLICATION");
    }

    public ChariCardApplicationsResponse getApplications(ChariCardApplicationsQuery query) {
        if (query == null) {
            query = new ChariCardApplicationsQuery();
        }
        validatePagination(query.getPageSize(), query.getPageNumber());
        if (query.getStatus() == ChariCardApplicationStatus.UNKNOWN) {
            throw new IllegalArgumentException("Card application status must be a supported value");
        }
        UriComponentsBuilder builder = paginationPath(
                "/api/cards/applications", query.getPageSize(), query.getPageNumber());
        if (query.getStatus() != null) {
            builder.queryParam("status", query.getStatus().getCode());
        }
        return httpClient.get(builder.toUriString(), ChariCardApplicationsResponse.class,
                "GET_CARD_APPLICATIONS");
    }

    public ChariCardApplicationsResponse getApplicationsByCustomer(
            Integer pageSize, Integer pageNumber) {
        validatePagination(pageSize, pageNumber);
        String path = paginationPath(
                "/api/cards/applications/customer", pageSize, pageNumber).toUriString();
        return httpClient.get(path, ChariCardApplicationsResponse.class,
                "GET_CARD_APPLICATIONS_BY_CUSTOMER");
    }

    public ChariCardApplicationResponse updateApplication(
            Long applicationId, String phoneNumber, boolean validate) {
        validatePositiveId(applicationId, "Card application ID");
        validatePhone(phoneNumber);
        String action = validate ? "validate" : "reject";
        String path = UriComponentsBuilder.fromPath("/api/cards/applications/{id}/{action}")
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(phoneNumber))
                .buildAndExpand(applicationId, action)
                .toUriString();
        return httpClient.put(path, null, ChariCardApplicationResponse.class,
                validate ? "VALIDATE_CARD_APPLICATION" : "REJECT_CARD_APPLICATION");
    }

    public ChariManagedCardsResponse getCards(ChariManagedCardsQuery query) {
        if (query == null) {
            query = new ChariManagedCardsQuery();
        }
        validatePagination(query.getPageSize(), query.getPageNumber());
        UriComponentsBuilder builder = paginationPath("/api/cards", query.getPageSize(), query.getPageNumber());
        if (query.getPhoneNumber() != null && !query.getPhoneNumber().isBlank()) {
            validatePhone(query.getPhoneNumber());
            builder.queryParam("phoneNumber", PhoneNumberUtil.normalize(query.getPhoneNumber()));
        }
        if (query.getCardProgramId() != null) {
            validatePositiveId(query.getCardProgramId(), "Card program ID");
            builder.queryParam("cardProgramId", query.getCardProgramId());
        }
        if (query.getDeliveryStatus() != null) {
            if (query.getDeliveryStatus().getCode() < 0) {
                throw new IllegalArgumentException("Card delivery status must be a supported value");
            }
            builder.queryParam("deliveryStatusId", query.getDeliveryStatus().getCode());
        }
        if (query.getCardStatus() != null) {
            if (query.getCardStatus().getCode() < 0) {
                throw new IllegalArgumentException("Card status must be a supported value");
            }
            builder.queryParam("cardStatusId", query.getCardStatus().getCode());
        }
        return httpClient.get(builder.toUriString(), ChariManagedCardsResponse.class, "GET_MANAGED_CARDS");
    }

    public ChariManagedCardResponse getCard(Long cardId, String phoneNumber) {
        validatePositiveId(cardId, "Card ID");
        validatePhone(phoneNumber);
        String path = cardPath(cardId, null, phoneNumber);
        return httpClient.get(path, ChariManagedCardResponse.class, "GET_MANAGED_CARD");
    }

    public ChariBooleanResponse runAction(
            Long cardId, String phoneNumber, ChariCardAction action) {
        validatePositiveId(cardId, "Card ID");
        validatePhone(phoneNumber);
        if (action == null) {
            throw new IllegalArgumentException("Card action is required");
        }
        String path = cardPath(cardId, action.getPath(), phoneNumber);
        log.info("Running card action {} on card {}", action, cardId);
        return httpClient.put(path, null, ChariBooleanResponse.class, "MANAGE_CARD_" + action.name());
    }

    public ChariBooleanResponse updateUsageControl(
            Long cardId, String phoneNumber, ChariCardUsageControlPayload payload) {
        validatePositiveId(cardId, "Card ID");
        validatePhone(phoneNumber);
        validateUsageControl(payload);
        String path = cardPath(cardId, "services", phoneNumber);
        return httpClient.put(path, payload, ChariBooleanResponse.class, "UPDATE_CARD_USAGE_CONTROL");
    }

    public ChariCardTransactionsResponse getTransactions(
            Long cardId, ChariCardTransactionsQuery query) {
        validatePositiveId(cardId, "Card ID");
        if (query == null) {
            query = new ChariCardTransactionsQuery();
        }
        validatePagination(query.getPageSize(), query.getPageNumber());
        UriComponentsBuilder builder = paginationPath(
                "/api/cards/{id}/transactions", query.getPageSize(), query.getPageNumber());
        if (query.getPhoneNumber() != null && !query.getPhoneNumber().isBlank()) {
            validatePhone(query.getPhoneNumber());
            builder.queryParam("phoneNumber", PhoneNumberUtil.normalize(query.getPhoneNumber()));
        }
        if (query.getFrom() != null && !query.getFrom().isBlank()) {
            builder.queryParam("from", query.getFrom());
        }
        if (query.getTo() != null && !query.getTo().isBlank()) {
            builder.queryParam("to", query.getTo());
        }
        String path = builder.buildAndExpand(cardId).toUriString();
        return httpClient.get(path, ChariCardTransactionsResponse.class, "GET_CARD_TRANSACTIONS");
    }

    private UriComponentsBuilder paginationPath(String path, Integer pageSize, Integer pageNumber) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (pageSize != null) {
            builder.queryParam("pageSize", pageSize);
        }
        if (pageNumber != null) {
            builder.queryParam("pageNumber", pageNumber);
        }
        return builder;
    }

    private String cardPath(Long cardId, String action, String phoneNumber) {
        String template = action == null ? "/api/cards/{id}" : "/api/cards/{id}/{action}";
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(template)
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(phoneNumber));
        return action == null
                ? builder.buildAndExpand(cardId).toUriString()
                : builder.buildAndExpand(cardId, action).toUriString();
    }

    private void validateUsageControl(ChariCardUsageControlPayload payload) {
        if (payload == null || payload.getAllowAtm() == null || payload.getAllowOnline() == null
                || payload.getAllowPos() == null || payload.getContactlessEnabled() == null) {
            throw new IllegalArgumentException("All card usage-control fields are required");
        }
    }

    private void validatePagination(Integer pageSize, Integer pageNumber) {
        if (pageSize != null && pageSize < 1) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (pageNumber != null && pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be at least 1");
        }
    }

    private void validatePositiveId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }

    private void validatePhone(String phoneNumber) {
        if (!PhoneNumberUtil.isValidMoroccanNumber(phoneNumber)) {
            throw new IllegalArgumentException("A valid Moroccan mobile phone number is required");
        }
    }
}
