# Merchant Card Payment Quick Guide

This guide covers the two card-payment methods exposed by `ChariBaasClient`:

```java
ChariMerchantCardPaymentPreviewResponse previewMerchantCardPayment(
        String phoneNumber,
        BigDecimal amount);

ChariMerchantCardPaymentResponse executeMerchantCardPayment(
        String phoneNumber,
        ChariMerchantCardPaymentPayload payload);
```

The normal flow is:

1. Preview the payment to obtain the calculated fees and payment information.
2. Show the preview to the customer.
3. Execute the payment after the customer confirms.
4. If the execution response requires a redirect, send the customer to the returned URL.

## Packages and imports

```java
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantCardPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentResponse;

import java.math.BigDecimal;
```

In a Spring Boot application, inject the auto-configured client:

```java
@Service
public class CardPaymentService {

    private final ChariBaasClient chari;

    public CardPaymentService(ChariBaasClient chari) {
        this.chari = chari;
    }
}
```

## 1. Preview a card payment

```java
ChariMerchantCardPaymentPreviewResponse preview =
        chari.previewMerchantCardPayment(
                "0612345678",
                new BigDecimal("250.00"));

BigDecimal fees = preview.getData().getFeesAmount();
BigDecimal total = preview.getData().getTotalAmount();
Boolean openLoop = preview.getData().getOpenLoop();
```

The SDK normalizes a local Moroccan phone number such as `0612345678` to
`+212612345678` before calling the API.

### Preview HTTP payload

The phone number is sent as the `phoneNumber` query parameter. The request body contains only the amount:

```http
POST /api/operations/merchant/payment/push/card/preview?phoneNumber=+212612345678
Content-Type: application/json
```

```json
{
  "amount": 250.00
}
```

### Preview response DTO

`ChariMerchantCardPaymentPreviewResponse` contains a `data` object with these fields:

| Java field | Type | Meaning |
| --- | --- | --- |
| `type` | `Integer` | Numeric operation type. |
| `operation` | `MerchantCardPaymentPreviewOperation` | The operation being previewed. |
| `feesAmount` | `BigDecimal` | Calculated fees. |
| `totalAmount` | `BigDecimal` | Total amount when supplied by the API. |
| `checkedAt` | `String` | Timestamp at which the preview was calculated. |
| `openLoop` | `Boolean` | Whether the operation is open-loop. |

`operation` contains:

| Java field | Type | Meaning |
| --- | --- | --- |
| `phoneNumber` | `String` | Normalized merchant/customer phone number returned by the API. |
| `amount` | `BigDecimal` | Payment amount. |
| `method` | `Integer` | Numeric payment-method code. |

Use `preview.getData().getTypedOperationType()` to convert `type` to the
`ChariOperationType` enum when the code is known.

Example response:

```json
{
  "data": {
    "type": 5,
    "operation": {
      "phoneNumber": "+212612345678",
      "amount": 250,
      "method": 2
    },
    "feesAmount": 0,
    "totalAmount": 250,
    "checkedAt": "2025-04-12T12:55:39.213Z",
    "openLoop": false
  }
}
```

## 2. Execute the card payment

Create a `ChariMerchantCardPaymentPayload`, then pass it with the same phone number:

```java
ChariMerchantCardPaymentPayload payload = ChariMerchantCardPaymentPayload.builder()
        .firstName("John")
        .lastName("Doe")
        .cvv("123")
        .amount(new BigDecimal("250.00"))
        .pan("4111111111111111")
        .expiryDate("2608")
        .keepAlive(false)
        .currency("MAD")
        .threeDSecure(true)
        .autoCapture(true)
        .notificationUrl("https://merchant.example.com/webhooks/chari")
        .acceptUrl("https://merchant.example.com/payments/success")
        .declineUrl("https://merchant.example.com/payments/declined")
        .cardName("Personal card")
        .externalReference("ORDER-1001")
        .build();

ChariMerchantCardPaymentResponse response =
        chari.executeMerchantCardPayment("0612345678", payload);

if (Boolean.TRUE.equals(response.getData().getRedirect())) {
    String redirectUrl = response.getData().getRedirectionURL();
    // Return this URL to the frontend so the customer can complete the flow.
} else {
    Long operationId = response.getData().getOperationId();
    // The payment was handled without a customer redirect.
}
```

Use test card data only in the sandbox. Never hard-code or log real PAN or CVV values.

### Execution payload DTO

`ChariMerchantCardPaymentPayload` is in
`com.github.hamzaelalaouiismaili.chari.model.payload`.

| Java field | JSON field | Type | Serialization behavior |
| --- | --- | --- | --- |
| `firstName` | `firstName` | `String` | Always included. |
| `lastName` | `lastName` | `String` | Always included. |
| `cvv` | `cvv` | `String` | Always included; sensitive card data. |
| `amount` | `amount` | `BigDecimal` | Always included. |
| `pan` | `pan` | `String` | Always included; sensitive card data. |
| `expiryDate` | `expiryDate` | `String` | Always included and forwarded unchanged. |
| `keepAlive` | `keepAlive` | `Boolean` | Always included. |
| `currency` | `currency` | `String` | Included when non-null. |
| `threeDSecure` | `3dSecure` | `Boolean` | Included when non-null. Note the different JSON name. |
| `feesPercent` | `feesPercent` | `BigDecimal` | Included when non-null. |
| `allowInternationalCards` | `allowInternationalCards` | `Boolean` | Included when non-null. |
| `internationalFeesPercent` | `internationalFeesPercent` | `BigDecimal` | Included when non-null. |
| `autoCapture` | `autoCapture` | `Boolean` | Included when non-null. |
| `notificationUrl` | `notificationUrl` | `String` | Included when non-null. |
| `acceptUrl` | `acceptUrl` | `String` | Included when non-null. |
| `declineUrl` | `declineUrl` | `String` | Included when non-null. |
| `cardName` | `cardName` | `String` | Included when non-null. |
| `externalReference` | `externalReference` | `String` | Included when non-null; use your order/payment reference. |

The DTO does not define Bean Validation annotations. “Always included” describes
the current SDK serialization logic, not whether the remote API accepts a null value.
Supply all fields required by your Chari account and payment flow. The SDK sends
`expiryDate` exactly as provided, so use the format required by the configured gateway.

### Execution HTTP payload

```http
POST /api/operations/merchant/payment/card?phoneNumber=+212612345678
Content-Type: application/json
```

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "cvv": "123",
  "amount": 250.00,
  "pan": "4111111111111111",
  "expiryDate": "2608",
  "keepAlive": false,
  "currency": "MAD",
  "3dSecure": true,
  "autoCapture": true,
  "notificationUrl": "https://merchant.example.com/webhooks/chari",
  "acceptUrl": "https://merchant.example.com/payments/success",
  "declineUrl": "https://merchant.example.com/payments/declined",
  "cardName": "Personal card",
  "externalReference": "ORDER-1001"
}
```

### Execution response DTO

`ChariMerchantCardPaymentResponse` is in
`com.github.hamzaelalaouiismaili.chari.model.response`. Its `data` object contains:

| Java field | Type | Meaning |
| --- | --- | --- |
| `redirect` | `Boolean` | Whether the customer must be redirected. |
| `responseCode` | `Integer` | Gateway/API response code. |
| `amount` | `BigDecimal` | Executed payment amount. |
| `transactionTrackId` | `String` | Gateway transaction tracking ID. |
| `orderId` | `String` | Chari/gateway order ID. |
| `transactionReferenceId` | `String` | Transaction reference returned by the gateway. |
| `redirectionURL` | `String` | URL to which the customer must be sent when `redirect` is `true`. |
| `acceptURL` | `String` | Success return URL reported by the API. |
| `declineURL` | `String` | Decline return URL reported by the API. |
| `gateway` | `String` | Payment gateway name. |
| `operationId` | `Long` | Chari operation ID when immediately available. |
| `operationDate` | `String` | Operation timestamp when immediately available. |
| `feesAmount` | `BigDecimal` | Applied fees when available. |
| `externalReference` | `String` | Your external reference when returned by the API. |

Example redirect response:

```json
{
  "data": {
    "redirect": true,
    "responseCode": 0,
    "amount": 250,
    "transactionTrackId": "600789381213",
    "orderId": "CH473bbe51d546",
    "transactionReferenceId": "5852",
    "redirectionURL": "https://payment-gateway.example/3ds",
    "acceptURL": "https://merchant.example.com/payments/success",
    "declineURL": "https://merchant.example.com/payments/declined",
    "gateway": "CHARIPAY",
    "operationId": null,
    "operationDate": null,
    "feesAmount": null,
    "externalReference": "ORDER-1001"
  }
}
```

Do not assume that `operationId`, `operationDate`, or `feesAmount` is always present;
a redirect response can return them as `null` until the payment flow completes.

## 3. Authorize, capture, reverse, and refund (lifecycle)

To decouple authorization from settlement, execute the card payment (step 2) with
`autoCapture = false`. The funds are authorized without being debited, then you
finalize the transaction — targeting it via the `orderId` and `transactionTrackId`
returned by the payment.

Typical flow:

```
card payment (autoCapture = false) → authorization
    ├── capture   → debit the authorized funds
    ├── reverse   → release an uncaptured authorization (order abandoned)
    └── refund    → return funds on an already-captured payment (full or partial)
```

> **Scopes:** capture and reverse require `operations:merchant-payment`; refund
> requires the distinct `operations:refund` scope.

### Direct preview

A preview against the non-push card endpoint (`/api/operations/merchant/payment/card/preview`):

```java
ChariMerchantCardPaymentPreviewResponse preview =
        chari.previewMerchantCardPaymentDirect("0612345678", new BigDecimal("250"));
```

Returns the same `ChariMerchantCardPaymentPreviewResponse` as step 1.

### Capture

```java
ChariMerchantCardLifecycleResponse captured = chari.captureMerchantCardPayment(
        ChariMerchantCardCapturePayload.builder()
                .phoneNumber("0612345678")
                .amount(new BigDecimal("250"))
                .orderId("CH473bbe51d546")
                .transactionTrackId("600789381213")
                .skipGatewayCall(false)   // optional
                .build());
```

`POST /api/operations/merchant/payment/card/capture`

### Reverse

An abandoned order releases the authorized funds without debiting. The request body
is identical to capture. Use reverse for an **uncaptured** authorization; for an
already-captured payment, use refund instead.

```java
ChariMerchantCardLifecycleResponse reversed = chari.reverseMerchantCardPayment(
        ChariMerchantCardCapturePayload.builder()
                .phoneNumber("0612345678")
                .amount(new BigDecimal("250"))
                .orderId("CH473bbe51d546")
                .transactionTrackId("600789381213")
                .build());
```

`POST /api/operations/merchant/payment/card/reverse`

### Refund

Refunds an already-captured payment, identified by `operationId`. A `refundAmount`
lower than the captured amount performs a **partial refund**.

```java
ChariMerchantCardLifecycleResponse refunded = chari.refundMerchantCardPayment(
        ChariMerchantCardRefundPayload.builder()
                .phoneNumber("0612345678")
                .operationId(5231L)
                .refundAmount(new BigDecimal("100"))   // partial: below captured amount
                .orderId("CH473bbe51d546")
                .transactionTrackId("600789381213")
                .build());
```

`POST /api/operations/merchant/payment/card/refund`

### Lifecycle response DTO

Capture, reverse, and refund all return `ChariMerchantCardLifecycleResponse`:

| Getter from `response.getData()` | Description |
|---|---|
| `getPhoneNumber()` | Merchant phone number |
| `getOperationId()` | Operation ID (`Long`) |
| `getRefundAmount()` | Settled/refunded amount (`BigDecimal`) |
| `getOrderId()` | Order identifier |
| `getTransactionTrackId()` | Gateway transaction track ID |

### Payload fields

**`ChariMerchantCardCapturePayload`** (capture + reverse):

| Field | Type | Required |
|---|---|---:|
| `phoneNumber` | `String` | Yes |
| `amount` | `BigDecimal` | Yes |
| `orderId` | `String` | Yes |
| `transactionTrackId` | `String` | Yes |
| `skipGatewayCall` | `Boolean` | No |

**`ChariMerchantCardRefundPayload`** (refund):

| Field | Type | Required |
|---|---|---:|
| `phoneNumber` | `String` | Yes |
| `operationId` | `Long` | Yes |
| `refundAmount` | `BigDecimal` | Yes |
| `orderId` | `String` | Yes |
| `transactionTrackId` | `String` | Yes |

## Error handling

API, timeout, connection, and response-parsing failures are exposed as
`ChariBaasException`:

```java
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariBaasException;

try {
    ChariMerchantCardPaymentResponse response =
            chari.executeMerchantCardPayment(phoneNumber, payload);
} catch (IllegalArgumentException ex) {
    // Missing or blank phone number.
} catch (ChariBaasException ex) {
    Integer httpStatus = ex.getHttpStatusCode();
    Integer errorCode = ex.getErrorCode();
    String description = ex.getErrorDescription();
    String stage = ex.getStage(); // EXECUTE_MERCHANT_CARD_PAYMENT
}
```

The SDK automatically adds the configured `Chari-Api-Key` and a generated
`C-Request-Id` header. Keep `chari.baas.audit.mask-sensitive: true` when audit
logging is enabled so PAN and CVV values are masked in SDK request logs.


git push origin master            

git tag -a v1.0.15 -m "Release v1.0.15"

git push origin v1.0.15

git show v1.0.15 --stat 

https://jitpack.io/#hamzaelalaouiismaili/chari-baas-spring-boot-starter/v1.0.15

https://jitpack.io/#hamzaelalaouiismaili/chari-baas-spring-boot-starter