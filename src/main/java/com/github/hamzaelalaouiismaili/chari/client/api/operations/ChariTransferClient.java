package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTransferPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTransferPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTransferResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Wallet-to-wallet transfer operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariTransferClient {

        private final ChariHttpClient httpClient;

        public ChariTransferPreviewResponse previewTransfer(ChariTransferPayload payload) {
                log.debug("Previewing transfer from {} to {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()),
                                PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()));

                return httpClient.post("/api/operations/transfer/preview", normalize(payload),
                                ChariTransferPreviewResponse.class, "PREVIEW_TRANSFER");
        }

        public ChariTransferResponse executeTransfer(ChariTransferPayload payload) {
                log.info("Executing transfer from {} to {}, amount: {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()),
                                PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()),
                                payload.getAmount());

                return httpClient.post("/api/operations/transfer", normalize(payload),
                                ChariTransferResponse.class, "EXECUTE_TRANSFER");
        }

        private ChariTransferPayload normalize(ChariTransferPayload payload) {
                return ChariTransferPayload.builder()
                                .customerPhoneNumber(PhoneNumberUtil.normalize(payload.getCustomerPhoneNumber()))
                                .recipientPhoneNumber(PhoneNumberUtil.normalize(payload.getRecipientPhoneNumber()))
                                .amount(payload.getAmount())
                                .reason(payload.getReason())
                                .beneficiaryId(payload.getBeneficiaryId())
                                .idempotencyKey(payload.getIdempotencyKey())
                                .build();
        }
}
