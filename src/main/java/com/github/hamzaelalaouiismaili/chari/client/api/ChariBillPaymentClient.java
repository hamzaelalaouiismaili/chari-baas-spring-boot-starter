package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillUnpaidItemsPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillCreditorsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillFormResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillPaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceivablesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillUnpaidItemsResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/** Five-step Fatourati bill discovery and payment workflow. */
@Slf4j
@RequiredArgsConstructor
public class ChariBillPaymentClient {

    private final ChariHttpClient httpClient;

    public ChariBillCreditorsResponse getCreditors() {
        return httpClient.get("/api/bills/creanciers",
                ChariBillCreditorsResponse.class, "GET_BILL_CREDITORS");
    }

    public ChariBillReceivablesResponse getReceivables(String creditorId) {
        validateCreditorId(creditorId);
        String path = UriComponentsBuilder.fromPath("/api/bills/creances")
                .queryParam("creancierId", creditorId)
                .toUriString();
        return httpClient.get(path, ChariBillReceivablesResponse.class, "GET_BILL_RECEIVABLES");
    }

    public ChariBillFormResponse getIdentificationForm(
            String creditorId, String receivableId) {
        validateServiceIds(creditorId, receivableId);
        String path = servicePath("/api/bills/form", creditorId, receivableId);
        return httpClient.get(path, ChariBillFormResponse.class, "GET_BILL_IDENTIFICATION_FORM");
    }

    public ChariBillUnpaidItemsResponse getUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillUnpaidItemsPayload payload) {
        validateServiceIds(creditorId, receivableId);
        if (payload == null) {
            throw new IllegalArgumentException("Bill identification values are required");
        }
        validateFieldValues(payload.getCreditorValues());
        String path = servicePath("/api/bills/impayes", creditorId, receivableId);
        return httpClient.post(path, payload, ChariBillUnpaidItemsResponse.class, "GET_BILL_UNPAID_ITEMS");
    }

    public ChariBillPaymentResponse confirmPayment(
            String phoneNumber, ChariBillPaymentPayload payload) {
        if (!PhoneNumberUtil.isValidMoroccanNumber(phoneNumber)) {
            throw new IllegalArgumentException("A valid Chari Money Moroccan phone number is required");
        }
        validatePayment(payload);
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        String path = UriComponentsBuilder.fromPath("/api/bills/confirm")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        log.info("Confirming Fatourati payment {} for customer {}",
                payload.getRefTxFatourati(), PhoneNumberUtil.mask(normalizedPhone));
        return httpClient.post(path, payload, ChariBillPaymentResponse.class, "CONFIRM_BILL_PAYMENT");
    }

    private String servicePath(String path, String creditorId, String receivableId) {
        return UriComponentsBuilder.fromPath(path)
                .queryParam("creancierId", creditorId)
                .queryParam("creanceId", receivableId)
                .toUriString();
    }

    private void validatePayment(ChariBillPaymentPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Bill payment payload is required");
        }
        validateServiceIds(payload.getCreancierId(), payload.getCreanceId());
        if (payload.getRefTxFatourati() == null
                || !payload.getRefTxFatourati().matches("\\d{12}")) {
            throw new IllegalArgumentException("Fatourati transaction reference must contain 12 digits");
        }
        validateFieldValues(payload.getCreditorValues());
        validateSelectedArticles(payload.getSelectedArticles());
    }

    private void validateFieldValues(List<ChariBillFieldValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("CreancierVals is required");
        }
        for (ChariBillFieldValue value : values) {
            if (value == null || value.getNomChamp() == null || value.getNomChamp().isBlank()
                    || value.getValeurChamp() == null || value.getValeurChamp().isBlank()) {
                throw new IllegalArgumentException("Each creditor value requires nomChamp and valeurChamp");
            }
        }
    }

    private void validateSelectedArticles(List<ChariBillArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            throw new IllegalArgumentException("At least one bill article must be selected");
        }
        for (ChariBillArticle article : articles) {
            if (article == null || article.getIdArticle() == null || article.getIdArticle().isBlank()
                    || article.getPrixTTC() == null || article.getPrixTTC().signum() < 0
                    || article.getTypeArticle() == null) {
                throw new IllegalArgumentException(
                        "Each selected article requires idArticle, prixTTC, and typeArticle");
            }
        }
    }

    private void validateServiceIds(String creditorId, String receivableId) {
        validateCreditorId(creditorId);
        if (receivableId == null || !receivableId.matches("\\d{2}")) {
            throw new IllegalArgumentException("Fatourati receivable ID must contain 2 digits");
        }
    }

    private void validateCreditorId(String creditorId) {
        if (creditorId == null || !creditorId.matches("\\d{4}")
                || Integer.parseInt(creditorId) < 1000) {
            throw new IllegalArgumentException("Fatourati creditor ID must be 4 digits and at least 1000");
        }
    }
}
