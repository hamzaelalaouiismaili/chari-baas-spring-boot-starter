package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariOperationsByCustomerQuery;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariOperationResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariOperationsResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Operation lookup APIs.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariOperationsClient {

    private final ChariHttpClient httpClient;

    public ChariOperationsResponse getOperationsByCustomer(ChariOperationsByCustomerQuery query) {
        return getOperations(query, "/api/operations", "GET_OPERATIONS_BY_CUSTOMER");
    }

    public ChariOperationsResponse getAllOperationsByPartner(ChariOperationsByCustomerQuery query) {
        return getOperations(query, "/api/operations/all", "GET_ALL_OPERATIONS_BY_PARTNER");
    }

    private ChariOperationsResponse getOperations(ChariOperationsByCustomerQuery query, String path,
            String auditOperation) {
        String normalizedPhone = PhoneNumberUtil.normalize(query.getPhoneNumber());
        log.debug("Listing operations for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
                .queryParam("phoneNumber", normalizedPhone);
        if (query.getPageSize() != null) {
            builder.queryParam("pageSize", query.getPageSize());
        }
        if (query.getPageNumber() != null) {
            builder.queryParam("pageNumber", query.getPageNumber());
        }
        if (query.getOperationType() != null) {
            query.getOperationType().forEach(type -> builder.queryParam("operationType", type));
        }
        if (query.getTransactionStatus() != null) {
            builder.queryParam("transactionStatus", query.getTransactionStatus());
        }
        if (query.getSens() != null) {
            builder.queryParam("sens", query.getSens());
        }
        if (query.getFrom() != null) {
            builder.queryParam("from", query.getFrom());
        }
        if (query.getTo() != null) {
            builder.queryParam("to", query.getTo());
        }

        return httpClient.get(builder.toUriString(), ChariOperationsResponse.class, auditOperation);
    }

    public ChariOperationResponse getOperationById(Long id, String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Getting operation {} for customer: {}", id, PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/operations/{id}")
                .queryParam("phoneNumber", normalizedPhone)
                .buildAndExpand(id)
                .toUriString();
        return httpClient.get(url, ChariOperationResponse.class, "GET_OPERATION_BY_ID");
    }
}
