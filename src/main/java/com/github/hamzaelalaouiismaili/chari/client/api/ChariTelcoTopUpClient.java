package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTelcoCatalogPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTelcoRechargePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoCatalogResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoRechargeResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Catalog and recharge operations for prepaid Moroccan mobile numbers. */
@Slf4j
@RequiredArgsConstructor
public class ChariTelcoTopUpClient {

    private static final String CATALOG_PATH = "/api/services/telco/catalog/b2b";
    private static final String RECHARGE_PATH = "/api/services/telco/recharge/b2b";

    private final ChariHttpClient httpClient;

    public ChariTelcoCatalogResponse getCatalog(ChariTelcoCatalogPayload payload) {
        validateCatalog(payload);
        ChariTelcoCatalogPayload normalized = ChariTelcoCatalogPayload.builder()
                .recipientPhoneNumber(PhoneNumberUtil.normalize(payload.getRecipientPhoneNumber()))
                .amount(payload.getAmount())
                .operator(payload.getOperator())
                .build();

        log.debug("Retrieving Telco catalog for operator {} and phone {}",
                payload.getOperator(), PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()));
        return httpClient.post(CATALOG_PATH, normalized, ChariTelcoCatalogResponse.class, "TELCO_CATALOG");
    }

    public ChariTelcoRechargeResponse recharge(ChariTelcoRechargePayload payload) {
        validateRecharge(payload);
        ChariTelcoRechargePayload normalized = ChariTelcoRechargePayload.builder()
                .recipientPhoneNumber(PhoneNumberUtil.normalize(payload.getRecipientPhoneNumber()))
                .amount(payload.getAmount())
                .operator(payload.getOperator())
                .rechargeType(payload.getRechargeType())
                .productCode(payload.getProductCode())
                .code(payload.getCode().trim())
                .build();

        log.info("Executing Telco top-up for operator {}, phone {}, amount {}",
                payload.getOperator(), PhoneNumberUtil.mask(payload.getRecipientPhoneNumber()), payload.getAmount());
        return httpClient.post(RECHARGE_PATH, normalized, ChariTelcoRechargeResponse.class, "TELCO_RECHARGE");
    }

    private void validateCatalog(ChariTelcoCatalogPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Telco catalog payload is required");
        }
        validateCommon(payload.getRecipientPhoneNumber(), payload.getAmount(), payload.getOperator());
    }

    private void validateRecharge(ChariTelcoRechargePayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Telco recharge payload is required");
        }
        validateCommon(payload.getRecipientPhoneNumber(), payload.getAmount(), payload.getOperator());
        if (payload.getRechargeType() == null) {
            throw new IllegalArgumentException("Recharge type is required");
        }
        if (payload.getProductCode() == null || payload.getProductCode() < 0) {
            throw new IllegalArgumentException("Product code must be zero or greater");
        }
        if (payload.getCode() == null || payload.getCode().isBlank()) {
            throw new IllegalArgumentException("Principal agent code is required");
        }
    }

    private void validateCommon(String phoneNumber, Integer amount, ChariTelcoOperator operator) {
        if (!PhoneNumberUtil.isValidMoroccanNumber(phoneNumber)) {
            throw new IllegalArgumentException("A valid Moroccan mobile phone number is required");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Telco amount must be positive");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Telco operator is required");
        }
    }
}
