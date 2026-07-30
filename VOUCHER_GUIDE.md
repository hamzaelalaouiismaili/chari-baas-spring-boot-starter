# Voucher Integration Guide

Complete guide to buying and browsing digital vouchers and gift cards through the
Chari BaaS SDK — Digital Virgo / Carry1st articles, Click Apporter / Blackhawk
products, and local vouchers.

## Contents

1. [Overview](#1-overview)
2. [Quick start](#2-quick-start)
3. [Browsing the catalog](#3-browsing-the-catalog)
4. [Product catalog (Click Apporter / Blackhawk)](#4-product-catalog-click-apporter--blackhawk)
5. [Local vouchers](#5-local-vouchers)
6. [Preview and confirm a purchase](#6-preview-and-confirm-a-purchase)
7. [Service endpoint variant](#7-service-endpoint-variant)
8. [Data models](#8-data-models)
9. [Validation rules](#9-validation-rules)
10. [Error handling](#10-error-handling)
11. [Security notes](#11-security-notes)
12. [Method reference](#12-method-reference)

---

## 1. Overview

A voucher purchase always follows a **preview → confirm** flow:

1. **Preview** validates the request and returns the fees and the total the
   customer will pay. Nothing is charged.
2. **Confirm** executes the purchase and returns the redeemable voucher `code`.

Pass the **same** `ChariVoucherPurchasePayload` to both calls. All catalog and
purchase methods are exposed on `ChariBaasClient`. Local Moroccan phone numbers
(`06…` / `07…`) are normalized to `+212…` automatically.

Voucher operations report `ChariOperationType.VOUCHER` (operation code `23`).

> **Contract note:** `skuId` is the purchase identifier. `amount`, `price`, and
> `providerSkuId` are optional. The confirmation response is **flat** — read the
> voucher `code` directly from `confirmed.getData()`, not from a nested
> `getOperation()`.

---

## 2. Quick start

```java
@Service
public class VoucherService {

    private final ChariBaasClient chari;

    public VoucherService(ChariBaasClient chari) {
        this.chari = chari;
    }

    public String buyFirstArticle(String customerPhone, String recipientPhone, int brandId) {
        // 1. Pick an article from the catalog
        ChariVoucherArticlesResponse catalog = chari.getVoucherArticles(customerPhone, brandId);
        ChariVoucherArticlesResponse.VoucherArticle article =
                catalog.getData().getCollection().getFirst();

        // 2. Build the purchase payload (skuId is the identifier)
        ChariVoucherPurchasePayload purchase = ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber(customerPhone)
                .destinationPhoneNumber(recipientPhone)
                .beneficiaryName("Abdennour")
                .skuId(article.getSkuId())
                .price(article.getPrice())      // optional
                .providerId(article.getProviderId())
                .build();

        // 3. Preview — show fees + total to the customer
        ChariVoucherPreviewResponse preview = chari.previewVoucherPurchase(purchase);
        BigDecimal total = preview.getData().getTotalAmount();

        // 4. Confirm after the customer accepts the total
        ChariVoucherPurchaseResponse confirmed = chari.confirmVoucherPurchase(purchase);
        return confirmed.getData().getCode();   // deliver this securely
    }
}
```

---

## 3. Browsing the catalog

### Articles for a brand

The short overload returns the first ten articles for a brand:

```java
ChariVoucherArticlesResponse articles = chari.getVoucherArticles("0661231234", 25);
```

Use `ChariVoucherCatalogQuery` for pagination and keyword search:

```java
ChariVoucherCatalogQuery query = ChariVoucherCatalogQuery.builder()
        .phoneNumber("0661231234")
        .brandId(25)
        .page(1)
        .take(20)
        .keyword("razer")   // optional free-text filter
        .build();

ChariVoucherArticlesResponse articles = chari.getVoucherArticles(query);
ChariVoucherBrandsResponse brands = chari.getVoucherBrands(query);
```

### Brands

```java
// First ten brands
ChariVoucherBrandsResponse brands = chari.getVoucherBrands("0661231234", 25);

// One brand by ID
ChariVoucherBrandResponse brand = chari.getVoucherBrand(2, "0661231234");

// Articles that belong to a brand
ChariVoucherBrandResponse brandArticles = chari.getVouchersByBrand(2, "0661231234");
```

> The current upstream contract maps `getVouchersByBrand(...)` to a
> `ChariVoucherBrandResponse`.

---

## 4. Product catalog (Click Apporter / Blackhawk)

The product catalog is paginated and does **not** require a phone number.

```java
// page, take (both optional; omit for the provider default)
ChariVoucherArticlesResponse products = chari.getVoucherProducts(1, 20);

int total = products.getData().getCount();
List<ChariVoucherArticlesResponse.VoucherArticle> items =
        products.getData().getCollection();
```

Fetch detailed information for a single product by its config ID
(`providerSkuId` from the product list):

```java
ChariVoucherProductResponse detail =
        chari.getVoucherProductDetail("NPAPCWWCJXWG61A9579JY2XC6C");

ChariVoucherProductResponse.ProductData data = detail.getData();
String name = data.getName();
BigDecimal priceMad = data.getPriceMadCurrent();
List<ChariVoucherProductResponse.ProductImage> images = data.getProductImages();
List<String> redemption = data.getRedemptionCharacteristics().getRedemptionOptions();
```

---

## 5. Local vouchers

Lists vouchers already available for a specific phone number. `brandId` and
`keyword` are optional here (unlike the brand catalog, where `brandId` is
required).

```java
// First ten local vouchers
ChariVoucherArticlesResponse local = chari.getLocalVouchers("0661231234");

// With filters
ChariVoucherArticlesResponse filtered = chari.getLocalVouchers(
        ChariVoucherCatalogQuery.builder()
                .phoneNumber("0661231234")
                .keyword("pubg")
                .page(1)
                .take(20)
                .build());
```

---

## 6. Preview and confirm a purchase

```java
ChariVoucherArticlesResponse.VoucherArticle article = articles.getData()
        .getCollection()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No voucher article available"));

ChariVoucherPurchasePayload purchase = ChariVoucherPurchasePayload.builder()
        .customerPhoneNumber("0661231234")
        .destinationPhoneNumber("0662345678")
        .beneficiaryName("Abdennour")
        .skuId(article.getSkuId())                 // required identifier
        .amount(new BigDecimal("50"))              // optional (variable-value vouchers)
        .providerSkuId(article.getProviderSkuId()) // optional / nullable
        .price(article.getPrice())                 // optional
        .providerId(article.getProviderId())       // required
        .build();

// Preview — no charge
ChariVoucherPreviewResponse preview = chari.previewVoucherPurchase(purchase);
BigDecimal fees = preview.getData().getFeesAmount();
BigDecimal total = preview.getData().getTotalAmount();

// Confirm only after the customer accepts the displayed total
ChariVoucherPurchaseResponse confirmed = chari.confirmVoucherPurchase(purchase);

String voucherCode = confirmed.getData().getCode();
String activationUrl = confirmed.getData().getUrlActivateCard();  // optional
String instructions = confirmed.getData().getDescription();
```

---

## 7. Service endpoint variant

The `service` endpoints accept the identical payload and return the same response
types. Use them when your integration is routed through the service operations
path.

```java
ChariVoucherPreviewResponse preview  = chari.previewServiceVoucherPurchase(purchase);
ChariVoucherPurchaseResponse confirmed = chari.purchaseServiceVoucher(purchase);
```

---

## 8. Data models

### `ChariVoucherPurchasePayload` (request)

| Field | Java type | Required | Description |
|---|---|---:|---|
| `customerPhoneNumber` | `String` | Yes | Chari customer who pays |
| `destinationPhoneNumber` | `String` | Yes | Voucher recipient |
| `beneficiaryName` | `String` | Yes | Recipient display name |
| `skuId` | `Integer` | Yes | Positive SKU — the purchase identifier |
| `amount` | `BigDecimal` | No | Face value for variable-value vouchers |
| `providerSkuId` | `String` | No | Optional provider SKU (nullable) |
| `price` | `BigDecimal` | No | Displayed price from the selected article |
| `providerId` | `Integer` | Yes | Provider ID from the selected article |

### `ChariVoucherArticlesResponse.VoucherArticle` (catalog item)

Returned by `getVoucherArticles`, `getVoucherProducts`, and `getLocalVouchers`.

| Getter | Description |
|---|---|
| `getSkuId()` | Voucher SKU identifier used for purchase |
| `getProviderSkuId()` | Optional provider article identifier |
| `getProductName()` | Display name |
| `getImageUrl()` | Optional image URL |
| `getPrice()` | Price as `BigDecimal` |
| `getDescription()` | Optional description |
| `getProviderId()` | Provider ID used for purchase |
| `getBrandId()` | Brand ID |
| `getPricing()` | Optional `VoucherPricing`, may be `null` |

`VoucherPricing`: `getMaxValueAmount()`, `getBaseValueAmount()`,
`getIsVariableValue()`, `getMultiplicateur()`.

### `ChariVoucherPreviewResponse` (preview)

| Getter from `preview.getData()` | Description |
|---|---|
| `getTypedOperationType()` | `ChariOperationType.VOUCHER` |
| `getOperation().getAmount()` | Voucher amount |
| `getFeesAmount()` | Fees |
| `getTotalAmount()` | Customer total |
| `getCheckedAt()` | Preview timestamp |
| `getOpenLoop()` | Open-loop indicator |

### `ChariVoucherPurchaseResponse` (confirmation — flat)

Read directly from `confirmed.getData()` — there is **no** nested `getOperation()`.

| Getter | Description |
|---|---|
| `getTypedOperationType()` | `ChariOperationType.VOUCHER` (from `operationType`) |
| `getVoucherName()` | Purchased voucher name |
| `getCode()` | Voucher code to deliver securely |
| `getDescription()` | Redemption instructions |
| `getAmount()` | Voucher amount |
| `getCashBack()` | Optional cashback |
| `getTotalAmount()` | Operation total |
| `getRecipientPhoneNumber()` | Recipient number |
| `getDestinationPhoneNumber()` | Recipient number |
| `getBeneficiaryName()` | Recipient name |
| `getUrlActivateCard()` | Optional activation URL |
| `getCheckedAt()` | Execution timestamp |

### `ChariVoucherProductResponse` (product detail)

`detail.getData()` exposes: `getCapProductId()`, `getCapDefaultUpc()`, `getName()`,
`getBlackhawkId()`, `getPriceUsd()`, `getPriceMadCurrent()`, `getProductConfigId()`,
`getProductDescription()`, `getProductImages()` (`ProductImage`:
`getId()`/`getImageSize()`/`getFrontImage()`), `getActivationCharacteristics()`
(`getMaxValueAmount()`/`getBaseValueAmount()`/`getIsVariableValue()`),
`getRedemptionCharacteristics()` (`getRedemptionOptions()`), and
`getTermsAndConditions()` (`TermsAndConditions`:
`getId()`/`getTermsAndConditions()`/`getTermsAndConditionsType()`).

---

## 9. Validation rules

The SDK validates inputs locally before any network call and throws
`IllegalArgumentException` on failure:

| Method group | Rule |
|---|---|
| Catalog (articles/brands) | `brandId` must be positive; `phoneNumber` must be a valid Moroccan mobile number |
| `getVoucherProducts` | `page`/`take`, when present, must be ≥ 1 |
| `getLocalVouchers` | `phoneNumber` must be valid; `page`/`take`, when present, must be ≥ 1 |
| `getVoucherProductDetail` | `configId` must be non-blank |
| Purchase (preview/confirm/service) | both phone numbers valid; `beneficiaryName` non-blank; `skuId` positive; `providerId` positive |

---

## 10. Error handling

```java
try {
    ChariVoucherPurchaseResponse confirmed = chari.confirmVoucherPurchase(purchase);
    deliver(confirmed.getData().getCode());
} catch (IllegalArgumentException e) {
    // Local validation failed (bad phone, missing skuId, …) — nothing was sent
    log.warn("Invalid voucher request: {}", e.getMessage());
} catch (ChariBaasException e) {
    // Provider rejected the request or returned an error
    log.error("Voucher provider error", e);
}
```

Always call **preview** first and show the fees and total before **confirm**.

---

## 11. Security notes

- Treat voucher `code` values as sensitive. **Never** log them.
- Deliver codes to the recipient over a secure channel.
- Do not log API keys, PINs, or activation URLs.

---

## 12. Method reference

| Method | Purpose |
|---|---|
| `getVoucherArticles(phone, brandId)` / `(query)` | List articles for a brand |
| `getVoucherBrands(phone, brandId)` / `(query)` | List brands |
| `getVoucherBrand(brandId, phone)` | Retrieve one brand |
| `getVouchersByBrand(brandId, phone)` | Articles that belong to a brand |
| `getVoucherProducts(page, take)` | Click Apporter / Blackhawk product catalog |
| `getVoucherProductDetail(configId)` | Detailed product info by config ID |
| `getLocalVouchers(phone)` / `(query)` | Local vouchers for a phone number |
| `previewVoucherPurchase(payload)` | Validate a purchase and calculate fees |
| `confirmVoucherPurchase(payload)` | Buy the voucher and receive its code |
| `previewServiceVoucherPurchase(payload)` | Preview via the service endpoint variant |
| `purchaseServiceVoucher(payload)` | Purchase via the service endpoint variant |

---

**Imports used in this guide**

```java
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherCatalogQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherPurchasePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherArticlesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherProductResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPurchaseResponse;
import java.math.BigDecimal;
import java.util.List;
```
