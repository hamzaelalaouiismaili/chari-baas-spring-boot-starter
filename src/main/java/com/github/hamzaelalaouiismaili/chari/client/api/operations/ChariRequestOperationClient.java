package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashinByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashoutByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariExecuteRequestOperationByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariFatouratiCashinRequestPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashinByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashoutByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRequestOperationsResponse;
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

        public ChariCashinByReferenceResponse requestFatouratiCashin(ChariFatouratiCashinRequestPayload payload) {
                if (payload == null || payload.getCode() == null || payload.getCode().isBlank()) {
                        throw new IllegalArgumentException("Fatourati cash-in code is required");
                }
                if (payload.getAmount() == null || payload.getAmount().signum() <= 0) {
                        throw new IllegalArgumentException("Amount must be positive");
                }
                log.debug("Requesting Fatourati cash-in for code: {}, amount: {}",
                                payload.getCode(), payload.getAmount());
                return httpClient.post("/api/operations/fatourati/cashin/request",
                                buildFatouratiPayload(payload),
                                ChariCashinByReferenceResponse.class, "FATOURATI_CASHIN_REQUEST");
        }

        /**
         * Lists the cash-in / cash-out request operations of a customer.
         * GET /api/operations/requests
         *
         * @param phoneNumber customer phone number (required)
         * @param pageSize    page size, optional
         * @param pageNumber  page number, optional
         */
        public ChariRequestOperationsResponse getRequestOperations(String phoneNumber, Integer pageSize,
                        Integer pageNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Listing request operations for customer: {}", PhoneNumberUtil.mask(normalizedPhone));

                UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/operations/requests")
                                .queryParam("phoneNumber", normalizedPhone);
                if (pageSize != null) {
                        builder.queryParam("pageSize", pageSize);
                }
                if (pageNumber != null) {
                        builder.queryParam("pageNumber", pageNumber);
                }

                return httpClient.get(builder.toUriString(), ChariRequestOperationsResponse.class,
                                "GET_REQUEST_OPERATIONS");
        }

        private Map<String, Object> buildRequestPayload(String phoneNumber, Object amount) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("PhoneNumber", PhoneNumberUtil.normalize(phoneNumber));
                requestPayload.put("amount", amount);
                return requestPayload;
        }

        private Map<String, Object> buildFatouratiPayload(ChariFatouratiCashinRequestPayload payload) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("code", payload.getCode());
                requestPayload.put("Amount", payload.getAmount());
                if (payload.getFeesPercent() != null) {
                        requestPayload.put("FeesPercent", payload.getFeesPercent());
                }
                if (payload.getDescription() != null) {
                        requestPayload.put("Description", payload.getDescription());
                }
                return requestPayload;
        }
}
