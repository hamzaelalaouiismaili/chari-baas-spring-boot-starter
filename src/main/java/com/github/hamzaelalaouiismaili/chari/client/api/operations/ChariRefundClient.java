package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRefundPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRefundResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Refund operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariRefundClient {

    private final ChariHttpClient httpClient;

    public ChariRefundResponse previewRefund(ChariRefundPayload payload) {
        log.debug("Previewing refund for phone: {}, operationId: {}, amount: {}",
                PhoneNumberUtil.mask(payload.getPhoneNumber()),
                payload.getOperationId(), payload.getRefundAmount());
        return httpClient.post("/api/operations/refund/preview", buildRefundPayload(payload, "PhoneNumber"),
                ChariRefundResponse.class, "REFUND_PREVIEW");
    }

    public ChariRefundResponse executeRefund(ChariRefundPayload payload) {
        log.debug("Executing refund for phone: {}, operationId: {}, amount: {}",
                PhoneNumberUtil.mask(payload.getPhoneNumber()),
                payload.getOperationId(), payload.getRefundAmount());
        return httpClient.post("/api/operations/refund", buildRefundPayload(payload, "phoneNumber"),
                ChariRefundResponse.class, "REFUND_EXECUTE");
    }

    private Map<String, Object> buildRefundPayload(ChariRefundPayload payload, String phoneNumberFieldName) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put(phoneNumberFieldName, PhoneNumberUtil.normalize(payload.getPhoneNumber()));
        requestPayload.put("operationId", payload.getOperationId());
        requestPayload.put("refundAmount", payload.getRefundAmount());
        requestPayload.put("orderId", payload.getOrderId());
        requestPayload.put("transactionTrackId", payload.getTransactionTrackId());
        return requestPayload;
    }
}
