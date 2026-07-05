package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherCatalogQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherPurchasePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherArticlesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPurchaseResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/** Catalog and preview/confirm purchase operations for digital vouchers. */
@Slf4j
@RequiredArgsConstructor
public class ChariVoucherClient {

    private final ChariHttpClient httpClient;

    public ChariVoucherArticlesResponse getArticles(ChariVoucherCatalogQuery query) {
        validateCatalogQuery(query);
        return httpClient.get(buildCatalogUrl("/api/vouchers/articles", query),
                ChariVoucherArticlesResponse.class, "GET_VOUCHER_ARTICLES");
    }

    public ChariVoucherBrandsResponse getBrands(ChariVoucherCatalogQuery query) {
        validateCatalogQuery(query);
        return httpClient.get(buildCatalogUrl("/api/vouchers/brands", query),
                ChariVoucherBrandsResponse.class, "GET_VOUCHER_BRANDS");
    }

    public ChariVoucherBrandResponse getBrand(Integer id, String phoneNumber) {
        validateBrandAndPhone(id, phoneNumber);
        String path = UriComponentsBuilder.fromPath("/api/vouchers/brands/{id}")
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(phoneNumber))
                .buildAndExpand(id)
                .toUriString();
        return httpClient.get(path, ChariVoucherBrandResponse.class, "GET_VOUCHER_BRAND");
    }

    /**
     * Returns the provider response for the brand's voucher endpoint. The current
     * upstream contract represents this response as a brand object.
     */
    public ChariVoucherBrandResponse getVouchersByBrand(Integer id, String phoneNumber) {
        validateBrandAndPhone(id, phoneNumber);
        String path = UriComponentsBuilder.fromPath("/api/vouchers/{id}/articles")
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(phoneNumber))
                .buildAndExpand(id)
                .toUriString();
        return httpClient.get(path, ChariVoucherBrandResponse.class, "GET_VOUCHERS_BY_BRAND");
    }

    public ChariVoucherPreviewResponse previewPurchase(ChariVoucherPurchasePayload payload) {
        ChariVoucherPurchasePayload normalized = normalizePurchase(payload);
        log.debug("Previewing voucher {} for recipient {}", normalized.getProviderSkuId(),
                PhoneNumberUtil.mask(normalized.getDestinationPhoneNumber()));
        return httpClient.post("/api/operations/voucher/preview", normalized,
                ChariVoucherPreviewResponse.class, "PREVIEW_VOUCHER_PURCHASE");
    }

    public ChariVoucherPurchaseResponse confirmPurchase(ChariVoucherPurchasePayload payload) {
        ChariVoucherPurchasePayload normalized = normalizePurchase(payload);
        log.info("Confirming voucher {} for recipient {}", normalized.getProviderSkuId(),
                PhoneNumberUtil.mask(normalized.getDestinationPhoneNumber()));
        return httpClient.post("/api/operations/voucher/confirm", normalized,
                ChariVoucherPurchaseResponse.class, "CONFIRM_VOUCHER_PURCHASE");
    }

    private String buildCatalogUrl(String path, ChariVoucherCatalogQuery query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(query.getPhoneNumber()))
                .queryParam("brandId", query.getBrandId());
        if (query.getPage() != null) {
            builder.queryParam("page", query.getPage());
        }
        if (query.getTake() != null) {
            builder.queryParam("take", query.getTake());
        }
        return builder.toUriString();
    }

    private ChariVoucherPurchasePayload normalizePurchase(ChariVoucherPurchasePayload payload) {
        validatePurchase(payload);
        return ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber(PhoneNumberUtil.normalize(payload.getCustomerPhoneNumber()))
                .destinationPhoneNumber(PhoneNumberUtil.normalize(payload.getDestinationPhoneNumber()))
                .beneficiaryName(payload.getBeneficiaryName().trim())
                .providerSkuId(payload.getProviderSkuId().trim())
                .providerId(payload.getProviderId())
                .build();
    }

    private void validateCatalogQuery(ChariVoucherCatalogQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Voucher catalog query is required");
        }
        validateBrandAndPhone(query.getBrandId(), query.getPhoneNumber());
        if (query.getPage() != null && query.getPage() < 1) {
            throw new IllegalArgumentException("Voucher page must be at least 1");
        }
        if (query.getTake() != null && query.getTake() < 1) {
            throw new IllegalArgumentException("Voucher page size must be positive");
        }
    }

    private void validateBrandAndPhone(Integer brandId, String phoneNumber) {
        if (brandId == null || brandId <= 0) {
            throw new IllegalArgumentException("Voucher brand ID must be positive");
        }
        if (!PhoneNumberUtil.isValidMoroccanNumber(phoneNumber)) {
            throw new IllegalArgumentException("A valid Moroccan mobile phone number is required");
        }
    }

    private void validatePurchase(ChariVoucherPurchasePayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Voucher purchase payload is required");
        }
        if (!PhoneNumberUtil.isValidMoroccanNumber(payload.getCustomerPhoneNumber())) {
            throw new IllegalArgumentException("A valid customer Moroccan mobile phone number is required");
        }
        if (!PhoneNumberUtil.isValidMoroccanNumber(payload.getDestinationPhoneNumber())) {
            throw new IllegalArgumentException("A valid destination Moroccan mobile phone number is required");
        }
        if (payload.getBeneficiaryName() == null || payload.getBeneficiaryName().isBlank()) {
            throw new IllegalArgumentException("Voucher beneficiary name is required");
        }
        if (payload.getProviderSkuId() == null || payload.getProviderSkuId().isBlank()) {
            throw new IllegalArgumentException("Voucher provider SKU ID is required");
        }
        if (payload.getProviderId() == null || payload.getProviderId() <= 0) {
            throw new IllegalArgumentException("Voucher provider ID must be positive");
        }
    }
}
