package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariChargebackPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariChargebackPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Chargeback operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariChargebackClient {

    private final ChariHttpClient httpClient;

    public ChariChargebackPreviewResponse previewChargeback(ChariChargebackPayload payload) {
        log.debug("Previewing chargeback from {} to {}, original operation: {}",
                PhoneNumberUtil.mask(payload.getSourcePhoneNumber()),
                PhoneNumberUtil.mask(payload.getDestinationPhoneNumber()),
                payload.getOriginalOperationId());

        return httpClient.post("/api/operations/chargeback", normalize(payload),
                ChariChargebackPreviewResponse.class, "PREVIEW_CHARGEBACK");
    }

    private Map<String, Object> normalize(ChariChargebackPayload payload) {
        Map<String, Object> normalizedPayload = new HashMap<>();
        normalizedPayload.put("sourcePhoneNumber", PhoneNumberUtil.normalize(payload.getSourcePhoneNumber()));
        normalizedPayload.put("destinationPhoneNumber", PhoneNumberUtil.normalize(payload.getDestinationPhoneNumber()));
        normalizedPayload.put("amount", payload.getAmount());
        normalizedPayload.put("description", payload.getDescription());
        normalizedPayload.put("originalOperationId", payload.getOriginalOperationId());
        return normalizedPayload;
    }
}
