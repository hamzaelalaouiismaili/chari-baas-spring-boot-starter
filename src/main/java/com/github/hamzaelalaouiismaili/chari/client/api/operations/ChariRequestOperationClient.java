package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashinByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashoutByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariExecuteRequestOperationByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashinByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashoutByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Cash-in and cash-out request operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariRequestOperationClient {

        private final ChariHttpClient httpClient;

        public ChariCashinByReferenceResponse requestCashinByReference(ChariCashinByReferencePayload payload) {
                log.debug("Requesting cash-in by reference for phone: {}, amount: {}",
                                PhoneNumberUtil.mask(payload.getPhoneNumber()), payload.getAmount());
                return httpClient.post("/api/operations/cashin/request",
                                buildRequestPayload(payload.getPhoneNumber(), payload.getAmount()),
                                ChariCashinByReferenceResponse.class, "CASHIN_BY_REFERENCE");
        }

        public ChariCashoutByReferenceResponse requestCashoutByReference(ChariCashoutByReferencePayload payload) {
                log.debug("Requesting cash-out by reference for phone: {}, amount: {}",
                                PhoneNumberUtil.mask(payload.getPhoneNumber()), payload.getAmount());
                return httpClient.post("/api/operations/cashout/request",
                                buildRequestPayload(payload.getPhoneNumber(), payload.getAmount()),
                                ChariCashoutByReferenceResponse.class, "CASHOUT_BY_REFERENCE");
        }

        public ChariCashinByReferenceResponse getCashinByReference(String reference) {
                String url = UriComponentsBuilder.fromPath("/api/operations/cashin/request")
                                .queryParam("reference", reference)
                                .toUriString();
                return httpClient.get(url, ChariCashinByReferenceResponse.class, "GET_CASHIN_BY_REFERENCE");
        }

        public ChariCashinByReferenceResponse executeCashinByReference(
                        ChariExecuteRequestOperationByReferencePayload payload) {
                return httpClient.post("/api/operations/cashin/agent", payload,
                                ChariCashinByReferenceResponse.class, "EXECUTE_CASHIN_BY_REFERENCE");
        }

        public ChariCashoutByReferenceResponse getCashoutByReference(String reference) {
                String url = UriComponentsBuilder.fromPath("/api/operations/cashout/request")
                                .queryParam("reference", reference)
                                .toUriString();
                return httpClient.get(url, ChariCashoutByReferenceResponse.class, "GET_CASHOUT_BY_REFERENCE");
        }

        public ChariCashoutByReferenceResponse executeCashoutByReference(
                        ChariExecuteRequestOperationByReferencePayload payload) {
                return httpClient.post("/api/operations/cashout/agent", payload,
                                ChariCashoutByReferenceResponse.class, "EXECUTE_CASHOUT_BY_REFERENCE");
        }

        private Map<String, Object> buildRequestPayload(String phoneNumber, Object amount) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("PhoneNumber", PhoneNumberUtil.normalize(phoneNumber));
                requestPayload.put("amount", amount);
                return requestPayload;
        }
}
