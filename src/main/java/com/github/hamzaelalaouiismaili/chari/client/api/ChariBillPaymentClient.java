package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillUnpaidItemsPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillCreditorsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillFormResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillPaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceiptResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceivablesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillUnpaidItemsResponse;
import com.github.hamzaelalaouiismaili.chari.util.ChariBillFormValidator;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Fatourati bill discovery and payment workflow:
 *
 * <ol>
 *   <li>{@link #getCreditors()} — list creditors (each already embeds its receivables);</li>
 *   <li>{@link #getReceivables(String)} — refresh one creditor's receivables;</li>
 *   <li>{@link #getIdentificationForm(String, String)} — fetch the dynamic form;</li>
 *   <li>{@link #getUnpaidItems(String, String, ChariBillUnpaidItemsPayload, String)} —
 *       open a transaction and list unpaid articles (form values or QR code);</li>
 *   <li>{@link #confirmPayment(String, ChariBillPaymentPayload)} — pay selected articles;</li>
 *   <li>{@link #getBillReceipt(long, String)} — download the receipt of a paid operation.</li>
 * </ol>
 *
 * <p>Every method validates its inputs locally and fails fast with a precise
 * {@link IllegalArgumentException} before any network call.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariBillPaymentClient {

    private final ChariHttpClient httpClient;

    /** Step 1: lists active creditors, grouped by category. */
    public ChariBillCreditorsResponse getCreditors() {
        return httpClient.get("/api/bills/creanciers",
                ChariBillCreditorsResponse.class, "GET_BILL_CREDITORS");
    }

    /** Step 2: lists active receivables (services) for one creditor. */
    public ChariBillReceivablesResponse getReceivables(String creditorId) {
        validateCreditorId(creditorId);
        String path = UriComponentsBuilder.fromPath("/api/bills/creances")
                .queryParam("creancierId", creditorId)
                .toUriString();
        return httpClient.get(path, ChariBillReceivablesResponse.class, "GET_BILL_RECEIVABLES");
    }

    /** Step 3: retrieves the dynamic customer-identification form. */
    public ChariBillFormResponse getIdentificationForm(
            String creditorId, String receivableId) {
        validateServiceIds(creditorId, receivableId);
        String path = servicePath("/api/bills/form", creditorId, receivableId);
        return httpClient.get(path, ChariBillFormResponse.class, "GET_BILL_IDENTIFICATION_FORM");
    }

    /** Step 4: opens a transaction and retrieves unpaid items. */
    public ChariBillUnpaidItemsResponse getUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillUnpaidItemsPayload payload) {
        return getUnpaidItems(creditorId, receivableId, payload, null);
    }

    /**
     * Step 4 with the customer's phone number attached, required when saving
     * the bill to favorites ({@code alias}/{@code addToFavorites}).
     */
    public ChariBillUnpaidItemsResponse getUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillUnpaidItemsPayload payload,
            String phoneNumber) {
        validateServiceIds(creditorId, receivableId);
        validateUnpaidItemsPayload(payload);
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/bills/impayes")
                .queryParam("creancierId", creditorId)
                .queryParam("creanceId", receivableId);
        if (phoneNumber != null) {
            builder.queryParam("phoneNumber", requireValidPhone(phoneNumber));
        }
        return httpClient.post(builder.toUriString(), payload,
                ChariBillUnpaidItemsResponse.class, "GET_BILL_UNPAID_ITEMS");
    }

    /**
     * Step 4 with a local pre-check of the submitted values against the
     * dynamic identification form (required fields, lengths, select values),
     * failing fast before any network call.
     */
    public ChariBillUnpaidItemsResponse getUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillFormResponse form,
            ChariBillUnpaidItemsPayload payload,
            String phoneNumber) {
        if (payload != null && !payload.isQrCodeLookup()) {
            ChariBillFormValidator.validate(form, payload.getCreditorValues());
        }
        return getUnpaidItems(creditorId, receivableId, payload, phoneNumber);
    }

    /** Step 5: pays the selected articles for a single creditor. */
    public ChariBillPaymentResponse confirmPayment(
            String phoneNumber, ChariBillPaymentPayload payload) {
        String normalizedPhone = requireValidPhone(phoneNumber);
        validatePayment(payload);
        String path = UriComponentsBuilder.fromPath("/api/bills/confirm")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();
        log.info("Confirming Fatourati payment {} for customer {}",
                payload.getRefTxFatourati(), PhoneNumberUtil.mask(normalizedPhone));
        return httpClient.post(path, payload, ChariBillPaymentResponse.class, "CONFIRM_BILL_PAYMENT");
    }

    /** Step 6: downloads the receipt of a settled bill payment operation. */
    public ChariBillReceiptResponse getBillReceipt(long operationId, String phoneNumber) {
        if (operationId <= 0) {
            throw new IllegalArgumentException("Bill operation ID must be a positive number");
        }
        String path = UriComponentsBuilder.fromPath("/api/bills/bill-receipt/" + operationId)
                .queryParam("phoneNumber", requireValidPhone(phoneNumber))
                .toUriString();
        return httpClient.get(path, ChariBillReceiptResponse.class, "GET_BILL_RECEIPT");
    }

    private String servicePath(String path, String creditorId, String receivableId) {
        return UriComponentsBuilder.fromPath(path)
                .queryParam("creancierId", creditorId)
                .queryParam("creanceId", receivableId)
                .toUriString();
    }

    private String requireValidPhone(String phoneNumber) {
        if (!PhoneNumberUtil.isValidMoroccanNumber(phoneNumber)) {
            throw new IllegalArgumentException("A valid Chari Money Moroccan phone number is required");
        }
        return PhoneNumberUtil.normalize(phoneNumber);
    }

    private void validateUnpaidItemsPayload(ChariBillUnpaidItemsPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Bill identification payload is required");
        }
        if (payload.isQrCodeLookup()) {
            return;
        }
        if (payload.getCreditorValues() == null || payload.getCreditorValues().isEmpty()) {
            throw new IllegalArgumentException(
                    "Either identification values (CreancierVals) or a QR code content is required");
        }
        validateFieldValues(payload.getCreditorValues());
        if (Boolean.TRUE.equals(payload.getAddToFavorites())
                && (payload.getAlias() == null || payload.getAlias().isBlank())) {
            throw new IllegalArgumentException("An alias is required when saving a bill to favorites");
        }
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
        validateFieldValues(payload.getGlobalParams());
        validateSelectedArticles(payload.getSelectedArticles());
    }

    private void validateFieldValues(List<ChariBillFieldValue> values) {
        if (values == null) {
            throw new IllegalArgumentException("Identification field values are required");
        }
        for (ChariBillFieldValue value : values) {
            if (value == null || value.getNomChamp() == null || value.getNomChamp().isBlank()
                    || value.getValeurChamp() == null || value.getValeurChamp().isBlank()) {
                throw new IllegalArgumentException("Each field value requires nomChamp and valeurChamp");
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
