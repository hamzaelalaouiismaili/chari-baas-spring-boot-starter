# Merchant Card Lifecycle + Network + Fatourati APIs — Design

**Date:** 2026-07-30
**Status:** Approved for planning

## Context

Add the card authorization/settlement lifecycle to the merchant payment client
(the `autoCapture=false` flow), plus two adjacent sandbox/provider endpoints the
user requested together: network cash-in/out simulation and the Fatourati
cash-in request. Clean additions — no changes to existing endpoints.

Existing state:
- `ChariMerchantPaymentClient` has card **payment** (`/api/operations/merchant/payment/card`,
  supports `autoCapture`) and a **push** card preview (`/push/card/preview`), but
  no capture/reverse/refund.
- `ChariRequestOperationClient` owns cash-in/out **request** endpoints
  (`/api/operations/cashin/request`, etc.).
- `ChariCashinByReferenceResponse` already models the shape returned by the
  Fatourati request.
- `ChariMerchantCardPaymentPreviewResponse` already includes `data.operation.method`,
  so it is reused as-is for the new preview.

## A. Merchant card lifecycle (`ChariMerchantPaymentClient`)

Endpoints under `/api/operations/merchant/payment/card/`. Required scopes noted
for documentation only (the SDK sends the configured API key).

| Client method | Facade method | HTTP | Scope |
|---|---|---|---|
| `previewCardPaymentDirect(String phoneNumber, BigDecimal amount)` | `previewMerchantCardPaymentDirect` | `POST /card/preview?phoneNumber=` | operations:merchant-payment |
| `captureCardPayment(ChariMerchantCardCapturePayload)` | `captureMerchantCardPayment` | `POST /card/capture` | operations:merchant-payment |
| `reverseCardPayment(ChariMerchantCardCapturePayload)` | `reverseMerchantCardPayment` | `POST /card/reverse` | operations:merchant-payment |
| `refundCardPayment(ChariMerchantCardRefundPayload)` | `refundMerchantCardPayment` | `POST /card/refund` | operations:refund |

Naming: the existing `previewMerchantCardPayment` targets the **push** path and is
left untouched; the new `…Direct` method targets the non-push `/card/preview`.

### Request bodies

`previewCardPaymentDirect` — query `phoneNumber` (normalized), body `{ "amount": <amount> }`
(reuses the existing `amountPayload` helper). Returns `ChariMerchantCardPaymentPreviewResponse`.

**`ChariMerchantCardCapturePayload`** (shared by capture + reverse):
| Field | Java type | Required | JSON key |
|---|---|---:|---|
| `phoneNumber` | `String` | Yes | `phoneNumber` (normalized) |
| `amount` | `BigDecimal` | Yes | `amount` |
| `orderId` | `String` | Yes | `orderId` |
| `transactionTrackId` | `String` | Yes | `transactionTrackId` |
| `skipGatewayCall` | `Boolean` | No | `skipGatewayCall` (omitted when null) |

**`ChariMerchantCardRefundPayload`**:
| Field | Java type | Required | JSON key |
|---|---|---:|---|
| `phoneNumber` | `String` | Yes | `phoneNumber` (normalized) |
| `operationId` | `Long` | Yes | `operationId` |
| `refundAmount` | `BigDecimal` | Yes | `refundAmount` |
| `orderId` | `String` | Yes | `orderId` |
| `transactionTrackId` | `String` | Yes | `transactionTrackId` |

A `refundAmount` below the captured amount performs a partial refund (provider-side).

### Response — `ChariMerchantCardLifecycleResponse` (shared by capture/reverse/refund)

```
data:
  String phoneNumber
  Long operationId
  BigDecimal refundAmount
  String orderId
  String transactionTrackId
```

`@JsonInclude(NON_NULL)` on the class and nested data, matching the existing
merchant response DTOs.

### Validation (local, throws `IllegalArgumentException`)

- `previewCardPaymentDirect`: `phoneNumber` valid Moroccan number; `amount` non-null and > 0.
- capture/reverse: `phoneNumber` valid; `amount` > 0; `orderId` and `transactionTrackId` non-blank.
- refund: `phoneNumber` valid; `operationId` non-null and > 0; `refundAmount` > 0;
  `orderId` and `transactionTrackId` non-blank.

## B. Network cash-in/out simulation (new `ChariNetworkOperationClient`)

New client at `client/api/operations/ChariNetworkOperationClient.java`.

| Facade method | HTTP |
|---|---|
| `simulateNetworkCashin(ChariNetworkOperationPayload, Boolean withContext)` | `POST /api/network/operations/cashin[?withContext=]` |
| `simulateNetworkCashout(ChariNetworkOperationPayload, Boolean withContext)` | `POST /api/network/operations/cashout[?withContext=]` |

`withContext` is optional; when non-null it is appended as a query param.

**`ChariNetworkOperationPayload`**:
| Field | Java type | Required | JSON key |
|---|---|---:|---|
| `reference` | `String` | Yes | `reference` |
| `entity` | `String` | No | `entity` (omitted when null) |

**`ChariNetworkOperationResponse`**:
```
data:
  String reference
  String entity
  String createdAt
  String executedAt
  String phoneNumber
  BigDecimal amount
  String description
  String partner
```

Validation: `reference` non-blank.

## C. Fatourati cash-in request (`ChariRequestOperationClient`)

| Facade method | HTTP |
|---|---|
| `requestFatouratiCashin(ChariFatouratiCashinRequestPayload)` | `POST /api/operations/fatourati/cashin/request` |

**`ChariFatouratiCashinRequestPayload`**:
| Field | Java type | Required | JSON key |
|---|---|---:|---|
| `code` | `String` | Yes | `code` |
| `amount` | `BigDecimal` | Yes | `Amount` |
| `feesPercent` | `BigDecimal` | No | `FeesPercent` (omitted when null) |
| `description` | `String` | No | `Description` (omitted when null) |

Note the mixed key casing (`code` lowercase; `Amount`/`FeesPercent`/`Description`
capitalized) — built explicitly in a request map, matching the other clients'
`buildX` helpers. `code` replaces `phoneNumber` for principal agents.

Response reuses `ChariCashinByReferenceResponse` (its `data` already exposes
`reference, createdAt, executedAt, phoneNumber, code, amount, description, status, type`).

Validation: `code` non-blank; `amount` non-null and > 0.

## Wiring

`ChariBaasClient`:
- Add `private final ChariNetworkOperationClient networkOperationClient;` initialized
  in the constructor (`new ChariNetworkOperationClient(httpClient)`).
- Add delegating facade methods for every new operation (A, B, C).

## Testing

`MockRestServiceServer`-based tests asserting method + URL + request body + response
parsing for each endpoint:
- `ChariMerchantPaymentClientTest` (or the merchant section of `ChariBaasClientTest`):
  preview-direct, capture, reverse, refund (incl. partial), and a local-validation case.
- New `ChariNetworkOperationClientTest`: cashin, cashout, `withContext` query, reference-required validation.
- `ChariRequestOperationClient` coverage: Fatourati request body key casing + response parse.

## Docs

- Extend `MERCHANT_CARD_PAYMENT_GUIDE.md` with the authorize → capture/reverse → refund
  lifecycle (payloads, responses, partial refund, scope note).
- Add network simulation + Fatourati request notes to `SERVICES_INTEGRATION_GUIDE.md`.

## Out of scope

- No changes to existing payment/preview/QR methods.
- No webhook model changes (the `cashin.network.executed` / `cashout.network.executed`
  events are provider-emitted and handled by the existing webhook dispatcher).
