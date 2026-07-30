# Voucher Provider Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the starter's voucher client, payloads, and response models to the provider's new contract captured in `vouchers.json`.

**Architecture:** `ChariVoucherClient` holds the HTTP calls and validation; `ChariBaasClient` is the public facade that delegates to it; request/response DTOs live under `model/payload` and `model/response`. This migration mutates the changed DTOs in place (clean replace) and adds new endpoints + one new response model.

**Tech Stack:** Java 21, Spring `RestTemplate`, Lombok, Jackson, JUnit 5 + `MockRestServiceServer` + AssertJ, Maven (via `./mvnw`).

## Global Constraints

- Java 21 toolchain (`./mvnw` uses the bundled wrapper 3.9.9).
- Clean replace: no `@Deprecated` wrappers for old voucher fields/methods.
- Purchase identifier is now `skuId` (Integer, required, > 0). `amount`, `price`, `providerSkuId` are optional/nullable and passed through as-is.
- Preview request/response and the shared `normalizePurchase`/`validatePurchase` are reused by both the standard and the `service` voucher endpoints.
- Payload is serialized with null fields included (no `@JsonInclude`).
- Run the voucher suite with: `./mvnw -q test -Dtest=ChariVoucherClientTest`
- Package base: `com.github.hamzaelalaouiismaili.chari`
- Test file uses text blocks and existing helpers (`context()`, `properties()`).

---

### Task 1: Migrate purchase payload + client validation/normalize

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/model/payload/ChariVoucherPurchasePayload.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java:86-95` (normalizePurchase), `:119-138` (validatePurchase)
- Test: `src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java:152-175`

**Interfaces:**
- Produces: `ChariVoucherPurchasePayload` with getters/builder for `customerPhoneNumber, destinationPhoneNumber, beneficiaryName, amount (BigDecimal), skuId (Integer), providerSkuId (String), price (BigDecimal), providerId (Integer)`.
- Produces: `ChariVoucherClient.normalizePurchase(payload)` and `validatePurchase(payload)` requiring `skuId > 0` and `providerId > 0`.

- [ ] **Step 1: Update the payload DTO**

Replace the fields block in `ChariVoucherPurchasePayload.java` (keep the Lombok annotations and `import java.math.BigDecimal;`):

```java
package com.github.hamzaelalaouiismaili.chari.model.payload;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Shared payload for voucher purchase preview and confirmation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherPurchasePayload {

    private String customerPhoneNumber;
    private String destinationPhoneNumber;
    private String beneficiaryName;
    private BigDecimal amount;
    private Integer skuId;
    private String providerSkuId;
    private BigDecimal price;
    private Integer providerId;
}
```

- [ ] **Step 2: Update `normalizePurchase` and `validatePurchase`**

In `ChariVoucherClient.java`, replace `normalizePurchase` (lines ~86-95):

```java
    private ChariVoucherPurchasePayload normalizePurchase(ChariVoucherPurchasePayload payload) {
        validatePurchase(payload);
        return ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber(PhoneNumberUtil.normalize(payload.getCustomerPhoneNumber()))
                .destinationPhoneNumber(PhoneNumberUtil.normalize(payload.getDestinationPhoneNumber()))
                .beneficiaryName(payload.getBeneficiaryName().trim())
                .amount(payload.getAmount())
                .skuId(payload.getSkuId())
                .providerSkuId(payload.getProviderSkuId() == null ? null : payload.getProviderSkuId().trim())
                .price(payload.getPrice())
                .providerId(payload.getProviderId())
                .build();
    }
```

Replace the last two checks in `validatePurchase` (the `providerSkuId` and `providerId` blocks, lines ~132-137) with:

```java
        if (payload.getSkuId() == null || payload.getSkuId() <= 0) {
            throw new IllegalArgumentException("Voucher SKU ID must be positive");
        }
        if (payload.getProviderId() == null || payload.getProviderId() <= 0) {
            throw new IllegalArgumentException("Voucher provider ID must be positive");
        }
```

- [ ] **Step 3: Rewrite the local-validation test**

Replace `rejectsInvalidCatalogAndPurchaseInputsLocally` and the `purchasePayload()` helper in `ChariVoucherClientTest.java`:

```java
    @Test
    void rejectsInvalidCatalogAndPurchaseInputsLocally() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

        assertThatThrownBy(() -> client.getVoucherArticles("0661231234", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher brand ID must be positive");

        ChariVoucherPurchasePayload payload = purchasePayload();
        payload.setSkuId(0);
        assertThatThrownBy(() -> client.previewVoucherPurchase(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher SKU ID must be positive");
    }

    private ChariVoucherPurchasePayload purchasePayload() {
        return ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber("0661231234")
                .destinationPhoneNumber("0662345678")
                .beneficiaryName(" Abdennour ")
                .amount(new java.math.BigDecimal("0"))
                .skuId(18)
                .price(new java.math.BigDecimal("512.76"))
                .providerId(2)
                .build();
    }
```

- [ ] **Step 4: Run tests — expect the round-trip test to fail, validation test to pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: `rejectsInvalidCatalogAndPurchaseInputsLocally` PASSES; `previewsAndConfirmsSameNormalizedPurchasePayload` FAILS (body/response mismatch — fixed in Task 2). Compilation succeeds.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/model/payload/ChariVoucherPurchasePayload.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): migrate purchase payload to skuId-based contract"
```

---

### Task 2: Flatten the confirm response model

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherPurchaseResponse.java`
- Test: `src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java:100-150`

**Interfaces:**
- Consumes: `ChariVoucherPurchasePayload` (Task 1).
- Produces: `ChariVoucherPurchaseResponse.getData()` returning `VoucherPurchaseData` with flat getters `getOperationType(), getVoucherName(), getAmount(), getCashBack(), getTotalAmount(), getReason(), getRecipientPhoneNumber(), getCheckedAt(), getUrlActivateCard(), getDestinationPhoneNumber(), getBeneficiaryName(), getCode(), getDescription(), getTypedOperationType()`.

- [ ] **Step 1: Replace the response model**

Replace the whole `ChariVoucherPurchaseResponse.java`:

```java
package com.github.hamzaelalaouiismaili.chari.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Confirmed voucher purchase including the redeemable voucher code. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherPurchaseResponse {

    private VoucherPurchaseData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherPurchaseData {

        private Integer operationType;
        private String voucherName;
        private BigDecimal amount;
        private BigDecimal cashBack;
        private BigDecimal totalAmount;
        private String reason;
        private String recipientPhoneNumber;
        private String checkedAt;
        private String urlActivateCard;
        private String destinationPhoneNumber;
        private String beneficiaryName;
        private String code;
        private String description;

        @JsonIgnore
        public ChariOperationType getTypedOperationType() {
            return ChariOperationType.fromCode(operationType);
        }
    }
}
```

- [ ] **Step 2: Rewrite the round-trip test**

Replace `previewsAndConfirmsSameNormalizedPurchasePayload` in `ChariVoucherClientTest.java`:

```java
    @Test
    void previewsAndConfirmsSameNormalizedPurchasePayload() {
        TestContext context = context();
        String expectedBody = """
                {
                  "customerPhoneNumber":"+212661231234",
                  "destinationPhoneNumber":"+212662345678",
                  "beneficiaryName":"Abdennour",
                  "amount":0,
                  "skuId":18,
                  "providerSkuId":null,
                  "price":512.76,
                  "providerId":2
                }
                """;
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/voucher/preview"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedBody))
                .andRespond(withSuccess("""
                        {"data":{"type":23,"operation":{
                          "customerPhoneNumber":"+212661231234","amount":2.16,"reason":"",
                          "beneficiaryId":null,"recipientPhoneNumber":"+212662345678"
                        },"feesAmount":0.15,"totalAmount":2.16,
                        "checkedAt":"2026-03-31T14:52:07","openLoop":false}}
                        """, MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/voucher/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedBody))
                .andRespond(withSuccess("""
                        {"data":{
                          "operationType":23,"voucherName":"USD 50","amount":564.04,
                          "cashBack":36.66,"totalAmount":512.76,"reason":null,
                          "recipientPhoneNumber":"+212662345678",
                          "checkedAt":"2026-07-30T15:05:59.8632851Z","urlActivateCard":null,
                          "destinationPhoneNumber":"+212662345678","beneficiaryName":"Abdennour",
                          "code":"0qGNUOQWAWHVT1","description":"Gaming credit"
                        }}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherPurchasePayload payload = purchasePayload();
        ChariVoucherPreviewResponse preview = context.client.previewVoucherPurchase(payload);
        ChariVoucherPurchaseResponse purchase = context.client.confirmVoucherPurchase(payload);

        assertThat(preview.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(preview.getData().getFeesAmount()).isEqualByComparingTo("0.15");
        assertThat(purchase.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(purchase.getData().getCode()).isEqualTo("0qGNUOQWAWHVT1");
        assertThat(purchase.getData().getCashBack()).isEqualByComparingTo("36.66");
        context.server.verify();
    }
```

Note: `purchasePayload()` sets `beneficiaryName(" Abdennour ")` and `providerSkuId` unset (null), matching `expectedBody` after normalization (trim + null passthrough).

- [ ] **Step 3: Run tests — expect all current voucher tests to pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS (all methods).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherPurchaseResponse.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): flatten confirm response to new provider shape"
```

---

### Task 3: Add `skuId` + `pricing` to the catalog item

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherArticlesResponse.java`
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Produces: `ChariVoucherArticlesResponse.VoucherArticle` with added `getSkuId()` (Integer) and `getPricing()` returning nested `VoucherPricing { getMaxValueAmount(), getBaseValueAmount() (BigDecimal), getIsVariableValue() (Boolean), getMultiplicateur() (Integer) }`.

- [ ] **Step 1: Add a failing test for skuId + pricing parsing**

Add to `ChariVoucherClientTest.java`:

```java
    @Test
    void parsesSkuIdAndPricingOnArticles() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/articles?phoneNumber=+212661231234&brandId=25&page=1&take=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":15,"providerSkuId":"NPAP","productName":"1800 Flowers",
                          "imageUrl":null,"price":150,"description":"","providerId":3,"brandId":-1,
                          "pricing":{"maxValueAmount":5000,"baseValueAmount":150,
                                     "isVariableValue":true,"multiplicateur":0}
                        }],"count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response =
                context.client.getVoucherArticles("0661231234", 25);
        var article = response.getData().getCollection().getFirst();

        assertThat(article.getSkuId()).isEqualTo(15);
        assertThat(article.getPricing().getMaxValueAmount()).isEqualByComparingTo("5000");
        assertThat(article.getPricing().getIsVariableValue()).isTrue();
        assertThat(article.getPricing().getMultiplicateur()).isEqualTo(0);
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#parsesSkuIdAndPricingOnArticles`
Expected: FAIL — compile error (`getSkuId`/`getPricing` do not exist).

- [ ] **Step 3: Extend the model**

In `ChariVoucherArticlesResponse.java`, add `skuId` + `pricing` to `VoucherArticle` and add the nested class:

```java
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherArticle {

        private Integer skuId;
        private String providerSkuId;
        private String productName;
        private String imageUrl;
        private BigDecimal price;
        private String description;
        private Integer providerId;
        private Integer brandId;
        private VoucherPricing pricing;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoucherPricing {

        private BigDecimal maxValueAmount;
        private BigDecimal baseValueAmount;
        private Boolean isVariableValue;
        private Integer multiplicateur;
    }
```

- [ ] **Step 4: Run tests to verify pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherArticlesResponse.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): add skuId and pricing to catalog item"
```

---

### Task 4: Add optional `keyword` to catalog query + conditional URL params

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/model/payload/ChariVoucherCatalogQuery.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java:73-84` (buildCatalogUrl)
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Produces: `ChariVoucherCatalogQuery` with added `getKeyword()`/builder `.keyword(String)`.
- Produces: `buildCatalogUrl(path, query)` appending `brandId` only when non-null and `keyword` only when non-null, in order `phoneNumber, brandId, page, take, keyword`.

- [ ] **Step 1: Add a failing test that exercises keyword appended after paging**

Add to `ChariVoucherClientTest.java`. (`getVoucherArticles` still requires `brandId > 0`, so keep brandId set here; the null-brandId path is covered by `getLocalVouchers` in Task 7.)

```java
    @Test
    void appendsKeywordAfterPagingInCatalogUrl() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/articles?phoneNumber=+212661231234&brandId=25&page=1&take=10&keyword=razer"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":{\"collection\":[],\"count\":0}}",
                        MediaType.APPLICATION_JSON));

        context.client.getVoucherArticles(ChariVoucherCatalogQuery.builder()
                .phoneNumber("0661231234")
                .brandId(25)
                .page(1)
                .take(10)
                .keyword("razer")
                .build());
        context.server.verify();
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#appendsKeywordAfterPagingInCatalogUrl`
Expected: FAIL — compile error (`.keyword(...)` unknown) or URL mismatch (keyword absent).

- [ ] **Step 3: Add the field and update the URL builder**

Add to `ChariVoucherCatalogQuery.java` after `take`:

```java
    private String keyword;
```

Replace `buildCatalogUrl` in `ChariVoucherClient.java`:

```java
    private String buildCatalogUrl(String path, ChariVoucherCatalogQuery query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path)
                .queryParam("phoneNumber", PhoneNumberUtil.normalize(query.getPhoneNumber()));
        if (query.getBrandId() != null) {
            builder.queryParam("brandId", query.getBrandId());
        }
        if (query.getPage() != null) {
            builder.queryParam("page", query.getPage());
        }
        if (query.getTake() != null) {
            builder.queryParam("take", query.getTake());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            builder.queryParam("keyword", query.getKeyword());
        }
        return builder.toUriString();
    }
```

- [ ] **Step 4: Run the full voucher suite (verify existing article/brand URLs unchanged)**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS — existing `retrievesPaginatedArticlesAndMapsDecimalPrice` and `retrievesBrandsWithDefaultPagination` still match (brandId non-null → still appended).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/model/payload/ChariVoucherCatalogQuery.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): support keyword filter and optional brandId in catalog url"
```

---

### Task 5: Add product list endpoint (`/api/vouchers/product`)

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java:956` (add after `getVouchersByBrand`)
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Produces: `ChariVoucherClient.getProducts(Integer page, Integer take)` → `ChariVoucherArticlesResponse`.
- Produces: `ChariBaasClient.getVoucherProducts(Integer page, Integer take)` → `ChariVoucherArticlesResponse`.

- [ ] **Step 1: Add a failing test**

Add to `ChariVoucherClientTest.java`:

```java
    @Test
    void fetchesProductListWithPaging() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/product?page=1&take=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":0,"providerSkuId":"NPAP","productName":"1800 Flowers",
                          "imageUrl":"https://img","price":150,"description":"","providerId":3,
                          "brandId":-1,"pricing":{"maxValueAmount":5000,"baseValueAmount":150,
                          "isVariableValue":true,"multiplicateur":0}}],"count":168}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response = context.client.getVoucherProducts(1, 2);

        assertThat(response.getData().getCount()).isEqualTo(168);
        assertThat(response.getData().getCollection().getFirst().getProviderSkuId()).isEqualTo("NPAP");
        context.server.verify();
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#fetchesProductListWithPaging`
Expected: FAIL — `getVoucherProducts` unknown.

- [ ] **Step 3: Add the client method**

In `ChariVoucherClient.java`, add after `getVouchersByBrand`:

```java
    public ChariVoucherArticlesResponse getProducts(Integer page, Integer take) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/vouchers/product");
        if (page != null) {
            if (page < 1) {
                throw new IllegalArgumentException("Voucher page must be at least 1");
            }
            builder.queryParam("page", page);
        }
        if (take != null) {
            if (take < 1) {
                throw new IllegalArgumentException("Voucher page size must be positive");
            }
            builder.queryParam("take", take);
        }
        return httpClient.get(builder.toUriString(),
                ChariVoucherArticlesResponse.class, "GET_VOUCHER_PRODUCTS");
    }
```

- [ ] **Step 4: Add the facade method**

In `ChariBaasClient.java`, after `getVouchersByBrand` (line ~956):

```java
    /** Retrieves the paginated Click Apporter / Blackhawk product catalog. */
    public ChariVoucherArticlesResponse getVoucherProducts(Integer page, Integer take) {
        return voucherClient.getProducts(page, take);
    }
```

- [ ] **Step 5: Run tests to verify pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): add product list endpoint"
```

---

### Task 6: Add product detail endpoint + `ChariVoucherProductResponse`

**Files:**
- Create: `src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherProductResponse.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java`
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Produces: `ChariVoucherProductResponse.getData()` → `ProductData` with `getCapProductId(), getCapDefaultUpc(), getName(), getBlackhawkId(), getPriceUsd(), getPriceMadCurrent(), getProductConfigId(), getProductDescription(), getProductImages(), getActivationCharacteristics(), getRedemptionCharacteristics(), getTermsAndConditions()`.
- Produces: `ChariVoucherClient.getProductDetail(String configId)` → `ChariVoucherProductResponse`.
- Produces: `ChariBaasClient.getVoucherProductDetail(String configId)` → `ChariVoucherProductResponse`.

- [ ] **Step 1: Create the response model**

Create `ChariVoucherProductResponse.java`:

```java
package com.github.hamzaelalaouiismaili.chari.model.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Detailed voucher product information (Blackhawk product management shape). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChariVoucherProductResponse {

    private ProductData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductData {

        private Integer capProductId;
        private String capDefaultUpc;
        private String name;
        private String blackhawkId;
        private BigDecimal priceUsd;
        private BigDecimal priceMadCurrent;
        private String productConfigId;
        private String productDescription;
        private List<ProductImage> productImages;
        private ActivationCharacteristics activationCharacteristics;
        private RedemptionCharacteristics redemptionCharacteristics;
        private List<TermsAndConditions> termsAndConditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImage {

        private String id;
        private String imageSize;
        private String frontImage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivationCharacteristics {

        private BigDecimal maxValueAmount;
        private BigDecimal baseValueAmount;
        private Boolean isVariableValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedemptionCharacteristics {

        private List<String> redemptionOptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermsAndConditions {

        private String id;
        private String termsAndConditions;
        private String termsAndConditionsType;
    }
}
```

- [ ] **Step 2: Add a failing test**

Add to `ChariVoucherClientTest.java` (and add the import `com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherProductResponse`):

```java
    @Test
    void fetchesProductDetailByConfigId() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/products/NPAP123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{
                          "capProductId":29,"capDefaultUpc":"07675021141","name":"1800 Flowers",
                          "blackhawkId":"https://api/product/C6S7","priceUsd":15,
                          "priceMadCurrent":135.025,"productConfigId":"NPAP123",
                          "productDescription":"Flowers and gifts",
                          "productImages":[{"id":"Q717","imageSize":"SMALL",
                            "frontImage":"https://img/icon.png"}],
                          "activationCharacteristics":{"maxValueAmount":500,
                            "baseValueAmount":15,"isVariableValue":true},
                          "redemptionCharacteristics":{"redemptionOptions":["IN_STORE","ON_LINE"]},
                          "termsAndConditions":[{"id":"816B","termsAndConditions":"Terms text",
                            "termsAndConditionsType":"WEB"}]
                        }}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherProductResponse response = context.client.getVoucherProductDetail("NPAP123");
        var data = response.getData();

        assertThat(data.getCapProductId()).isEqualTo(29);
        assertThat(data.getPriceMadCurrent()).isEqualByComparingTo("135.025");
        assertThat(data.getProductImages().getFirst().getImageSize()).isEqualTo("SMALL");
        assertThat(data.getActivationCharacteristics().getIsVariableValue()).isTrue();
        assertThat(data.getRedemptionCharacteristics().getRedemptionOptions()).containsExactly("IN_STORE", "ON_LINE");
        assertThat(data.getTermsAndConditions().getFirst().getTermsAndConditionsType()).isEqualTo("WEB");
        context.server.verify();
    }
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#fetchesProductDetailByConfigId`
Expected: FAIL — `getVoucherProductDetail` unknown.

- [ ] **Step 4: Add client + facade methods**

In `ChariVoucherClient.java`, add (with import for `ChariVoucherProductResponse`):

```java
    public ChariVoucherProductResponse getProductDetail(String configId) {
        if (configId == null || configId.isBlank()) {
            throw new IllegalArgumentException("Voucher product config ID is required");
        }
        String path = UriComponentsBuilder.fromPath("/api/vouchers/products/{configId}")
                .buildAndExpand(configId)
                .toUriString();
        return httpClient.get(path, ChariVoucherProductResponse.class, "GET_VOUCHER_PRODUCT_DETAIL");
    }
```

In `ChariBaasClient.java` (add import + method after `getVoucherProducts`):

```java
    /** Retrieves detailed Blackhawk product information by config ID. */
    public ChariVoucherProductResponse getVoucherProductDetail(String configId) {
        return voucherClient.getProductDetail(configId);
    }
```

- [ ] **Step 5: Run tests to verify pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/model/response/ChariVoucherProductResponse.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): add product detail endpoint and response model"
```

---

### Task 7: Add local vouchers list endpoint (`/api/vouchers`)

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java`
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Produces: `ChariVoucherClient.getLocalVouchers(ChariVoucherCatalogQuery query)` → `ChariVoucherArticlesResponse`. Requires a valid phone number; `brandId`, `keyword`, `page`, `take` optional.
- Produces: `ChariBaasClient.getLocalVouchers(ChariVoucherCatalogQuery query)` and overload `getLocalVouchers(String phoneNumber)`.

- [ ] **Step 1: Add a failing test (phone required, brandId omitted, keyword applied)**

Add to `ChariVoucherClientTest.java`:

```java
    @Test
    void fetchesLocalVouchersWithoutBrandId() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers?phoneNumber=+212661231234&keyword=pubg"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":15,"providerSkuId":"15","productName":"PUBG UC","imageUrl":null,
                          "price":100,"description":"","providerId":1,"brandId":44,"pricing":null}],
                          "count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response = context.client.getLocalVouchers(
                ChariVoucherCatalogQuery.builder()
                        .phoneNumber("0661231234")
                        .keyword("pubg")
                        .build());

        assertThat(response.getData().getCollection().getFirst().getSkuId()).isEqualTo(15);
        context.server.verify();
    }

    @Test
    void rejectsLocalVouchersWithoutPhone() {
        assertThatThrownBy(() -> new ChariBaasClient(new RestTemplate(), properties())
                .getLocalVouchers(ChariVoucherCatalogQuery.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A valid Moroccan mobile phone number is required");
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#fetchesLocalVouchersWithoutBrandId+rejectsLocalVouchersWithoutPhone`
Expected: FAIL — `getLocalVouchers` unknown.

- [ ] **Step 3: Add the client method**

In `ChariVoucherClient.java`:

```java
    public ChariVoucherArticlesResponse getLocalVouchers(ChariVoucherCatalogQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Voucher catalog query is required");
        }
        if (!PhoneNumberUtil.isValidMoroccanNumber(query.getPhoneNumber())) {
            throw new IllegalArgumentException("A valid Moroccan mobile phone number is required");
        }
        if (query.getPage() != null && query.getPage() < 1) {
            throw new IllegalArgumentException("Voucher page must be at least 1");
        }
        if (query.getTake() != null && query.getTake() < 1) {
            throw new IllegalArgumentException("Voucher page size must be positive");
        }
        return httpClient.get(buildCatalogUrl("/api/vouchers", query),
                ChariVoucherArticlesResponse.class, "GET_LOCAL_VOUCHERS");
    }
```

- [ ] **Step 4: Add the facade methods**

In `ChariBaasClient.java`:

```java
    /** Lists local vouchers for a phone number (optional brandId/keyword filters). */
    public ChariVoucherArticlesResponse getLocalVouchers(ChariVoucherCatalogQuery query) {
        return voucherClient.getLocalVouchers(query);
    }

    /** Lists the first ten local vouchers for a phone number. */
    public ChariVoucherArticlesResponse getLocalVouchers(String phoneNumber) {
        return getLocalVouchers(ChariVoucherCatalogQuery.builder()
                .phoneNumber(phoneNumber)
                .page(1)
                .take(10)
                .build());
    }
```

- [ ] **Step 5: Run tests to verify pass**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): add local vouchers list endpoint"
```

---

### Task 8: Add service voucher preview + purchase endpoints

**Files:**
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java`
- Modify: `src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java`
- Test: `ChariVoucherClientTest.java` (new method)

**Interfaces:**
- Consumes: `normalizePurchase`/`validatePurchase` (Task 1), `ChariVoucherPreviewResponse`, `ChariVoucherPurchaseResponse` (Task 2).
- Produces: `ChariVoucherClient.previewServicePurchase(payload)` → `ChariVoucherPreviewResponse`; `purchaseServiceVoucher(payload)` → `ChariVoucherPurchaseResponse`.
- Produces: `ChariBaasClient.previewServiceVoucherPurchase(payload)` and `purchaseServiceVoucher(payload)`.

- [ ] **Step 1: Add a failing test**

Add to `ChariVoucherClientTest.java`:

```java
    @Test
    void previewsAndPurchasesServiceVoucher() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/service/voucher/preview"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"data":{"type":23,"operation":{
                          "customerPhoneNumber":"+212661231234","amount":2.16,"reason":"",
                          "beneficiaryId":null,"recipientPhoneNumber":"+212662345678"
                        },"feesAmount":0.15,"totalAmount":2.16,
                        "checkedAt":"2026-03-31T14:52:07","openLoop":false}}
                        """, MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/service/voucher"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"data":{"operationType":23,"voucherName":"USD 50","amount":564.04,
                          "cashBack":36.66,"totalAmount":512.76,"reason":null,
                          "recipientPhoneNumber":"+212662345678","checkedAt":"2026-07-30T15:05:59Z",
                          "urlActivateCard":null,"destinationPhoneNumber":"+212662345678",
                          "beneficiaryName":"Abdennour","code":"0qGNUOQWAWHVT1","description":"x"}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherPurchasePayload payload = purchasePayload();
        ChariVoucherPreviewResponse preview = context.client.previewServiceVoucherPurchase(payload);
        ChariVoucherPurchaseResponse purchase = context.client.purchaseServiceVoucher(payload);

        assertThat(preview.getData().getTotalAmount()).isEqualByComparingTo("2.16");
        assertThat(purchase.getData().getCode()).isEqualTo("0qGNUOQWAWHVT1");
        context.server.verify();
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest#previewsAndPurchasesServiceVoucher`
Expected: FAIL — `previewServiceVoucherPurchase` unknown.

- [ ] **Step 3: Add the client methods**

In `ChariVoucherClient.java`:

```java
    public ChariVoucherPreviewResponse previewServicePurchase(ChariVoucherPurchasePayload payload) {
        ChariVoucherPurchasePayload normalized = normalizePurchase(payload);
        log.debug("Previewing service voucher {} for recipient {}", normalized.getSkuId(),
                PhoneNumberUtil.mask(normalized.getDestinationPhoneNumber()));
        return httpClient.post("/api/operations/service/voucher/preview", normalized,
                ChariVoucherPreviewResponse.class, "PREVIEW_SERVICE_VOUCHER_PURCHASE");
    }

    public ChariVoucherPurchaseResponse purchaseServiceVoucher(ChariVoucherPurchasePayload payload) {
        ChariVoucherPurchasePayload normalized = normalizePurchase(payload);
        log.info("Purchasing service voucher {} for recipient {}", normalized.getSkuId(),
                PhoneNumberUtil.mask(normalized.getDestinationPhoneNumber()));
        return httpClient.post("/api/operations/service/voucher", normalized,
                ChariVoucherPurchaseResponse.class, "PURCHASE_SERVICE_VOUCHER");
    }
```

- [ ] **Step 4: Add the facade methods**

In `ChariBaasClient.java`, after `confirmVoucherPurchase`:

```java
    /** Previews a service voucher purchase (service endpoint variant). */
    public ChariVoucherPreviewResponse previewServiceVoucherPurchase(ChariVoucherPurchasePayload payload) {
        return voucherClient.previewServicePurchase(payload);
    }

    /** Purchases a service voucher (service endpoint variant). */
    public ChariVoucherPurchaseResponse purchaseServiceVoucher(ChariVoucherPurchasePayload payload) {
        return voucherClient.purchaseServiceVoucher(payload);
    }
```

- [ ] **Step 5: Run the full voucher suite**

Run: `./mvnw -q test -Dtest=ChariVoucherClientTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/github/hamzaelalaouiismaili/chari/client/api/ChariVoucherClient.java \
        src/main/java/com/github/hamzaelalaouiismaili/chari/client/ChariBaasClient.java \
        src/test/java/com/github/hamzaelalaouiismaili/chari/client/ChariVoucherClientTest.java
git commit -m "feat(voucher): add service voucher preview and purchase endpoints"
```

---

### Task 9: Update integration docs + full build

**Files:**
- Modify: whichever guide documents vouchers (`SERVICES_INTEGRATION_GUIDE.md`, `chari-baas-sdk.md`, or `SDK_USAGE.md` — grep first)

**Interfaces:** none (docs only).

- [ ] **Step 1: Find current voucher docs**

Run: `grep -rln -i "voucher" *.md`
Read each hit's voucher section.

- [ ] **Step 2: Update the docs**

For each voucher section, update:
- Purchase payload example to the new body: `{customerPhoneNumber, destinationPhoneNumber, beneficiaryName, amount, skuId, providerSkuId, price, providerId}` and note `skuId` is the required identifier.
- Confirm response example to the flattened `data` shape (fields directly on `data`, no nested `operation`).
- Add the new facade methods: `getVoucherProducts(page, take)`, `getVoucherProductDetail(configId)`, `getLocalVouchers(...)`, `previewServiceVoucherPurchase(...)`, `purchaseServiceVoucher(...)`.
- Note the new `keyword` catalog filter and `skuId`/`pricing` fields on catalog items.

Keep the prose style consistent with the surrounding guide (match heading levels and code-fence language tags already used).

- [ ] **Step 3: Run the entire test suite**

Run: `./mvnw -q test`
Expected: BUILD SUCCESS (all modules/tests green).

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "docs(voucher): document new provider contract and endpoints"
```

---

## Self-Review

**Spec coverage:**
- Payload changes → Task 1 ✓
- Confirm response flattening → Task 2 ✓
- Preview response unchanged → no task needed (verified in Task 2 test) ✓
- Article `skuId` + `pricing` → Task 3 ✓
- Catalog `keyword` + conditional URL → Task 4 ✓
- Product list → Task 5 ✓
- Product detail + new model → Task 6 ✓
- Local vouchers → Task 7 ✓
- Service preview + purchase → Task 8 ✓
- Docs → Task 9 ✓

**Type consistency:** `getSkuId()`/`getPricing()` (Task 3) reused in Tasks 5/7 tests; `VoucherPurchaseData` flat getters (Task 2) reused in Task 8; `getProducts`/`getProductDetail`/`getLocalVouchers`/`previewServicePurchase`/`purchaseServiceVoucher` client names match their facade delegations. `ChariVoucherArticlesResponse` reused for products, local vouchers, and articles. Consistent.

**Placeholder scan:** No TBD/TODO; every code step has concrete code and exact run/expected lines.
