# Card APIs Guide — Cash-In & Merchant Payment Lifecycle

Reference for the four card endpoints exposed by `ChariBaasClient`, what each SDK
payload accepts, and which fields are required.

The optional fee and capture fields on the cash-in payloads are available since
**v1.0.25**; everything else predates it.

| Chari endpoint | SDK method | Payload |
|---|---|---|
| `POST /api/operations/cashin/card` | `executeCardFunding(String, ChariCardCashinPayload)` | `ChariCardCashinPayload` |
| `POST /api/operations/merchant/payment/card` | `executeMerchantCardPayment(String, ChariMerchantCardPaymentPayload)` | `ChariMerchantCardPaymentPayload` |
| `POST /api/operations/merchant/payment/card/capture` | `captureMerchantCardPayment(ChariMerchantCardCapturePayload)` | `ChariMerchantCardCapturePayload` |
| `POST /api/operations/merchant/payment/card/reverse` | `reverseMerchantCardPayment(ChariMerchantCardCapturePayload)` | `ChariMerchantCardCapturePayload` |

**Cash-in vs merchant payment:** cash-in *funds a customer wallet* from a card.
Merchant payment *charges a card and credits a merchant*. Only the merchant flow
has a capture/reverse lifecycle.

---

## Conventions that apply to every call

**Phone numbers** are normalized by the SDK before being sent — pass
`0612345678`, `612345678`, `+212612345678`, or `00212612345678` and Chari always
receives `+212612345678`. A blank or null number throws `IllegalArgumentException`.

**Authentication headers** (`Chari-Api-Key`, a generated `C-Request-Id`, and the
configured browser headers) are added automatically. You never set them yourself.

**Optional fields are omitted, not nulled.** Any field documented as optional
below is left out of the JSON body entirely when you don't set it, so existing
integrations keep sending byte-identical bodies — unless that field's row says
otherwise (a few fields carry defaults and are always sent).

**Errors** surface as `ChariBaasException` (`getHttpStatusCode()`,
`getErrorCode()`, `getErrorDescription()`, `getStage()`). Client-side validation
failures throw `IllegalArgumentException` before any HTTP call is made.

---

## 1. Card cash-in — `POST /api/operations/cashin/card`

```java
ChariCardFundingPreviewResponse preview =
        chari.previewCardFunding("0612345678", new BigDecimal("100"));

ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("4918914107195005")
        .expiryDate("08/26")
        .cvv("123")
        .amount(new BigDecimal("100"))
        // --- optional ---
        .cardName("my_saved_card")
        .keepAlive(true)
        .feesPercent(new BigDecimal("1.5"))
        .internationalFeesPercent(new BigDecimal("2.5"))
        .threeDSecure(true)
        .autoCapture(true)
        .allowInternationalCards(true)
        .notificationUrl("https://merchant.example.com/webhook")
        .externalReference("ORDER-1001")
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

ChariCardFundingExecutionResponse response =
        chari.executeCardFunding("0612345678", payload);

if (Boolean.TRUE.equals(response.getData().getRedirect())) {
    // send the customer to response.getData().getRedirectionURL() for the 3DS challenge
}
```

### `ChariCardCashinPayload` fields

| Builder field | Type | Required | Wire key | Notes |
|---|---|---|---|---|
| `firstName` | `String` | **Yes** | `firstName` | |
| `lastName` | `String` | **Yes** | `lastName` | |
| `pan` | `String` | **Yes** | `pan` | Card number |
| `expiryDate` | `String` | **Yes** | `expiryDate` | Accepts `MM/YY`; the SDK converts it to `YYMM`. Passing `2608` directly also works |
| `cvv` | `String` | **Yes** | `cvv` | |
| `amount` | `BigDecimal` | **Yes** | `amount` | |
| `currency` | `String` | No | `currency` | Defaults to `"MAD"` — always sent |
| `keepAlive` | `Boolean` | No | `keepAlive` | Defaults to `false` — always sent. `true` tokenizes the card for later reuse |
| `cardName` | `String` | No | `cardName` | Label for the tokenized card; only meaningful with `keepAlive(true)` |
| `feesPercent` | `BigDecimal` | No | `feesPercent` | Fee percentage applied to the operation |
| `internationalFeesPercent` | `BigDecimal` | No | `internationalFeesPercent` | Fee percentage for international cards |
| `threeDSecure` | `Boolean` | No | **`3dSecure`** | Java can't name a field starting with a digit, hence the rename |
| `autoCapture` | `Boolean` | No | `autoCapture` | |
| `allowInternationalCards` | `Boolean` | No | `allowInternationalCards` | |
| `notificationUrl` | `String` | No | `notificationUrl` | Server-to-server callback |
| `externalReference` | `String` | No | `externalReference` | Echoed back on the response |
| `acceptUrl` / `acceptURL` | `String` | No | **`acceptURL`** | See redirect-URL resolution below |
| `declineUrl` / `declineURL` | `String` | No | **`declineURL`** | See redirect-URL resolution below |
| `idempotencyKey` | `String` | No | *(not sent)* | The field exists on the payload but is **not** transmitted by this endpoint |

The builder exposes both `acceptUrl(...)`/`acceptURL(...)` spellings — they set
the same field.

### Redirect-URL resolution

`acceptURL` / `declineURL` are resolved in this order:

1. the value on the payload, else
2. `chari.baas.card-funding.accept-url` / `.decline-url` from configuration, else
3. omitted from the body entirely.

> **Casing note:** this endpoint sends **`acceptURL` / `declineURL`** (uppercase
> `URL`), while the merchant endpoints send **`acceptUrl` / `declineUrl`**. That
> asymmetry is deliberate in the SDK and covered by tests. If Chari's cash-in
> endpoint turns out to be case-sensitive and expects `acceptUrl`, this is the
> one thing to verify in sandbox.

### Response — `ChariCardFundingExecutionResponse.getData()`

`redirect`, `amount`, `transactionTrackId`, `orderId`, `transactionReferenceId`,
`redirectionURL`, `acceptURL`, `declineURL`, `status`, `feesAmount`,
`totalAmount`, `externalReference`.

After the 3DS challenge, validate `RESPONSE_CODE` and `REASON_CODE` from the
redirect URL in your own controller.

### Variants of the same payload

```java
// By agent code instead of phone number — POST /api/operations/cashin/card/agent?code=...
chari.executeCardFundingByAgent("11023", payload);

// With an already-tokenized card — POST /api/operations/cashin/card/{cardId}?phoneNumber=...
ChariSavedCardCashinPayload saved = ChariSavedCardCashinPayload.builder()
        .cvv("123")                       // required
        .amount(new BigDecimal("200"))    // required
        .feesPercent(new BigDecimal("1.5"))
        .autoCapture(true)
        .allowInternationalCards(true)
        .externalReference("ORDER-1002")
        .build();
chari.cashinWithSavedCard(123, "0612345678", saved);
```

`ChariSavedCardCashinPayload` requires only `cvv` and `amount`; it accepts the
same optional set as above (`feesPercent`, `internationalFeesPercent`,
`threeDSecure`, `autoCapture`, `allowInternationalCards`, `notificationUrl`,
`externalReference`, `acceptUrl`, `declineUrl`) with identical wire keys and the
same "omitted when unset" rule.

> The optional fee/capture fields on the **saved-card** endpoint mirror the
> tokenized merchant endpoint; Chari's acceptance of them there has not been
> confirmed against sandbox. Sending nothing extra is always safe.

---

## 2. Merchant card payment — `POST /api/operations/merchant/payment/card`

```java
ChariMerchantCardPaymentPreviewResponse preview =
        chari.previewMerchantCardPayment("0612345678", new BigDecimal("250.00"));

ChariMerchantCardPaymentPayload payload = ChariMerchantCardPaymentPayload.builder()
        .firstName("John")
        .lastName("Doe")
        .pan("4111111111111111")
        .expiryDate("2608")
        .cvv("123")
        .amount(new BigDecimal("250"))
        // --- optional ---
        .keepAlive(true)
        .currency("MAD")
        .threeDSecure(true)
        .autoCapture(false)              // authorize now, capture later
        .feesPercent(new BigDecimal("1.5"))
        .internationalFeesPercent(new BigDecimal("2.5"))
        .allowInternationalCards(true)
        .cardName("John Doe Visa")
        .notificationUrl("https://merchant.example.com/webhook")
        .acceptUrl("https://merchant.example.com/success")
        .declineUrl("https://merchant.example.com/failure")
        .externalReference("ORDER-1001")
        .build();

ChariMerchantCardPaymentResponse response =
        chari.executeMerchantCardPayment("0612345678", payload);
```

The `phoneNumber` argument is the **merchant's** number and goes in the query
string, not the body.

### `ChariMerchantCardPaymentPayload` fields

| Builder field | Type | Required | Wire key | Notes |
|---|---|---|---|---|
| `firstName` | `String` | **Yes** | `firstName` | |
| `lastName` | `String` | **Yes** | `lastName` | |
| `pan` | `String` | **Yes** | `pan` | |
| `expiryDate` | `String` | **Yes** | `expiryDate` | Sent **verbatim** — unlike cash-in there is no `MM/YY` conversion, so pass `YYMM` |
| `cvv` | `String` | **Yes** | `cvv` | |
| `amount` | `BigDecimal` | **Yes** | `amount` | |
| `keepAlive` | `Boolean` | No | `keepAlive` | Always included in the body — set it explicitly. `true` tokenizes the card and returns `tokenizedCardId` |
| `currency` | `String` | No | `currency` | No default here — omitted when unset |
| `threeDSecure` | `Boolean` | No | **`3dSecure`** | |
| `feesPercent` | `BigDecimal` | No | `feesPercent` | |
| `internationalFeesPercent` | `BigDecimal` | No | `internationalFeesPercent` | |
| `allowInternationalCards` | `Boolean` | No | `allowInternationalCards` | |
| `autoCapture` | `Boolean` | No | `autoCapture` | `false` authorizes only — you must then capture or reverse |
| `notificationUrl` | `String` | No | `notificationUrl` | |
| `acceptUrl` | `String` | No | `acceptUrl` | Lowercase `Url` — no config fallback on this endpoint |
| `declineUrl` | `String` | No | `declineUrl` | Lowercase `Url` — no config fallback on this endpoint |
| `cardName` | `String` | No | `cardName` | |
| `externalReference` | `String` | No | `externalReference` | |

### Response — `ChariMerchantCardPaymentResponse.getData()`

`redirect`, `responseCode`, `amount`, `transactionTrackId`, `orderId`,
`transactionReferenceId`, `redirectionURL`, `acceptURL`, `declineURL`,
`gateway`, `operationId`, `operationDate`, `feesAmount`, `externalReference`,
`tokenizedCardId`.

**Keep `orderId` and `transactionTrackId`** — capture and reverse both need them.

### Tokenized variant

`executeMerchantTokenizedCardPayment(Integer cardId, String phoneNumber,
ChariMerchantTokenizedCardPaymentPayload payload)` charges a saved card.
Required: `cvv`, `amount`. Optional: the same fee/capture/URL/reference set as
above (no card fields, since the card is already on file).

---

> **Scopes:** capture and reverse require `operations:merchant-payment`; refund
> requires the distinct `operations:refund` scope.

## 3. Capture — `POST /api/operations/merchant/payment/card/capture`

Settles an authorization taken with `autoCapture(false)`.

```java
ChariMerchantCardCapturePayload capture = ChariMerchantCardCapturePayload.builder()
        .phoneNumber("0612345678")
        .amount(new BigDecimal("250"))
        .orderId(response.getData().getOrderId())
        .transactionTrackId(response.getData().getTransactionTrackId())
        .build();

ChariMerchantCardLifecycleResponse result =
        chari.captureMerchantCardPayment(capture);
```

## 4. Reverse — `POST /api/operations/merchant/payment/card/reverse`

Releases an authorization that was never captured.

```java
ChariMerchantCardLifecycleResponse result =
        chari.reverseMerchantCardPayment(capture);   // same payload type
```

### `ChariMerchantCardCapturePayload` — shared by capture and reverse

| Builder field | Type | Required | Wire key | Notes |
|---|---|---|---|---|
| `phoneNumber` | `String` | **Yes** | `phoneNumber` | The number tied to the original payment — **in the body** here, not the query string. Normalized to `+212…` |
| `amount` | `BigDecimal` | **Yes** | `amount` | Must be positive |
| `orderId` | `String` | **Yes** | `orderId` | From the payment response |
| `transactionTrackId` | `String` | **Yes** | `transactionTrackId` | From the payment response |
| `skipGatewayCall` | `Boolean` | No | `skipGatewayCall` | Omitted when unset |

Both methods validate **before** sending and throw `IllegalArgumentException`
when: the payload is null, the phone number is not a valid Moroccan mobile
(`+212` followed by 5/6/7 and 8 digits), the amount is null or ≤ 0, or `orderId`
/ `transactionTrackId` is null or blank.

### Response — `ChariMerchantCardLifecycleResponse.getData()`

`phoneNumber`, `operationId`, `refundAmount`, `orderId`, `transactionTrackId`.

### Related: refund

`refundMerchantCardPayment(ChariMerchantCardRefundPayload)` hits
`/api/operations/merchant/payment/card/refund` and refunds a payment that was
already **captured**. It requires `phoneNumber`, `operationId` (positive),
`refundAmount` (positive), `orderId`, and `transactionTrackId`; a `refundAmount`
below the captured amount performs a partial refund. Same response DTO.

---

## Choosing capture, reverse, or refund

| Transaction state | Use |
|---|---|
| Authorized, not captured — you want the money | `captureMerchantCardPayment` |
| Authorized, not captured — you want to cancel | `reverseMerchantCardPayment` |
| Already captured — you want to give money back | `refundMerchantCardPayment` |

A typical deferred-capture flow:

```java
// 1. authorize only
payload.setAutoCapture(false);
ChariMerchantCardPaymentResponse auth = chari.executeMerchantCardPayment(phone, payload);

String orderId = auth.getData().getOrderId();
String trackId = auth.getData().getTransactionTrackId();

// 2. ... goods shipped / order cancelled ...

ChariMerchantCardCapturePayload lifecycle = ChariMerchantCardCapturePayload.builder()
        .phoneNumber(phone).amount(auth.getData().getAmount())
        .orderId(orderId).transactionTrackId(trackId)
        .build();

// either:
chari.captureMerchantCardPayment(lifecycle);   // ship → take the money
// or:
chari.reverseMerchantCardPayment(lifecycle);   // cancel → release the hold
```

---

## Wire-key differences at a glance

Where the SDK field name and the JSON key differ, or where the two endpoints
disagree:

| | Cash-in card | Merchant card |
|---|---|---|
| 3DS flag | `threeDSecure` → `"3dSecure"` | `threeDSecure` → `"3dSecure"` |
| Redirect URLs | `"acceptURL"` / `"declineURL"` | `"acceptUrl"` / `"declineUrl"` |
| Redirect URL config fallback | `chari.baas.card-funding.*` | none |
| Expiry format | `MM/YY` accepted, converted to `YYMM` | sent verbatim |
| `currency` default | `"MAD"` | none |
| `keepAlive` default | `false` | none — set it explicitly |

---

## Error handling

```java
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariBaasException;

try {
    chari.executeCardFunding("0612345678", payload);
} catch (IllegalArgumentException ex) {
    // Client-side: bad phone number, non-positive amount, missing orderId, ...
} catch (ChariBaasException ex) {
    Integer httpStatus = ex.getHttpStatusCode();
    Integer errorCode = ex.getErrorCode();
    String description = ex.getErrorDescription();
    String stage = ex.getStage();   // EXECUTE_CARD_FUNDING, CAPTURE_MERCHANT_CARD_PAYMENT, ...
}
```

Keep `chari.baas.audit.mask-sensitive: true` so PAN and CVV are masked in SDK
request logs.

## Sandbox test card

```text
PAN:     4918914107195005
CVV:     123
Expiry:  08/26 (or any future date)
3DS code: 555
```

The 3DS code is entered on the redirected challenge page — never in the request
body.

## See also

- [MERCHANT_CARD_PAYMENT_GUIDE.md](MERCHANT_CARD_PAYMENT_GUIDE.md) — preview/execute walkthrough with full response DTO listings
- [README.md](README.md) — full endpoint catalogue
- [SDK_USAGE.md](SDK_USAGE.md) — configuration and quick snippets
