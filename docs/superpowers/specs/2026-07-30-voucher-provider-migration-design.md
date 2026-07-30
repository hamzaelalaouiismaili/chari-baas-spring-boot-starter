# Voucher Provider Migration — Design

**Date:** 2026-07-30
**Status:** Approved for planning

## Context

The voucher provider made breaking changes to its contract. The new contract is
captured in the Postman collection at `vouchers.json` (repo root). This migration
adapts the starter's voucher client, payloads, response models, facade, and tests
to the new contract. Decisions confirmed with the user:

- **Scope:** Full adaptation — fix the breaking changes to existing endpoints AND
  add all new endpoints from the collection.
- **Compatibility:** Clean replace. The old provider contract is dead, so old
  payload fields / response shapes / methods are replaced in place (no deprecated
  wrappers).
- **Purchase identifier:** `skuId` (int) is now the required identifier. `amount`,
  `price`, and `providerSkuId` are optional/nullable and passed through as-is.

Affected code lives under
`src/main/java/com/github/hamzaelalaouiismaili/chari/`:
- `client/api/ChariVoucherClient.java`
- `client/ChariBaasClient.java` (facade delegation)
- `model/payload/ChariVoucherPurchasePayload.java`
- `model/payload/ChariVoucherCatalogQuery.java`
- `model/response/ChariVoucherArticlesResponse.java`
- `model/response/ChariVoucherPurchaseResponse.java`
- `model/response/ChariVoucherPreviewResponse.java` (unchanged)
- `model/response/ChariVoucherProductResponse.java` (**new**)
- `src/test/java/.../client/ChariVoucherClientTest.java`

## Changes

### 1. Purchase payload — `ChariVoucherPurchasePayload` (breaking)

New request body (preview + confirm):
```json
{
  "customerPhoneNumber": "+212608814003",
  "destinationPhoneNumber": "+212608814003",
  "beneficiaryName": "string",
  "amount": 0,
  "skuId": 18,
  "providerSkuId": null,
  "price": 512.76,
  "providerId": 2
}
```

Fields:
- `String customerPhoneNumber` (required, valid Moroccan number)
- `String destinationPhoneNumber` (required, valid Moroccan number)
- `String beneficiaryName` (required, non-blank)
- `BigDecimal amount` (optional, passed through)
- `Integer skuId` (**required**, > 0) — new primary identifier
- `String providerSkuId` (optional, nullable; trimmed only if present)
- `BigDecimal price` (optional, passed through)
- `Integer providerId` (required, > 0)

`normalizePurchase` normalizes phone numbers, trims `beneficiaryName`, trims
`providerSkuId` only when non-null, and passes `amount`/`skuId`/`price`/`providerId`
through unchanged.

`validatePurchase` requires `skuId > 0` and `providerId > 0` (drops the old
`providerSkuId`-required check).

Nulls are serialized as-is (e.g. `providerSkuId:null`); no `@JsonInclude` filtering.

### 2. Confirm response — `ChariVoucherPurchaseResponse` (breaking)

The provider flattened the confirm response. Fields that were nested under
`data.operation` now sit directly on `data`:
```json
{
  "data": {
    "operationType": 23,
    "voucherName": "USD 50",
    "amount": 564.04,
    "cashBack": 36.66,
    "totalAmount": 512.76,
    "reason": null,
    "recipientPhoneNumber": "+212608814003",
    "checkedAt": "2026-07-30T15:05:59.8632851Z",
    "urlActivateCard": null,
    "destinationPhoneNumber": "+212608814003",
    "beneficiaryName": "string ",
    "code": "0qGNUOQWAWHVT1",
    "description": "..."
  }
}
```

`VoucherPurchaseData` fields: `operationType, voucherName, amount, cashBack,
totalAmount, reason, recipientPhoneNumber, checkedAt, urlActivateCard,
destinationPhoneNumber, beneficiaryName, code, description`. Add
`@JsonIgnore getTypedOperationType()` returning `ChariOperationType.fromCode(operationType)`.
Remove the nested `VoucherPurchaseOperation` class and the old
`type`/`operation`/`feesAmount`/`openLoop` wrapper.

### 3. Preview response — `ChariVoucherPreviewResponse` (unchanged)

Matches the collection (`data.type`, `data.operation{...}`, `feesAmount`,
`totalAmount`, `checkedAt`, `openLoop`). No change.

### 4. Catalog item — `VoucherArticle` (in `ChariVoucherArticlesResponse`)

Add:
- `Integer skuId`
- nested `VoucherPricing` (nullable):
  `BigDecimal maxValueAmount`, `BigDecimal baseValueAmount`,
  `Boolean isVariableValue`, `Integer multiplicateur`

This reused item model serves articles, local vouchers, and product lists.

### 5. Catalog query — `ChariVoucherCatalogQuery`

Add optional `String keyword`. `ChariVoucherClient.buildCatalogUrl` appends
`brandId` and `keyword` only when non-null (so local-vouchers may omit brandId,
while articles/brands still require it and keep identical URLs — existing
string-equality tests unaffected). Query-param order preserved:
`phoneNumber, brandId, page, take, keyword`.

### 6. New endpoints

| Facade method | Client method | HTTP | Response model |
|---|---|---|---|
| `getVoucherProducts(page, take)` | `getProducts(page, take)` | `GET /api/vouchers/product` | reuse `ChariVoucherArticlesResponse` |
| `getVoucherProductDetail(configId)` | `getProductDetail(configId)` | `GET /api/vouchers/products/{configId}` | **new** `ChariVoucherProductResponse` |
| `getLocalVouchers(query)` | `getLocalVouchers(query)` | `GET /api/vouchers?phoneNumber&page&take&brandId&keyword` | reuse `ChariVoucherArticlesResponse` |
| `previewServiceVoucherPurchase(payload)` | `previewServicePurchase(payload)` | `POST /api/operations/service/voucher/preview` | reuse `ChariVoucherPreviewResponse` |
| `purchaseServiceVoucher(payload)` | `purchaseServiceVoucher(payload)` | `POST /api/operations/service/voucher` | reuse `ChariVoucherPurchaseResponse` |

- `getProducts` takes optional `page`/`take` (no phone number); validates only that
  paging values, when present, are >= 1.
- `getLocalVouchers` requires a valid phone number; `brandId`/`keyword`/paging optional.
- `previewServicePurchase` / `purchaseServiceVoucher` reuse `normalizePurchase` +
  `validatePurchase`.

**New model `ChariVoucherProductResponse`** (`GET /api/vouchers/products/{configId}`):
```
data:
  Integer capProductId
  String capDefaultUpc
  String name
  String blackhawkId
  BigDecimal priceUsd
  BigDecimal priceMadCurrent
  String productConfigId
  String productDescription
  List<ProductImage> productImages        // { String id, imageSize, frontImage }
  ActivationCharacteristics activationCharacteristics
                                          // { BigDecimal maxValueAmount, baseValueAmount; Boolean isVariableValue }
  RedemptionCharacteristics redemptionCharacteristics
                                          // { List<String> redemptionOptions }
  List<TermsAndConditions> termsAndConditions
                                          // { String id, termsAndConditions, termsAndConditionsType }
```

### 7. Tests

- Rewrite `previewsAndConfirmsSameNormalizedPurchasePayload`: new request body
  (with `skuId`/`amount`/`price`) and flattened confirm response.
- Rewrite `rejectsInvalidCatalogAndPurchaseInputsLocally`: assert on the new
  required field (`skuId`) instead of `providerSkuId`.
- Add tests: `skuId` + `pricing` parsing on articles; product list; product
  detail; local vouchers (with/without brandId + keyword); service preview;
  service purchase.

### 8. Docs

Update voucher coverage in the integration guides
(`SERVICES_INTEGRATION_GUIDE.md` / `chari-baas-sdk.md` / `SDK_USAGE.md` — whichever
documents vouchers) to the new payload fields, flattened confirm response, and new
endpoints.

## Out of scope

- No changes to non-voucher clients.
- No `@JsonInclude` body-shaping unless the provider rejects null keys.
- `vouchers.json` (the Postman collection) is a reference artifact, left as-is.
