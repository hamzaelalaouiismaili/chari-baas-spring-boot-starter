package com.github.hamzaelalaouiismaili.chari.client.api.operations;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariGenerateQrCodePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantCardPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantPaymentByPhonePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantTokenizedCardPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariQrCodePaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariGenerateQrCodeResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantPaymentByPhonePreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantPaymentByPhoneResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariQrCodePaymentPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariQrCodePaymentResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Merchant payment and QR generation operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariMerchantPaymentClient {

        private final ChariHttpClient httpClient;

        public ChariMerchantPaymentByPhonePreviewResponse previewByPhone(ChariMerchantPaymentByPhonePayload payload) {
                log.debug("Previewing merchant payment by phone from {} to {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()),
                                PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()));

                return httpClient.post("/api/operations/merchant/payment/push/manual/preview",
                                buildByPhonePayload(payload),
                                ChariMerchantPaymentByPhonePreviewResponse.class, "PREVIEW_MERCHANT_PAYMENT_BY_PHONE");
        }

        public ChariMerchantPaymentByPhoneResponse executeByPhone(ChariMerchantPaymentByPhonePayload payload) {
                log.info("Executing merchant payment by phone from {} to {}, amount: {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()),
                                PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()), payload.getAmount());

                return httpClient.post("/api/operations/merchant/payment/push/manual",
                                buildByPhonePayload(payload),
                                ChariMerchantPaymentByPhoneResponse.class, "EXECUTE_MERCHANT_PAYMENT_BY_PHONE");
        }

        public ChariQrCodePaymentPreviewResponse previewQrCodePayment(ChariQrCodePaymentPayload payload) {
                log.debug("Previewing QR code payment for phone: {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()));

                return httpClient.post("/api/operations/merchant/payment/push/qrcode/preview",
                                buildQrPaymentPayload(payload),
                                ChariQrCodePaymentPreviewResponse.class, "PREVIEW_QR_PAYMENT");
        }

        public ChariQrCodePaymentResponse executeQrCodePayment(ChariQrCodePaymentPayload payload) {
                log.info("Executing QR code payment for phone: {}",
                                PhoneNumberUtil.mask(payload.getCustomerPhoneNumber()));

                return httpClient.post("/api/operations/merchant/payment/push/qrcode",
                                buildQrPaymentPayload(payload),
                                ChariQrCodePaymentResponse.class, "EXECUTE_QR_PAYMENT");
        }

        public ChariMerchantCardPaymentPreviewResponse previewCardPayment(String phoneNumber, BigDecimal amount) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Previewing merchant card payment for phone: {}, amount: {}",
                                PhoneNumberUtil.mask(normalizedPhone), amount);

                String url = UriComponentsBuilder.fromPath("/api/operations/merchant/payment/push/card/preview")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.post(url, amountPayload(amount),
                                ChariMerchantCardPaymentPreviewResponse.class, "PREVIEW_MERCHANT_CARD_PAYMENT");
        }

        public ChariMerchantCardPaymentResponse executeCardPayment(
                        String phoneNumber, ChariMerchantCardPaymentPayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.info("Executing merchant card payment for phone: {}, amount: {}",
                                PhoneNumberUtil.mask(normalizedPhone), payload.getAmount());

                String url = UriComponentsBuilder.fromPath("/api/operations/merchant/payment/card")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.post(url, buildCardPaymentPayload(payload),
                                ChariMerchantCardPaymentResponse.class, "EXECUTE_MERCHANT_CARD_PAYMENT");
        }

        public ChariMerchantCardPaymentResponse executeTokenizedCardPayment(
                        Integer cardId, String phoneNumber, ChariMerchantTokenizedCardPaymentPayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.info("Executing merchant tokenized card payment with card {} for phone: {}, amount: {}",
                                cardId, PhoneNumberUtil.mask(normalizedPhone), payload.getAmount());

                String url = UriComponentsBuilder.fromPath("/api/operations/merchant/payment/tokenized/card/{cardId}")
                                .queryParam("phoneNumber", normalizedPhone)
                                .buildAndExpand(cardId)
                                .toUriString();
                return httpClient.post(url, buildTokenizedCardPaymentPayload(payload),
                                ChariMerchantCardPaymentResponse.class, "EXECUTE_MERCHANT_TOKENIZED_CARD_PAYMENT");
        }

        public ChariGenerateQrCodeResponse generateQrCode(String phoneNumber, ChariGenerateQrCodePayload payload) {
                return generateQrCode(phoneNumber, null, payload);
        }

        public ChariGenerateQrCodeResponse generateQrCode(
                        String phoneNumber, Boolean maskedNumber, ChariGenerateQrCodePayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Generating QR code for phone: {}, amount: {}",
                                PhoneNumberUtil.mask(normalizedPhone), payload.getAmount());

                String url = UriComponentsBuilder.fromPath("/api/operations/merchant/qrcode")
                                .queryParam("phoneNumber", normalizedPhone)
                                .queryParamIfPresent("maskedNumber", java.util.Optional.ofNullable(maskedNumber))
                                .toUriString();
                return httpClient.post(url, payload, ChariGenerateQrCodeResponse.class, "GENERATE_QR_CODE");
        }

        public ChariGenerateQrCodeResponse generateStaticQrCode(String phoneNumber) {
                return generateStaticQrCode(phoneNumber, null);
        }

        public ChariGenerateQrCodeResponse generateStaticQrCode(String phoneNumber, Boolean maskedNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Generating static QR code for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/operations/merchant/qrcode/static")
                                .queryParam("phoneNumber", normalizedPhone)
                                .queryParamIfPresent("maskedNumber", java.util.Optional.ofNullable(maskedNumber))
                                .toUriString();
                return httpClient.get(url, ChariGenerateQrCodeResponse.class, "GENERATE_STATIC_QR_CODE");
        }

        private Map<String, Object> buildQrPaymentPayload(ChariQrCodePaymentPayload payload) {
                Map<String, Object> normalizedPayload = new HashMap<>();
                normalizedPayload.put("customerPhoneNumber",
                                PhoneNumberUtil.normalize(payload.getCustomerPhoneNumber()));
                normalizedPayload.put("qrCodeContent", payload.getQrCodeContent());
                normalizedPayload.put("amount", payload.getAmount());
                if (payload.getIdempotencyKey() != null) {
                        normalizedPayload.put("idempotencyKey", payload.getIdempotencyKey());
                }
                return normalizedPayload;
        }

        private Map<String, Object> amountPayload(BigDecimal amount) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("amount", amount);
                return payload;
        }

        private Map<String, Object> buildCardPaymentPayload(ChariMerchantCardPaymentPayload payload) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("firstName", payload.getFirstName());
                requestPayload.put("lastName", payload.getLastName());
                requestPayload.put("cvv", payload.getCvv());
                requestPayload.put("amount", payload.getAmount());
                requestPayload.put("pan", payload.getPan());
                requestPayload.put("expiryDate", payload.getExpiryDate());
                requestPayload.put("keepAlive", payload.getKeepAlive());
                putIfNotNull(requestPayload, "currency", payload.getCurrency());
                putIfNotNull(requestPayload, "3dSecure", payload.getThreeDSecure());
                putIfNotNull(requestPayload, "feesPercent", payload.getFeesPercent());
                putIfNotNull(requestPayload, "allowInternationalCards", payload.getAllowInternationalCards());
                putIfNotNull(requestPayload, "internationalFeesPercent", payload.getInternationalFeesPercent());
                putIfNotNull(requestPayload, "autoCapture", payload.getAutoCapture());
                putIfNotNull(requestPayload, "notificationUrl", payload.getNotificationUrl());
                putIfNotNull(requestPayload, "acceptUrl", payload.getAcceptUrl());
                putIfNotNull(requestPayload, "declineUrl", payload.getDeclineUrl());
                putIfNotNull(requestPayload, "cardName", payload.getCardName());
                putIfNotNull(requestPayload, "externalReference", payload.getExternalReference());
                return requestPayload;
        }

        private Map<String, Object> buildTokenizedCardPaymentPayload(ChariMerchantTokenizedCardPaymentPayload payload) {
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("cvv", payload.getCvv());
                requestPayload.put("amount", payload.getAmount());
                return requestPayload;
        }

        private void putIfNotNull(Map<String, Object> payload, String fieldName, Object value) {
                if (value != null) {
                        payload.put(fieldName, value);
                }
        }

        private Map<String, Object> buildByPhonePayload(ChariMerchantPaymentByPhonePayload payload) {
                Map<String, Object> normalizedPayload = new HashMap<>();
                normalizedPayload.put("customerPhoneNumber",
                                PhoneNumberUtil.normalize(payload.getCustomerPhoneNumber()));
                normalizedPayload.put("amount", payload.getAmount());
                normalizedPayload.put("reason", payload.getReason());
                normalizedPayload.put("recipientPhoneNumber",
                                PhoneNumberUtil.normalize(payload.getRecipientPhoneNumber()));
                if (payload.getBeneficiaryId() != null) {
                        normalizedPayload.put("beneficiaryId", payload.getBeneficiaryId());
                }
                return normalizedPayload;
        }
}
