package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRetailAgentPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentCreatedResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentsResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Retail agent APIs.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariRetailAgentClient {

    private final ChariHttpClient httpClient;

    public ChariRetailAgentsResponse getRetailAgents(String code, Integer pageSize, Integer pageNumber) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/agents/retail")
                .queryParamIfPresent("code", Optional.ofNullable(code))
                .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))
                .queryParamIfPresent("pageNumber", Optional.ofNullable(pageNumber));
        return httpClient.get(builder.toUriString(), ChariRetailAgentsResponse.class, "GET_RETAIL_AGENTS");
    }

    public ChariRetailAgentResponse getRetailAgentByCode(String code) {
        String url = UriComponentsBuilder.fromPath("/api/agents/retail/{code}")
                .buildAndExpand(code)
                .toUriString();
        return httpClient.get(url, ChariRetailAgentResponse.class, "GET_RETAIL_AGENT");
    }

    public ChariRetailAgentCreatedResponse addRetailAgent(ChariRetailAgentPayload payload) {
        log.info("Adding retail agent for phone: {}", PhoneNumberUtil.mask(payload.getPhoneNumber()));
        return httpClient.post("/api/agents/retail", buildPayload(payload),
                ChariRetailAgentCreatedResponse.class, "ADD_RETAIL_AGENT");
    }

    private Map<String, Object> buildPayload(ChariRetailAgentPayload payload) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("phoneNumber", PhoneNumberUtil.normalize(payload.getPhoneNumber()));
        requestPayload.put("name", payload.getName());
        requestPayload.put("firstName", payload.getFirstName());
        requestPayload.put("lastName", payload.getLastName());
        requestPayload.put("cin", payload.getCin());
        requestPayload.put("email", payload.getEmail());
        requestPayload.put("address", payload.getAddress());
        return requestPayload;
    }
}
