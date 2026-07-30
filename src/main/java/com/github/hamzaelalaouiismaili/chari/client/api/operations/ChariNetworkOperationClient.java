package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariNetworkOperationPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariNetworkOperationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Network CashIn/CashOut by reference simulation (sandbox network agent step).
 */
@Slf4j
@RequiredArgsConstructor
public class ChariNetworkOperationClient {

    private final ChariHttpClient httpClient;

    public ChariNetworkOperationResponse simulateCashin(
            ChariNetworkOperationPayload payload, Boolean withContext) {
        validate(payload);
        log.info("Simulating network cash-in for reference: {}", payload.getReference());
        return httpClient.post(buildUrl("/api/network/operations/cashin", withContext),
                buildPayload(payload), ChariNetworkOperationResponse.class, "NETWORK_CASHIN");
    }

    public ChariNetworkOperationResponse simulateCashout(
            ChariNetworkOperationPayload payload, Boolean withContext) {
        validate(payload);
        log.info("Simulating network cash-out for reference: {}", payload.getReference());
        return httpClient.post(buildUrl("/api/network/operations/cashout", withContext),
                buildPayload(payload), ChariNetworkOperationResponse.class, "NETWORK_CASHOUT");
    }

    private void validate(ChariNetworkOperationPayload payload) {
        if (payload == null || payload.getReference() == null || payload.getReference().isBlank()) {
            throw new IllegalArgumentException("Network operation reference is required");
        }
    }

    private String buildUrl(String path, Boolean withContext) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        if (withContext != null) {
            builder.queryParam("withContext", withContext);
        }
        return builder.toUriString();
    }

    private Map<String, Object> buildPayload(ChariNetworkOperationPayload payload) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("reference", payload.getReference());
        if (payload.getEntity() != null) {
            requestPayload.put("entity", payload.getEntity());
        }
        return requestPayload;
    }
}
