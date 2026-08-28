# Chari BaaS Spring Boot Starter

Spring Boot starter for integrating with the Chari Banking-as-a-Service HTTP API.

Official ChariBaaS API documentation: https://baas.ma/en/api-docs

For complete `ChariBaasClient` examples, see [SDK_USAGE.md](SDK_USAGE.md).
For webhook implementation examples, see [WEBHOOK_USAGE.md](WEBHOOK_USAGE.md).
For personal and merchant KYC/KYB upgrade flows, see [KYC_UPGRADE_GUIDE.md](KYC_UPGRADE_GUIDE.md).
For testing the request-operations listing (`GET /api/operations/requests`), see [REQUEST_OPERATIONS_TESTING.md](REQUEST_OPERATIONS_TESTING.md).


## Installation

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.hamzaelalaouiismaili</groupId>
    <artifactId>chari-baas-spring-boot-starter</artifactId>
    <version>v1.0.24</version>
</dependency>
```

## Configuration

```yaml
chari:
  baas:
    base-url: https://api.chari.ma
    api-key: ${CHARI_API_KEY}
    webhook-secret: ${CHARI_WEBHOOK_SECRET:}
    timeout-ms: 10000
    principal-agent-id: "11098"
    principal-agent-rib: "007123456789012345678901"
    webhook:
      enabled: true
      path: /webhooks/chari
    audit:
      enabled: true
      mask-sensitive: true
```

| Property                              | Required | Default           | Description                                                           |
| ------------------------------------- | -------: | ----------------- | --------------------------------------------------------------------- |
| `chari.baas.base-url`                 |      Yes |                   | Chari BaaS API base URL.                                              |
| `chari.baas.api-key`                  |      Yes |                   | API key sent as the `Chari-Api-Key` header.                           |
| `chari.baas.webhook-secret`           |       No |                   | HMAC-SHA256 secret for `x-chari-signature`. Blank skips verification. |
| `chari.baas.timeout-ms`               |       No | `10000`           | HTTP connect and read timeout.                                        |
| `chari.baas.principal-agent-id`       |       No |                   | Principal agent ID sent as `/api/agents/principal/{id}`.              |
| `chari.baas.principal-agent-rib`      |       No |                   | Default RIB used by AP bank-transfer methods.                         |
| `chari.baas.card-funding.accept-url`  |       No |                   | Default 3DS success return URL for card cash-in execution.            |
| `chari.baas.card-funding.decline-url` |       No |                   | Default 3DS failure return URL for card cash-in execution.            |
| `chari.baas.webhook.enabled`          |       No | `true`            | Registers the webhook controller when enabled.                        |
| `chari.baas.webhook.path`             |       No | `/webhooks/chari` | Path for webhook POST requests.                                       |
| `chari.baas.audit.enabled`            |       No | `true`            | Logs structured Chari request/response audit records.                 |
| `chari.baas.audit.mask-sensitive`     |       No | `true`            | Masks PAN, CVV, PIN, and expiry values in audit logs.                 |
| `chari.baas.audit.format`             |       No | `banner`          | Audit log rendering: `banner` (multi-line ASCII box) or `kv` (single-line logfmt, Loki-friendly). |

## Client Usage

Every SDK request includes:

```text
Chari-Api-Key: <configured API key>
C-Request-Id: <generated UUID v4>
```

## Sandbox Card

Use Chari's sandbox test card for card funding and saved-card flows:

| Field    | Value                      |
| -------- | -------------------------- |
| PAN      | `4918914107195005`         |
| CVV      | `123`                      |
| Expiry   | `08/26` or any future date |
| 3DS code | `555`                      |

`ChariCardCashinPayload.expiryDate` accepts `MM/YY`; the SDK sends it to Chari as `YYMM`. The `3DS code` is entered on the redirected 3DS challenge page, not included in the initial SDK request body.

```java
@Service
public class PaymentService {

    private final ChariBaasClient chari;

    public PaymentService(ChariBaasClient chari) {
        this.chari = chari;
    }

    public ChariTransferResponse sendMoney(String from, String to, BigDecimal amount) {
        ChariTransferPayload payload = ChariTransferPayload.builder()
                .customerPhoneNumber(from)
                .recipientPhoneNumber(to)
                .amount(amount)
                .reason("Payment")
                .idempotencyKey(UUID.randomUUID().toString())
                .build();
        return chari.executeTransfer(payload);
    }
}
```

## Webhooks

Implement `ChariWebhookHandler` as a Spring bean. Every method is a default no-op, so override only what your application needs.

```java
@Component
public class MyChariWebhookHandler implements ChariWebhookHandler {

    @Override
    public void onCashIn(WebhookData data) {
        // credit local ledger
    }

    @Override
    public void onBankTransfer(WebhookData data) {
        // reconcile transfer state
    }
}
```

Webhook payloads expose `data.getEventId()` and `data.getEventType()` for typed routing. Event-specific handler methods are available for `cashin.card.authorized`, `payment.card.authorized`, `payment.received`, bill-payment resolution events (`payment.confirmed`, `payment.cancelled`, `payment.refunded`, `payment.failed`), `transfer.received`, `bank-transfer.initiated`, `bank-transfer.completed`, `bank-transfer.failed`, `bank-transfer.received`, `cashin.network.executed`, and `cashout.network.executed`. Generic hooks such as `onBillPayment`, `onBankTransfer`, and `onCashIn` work through default delegation.

The auto-configured endpoint accepts:

```text
POST /webhooks/chari
x-chari-signature: <hex hmac>
x-chari-timestamp: <epoch millis>
```

The HMAC payload is `{timestamp}.{rawBody}` with `chari.baas.webhook-secret`.

## Operations

`ChariBaasClient` exposes customer status/info/balance, registration/OTP/PIN, wallet info, wallet transfers, bank transfers, AP bank transfers, card funding, issued-card management, QR payments, QR generation, Telco top-up, vouchers, Fatourati bill payment, saved cards, cash-in/cash-out by reference, and refunds.

### Telco Top-up

Use typed operators instead of API integer codes. Local Moroccan numbers are normalized to `+212...` automatically.

```java
List<ChariTelcoOperator> operators = chari.getSupportedTelcoOperators();

ChariTelcoCatalogResponse catalog = chari.getTelcoCatalog(
        "0661231234",
        10,
        ChariTelcoOperator.ORANGE);

ChariTelcoCatalogResponse.TelcoProduct product = catalog.getEnabledProducts().getFirst();

ChariTelcoRechargeResponse result = chari.rechargeTelco(
        "0661231234",
        10,
        ChariTelcoOperator.ORANGE,
        ChariTelcoRechargeType.PRODUCT,
        product.getProductCode(),
        "12003"); // principal agent code supplied by Chari
```

Supported operators are `MAROC_TELECOM` (API code 1), `ORANGE` (2), and `INWI` (3). Recharge types are `CLASSIC` (0) and `PRODUCT` (1). The recharge endpoint reports `ChariOperationType.RECHARGE` (operation code 10).

### Vouchers

Voucher purchases use a preview/confirm flow. The same payload can be passed to both operations.

```java
ChariVoucherArticlesResponse catalog = chari.getVoucherArticles(
        "0661231234",
        25);

ChariVoucherArticlesResponse.VoucherArticle article =
        catalog.getData().getCollection().getFirst();

ChariVoucherPurchasePayload purchase = ChariVoucherPurchasePayload.builder()
        .customerPhoneNumber("0661231234")
        .destinationPhoneNumber("0662345678")
        .beneficiaryName("Abdennour")
        .skuId(article.getSkuId())
        .price(article.getPrice())
        .providerId(article.getProviderId())
        .build();

ChariVoucherPreviewResponse preview = chari.previewVoucherPurchase(purchase);

// Confirm only after displaying preview fees and total to the customer.
ChariVoucherPurchaseResponse confirmed = chari.confirmVoucherPurchase(purchase);
String voucherCode = confirmed.getData().getCode();
```

`skuId` is the purchase identifier; `amount`, `price`, and `providerSkuId` are optional. Catalog methods include `getVoucherArticles`, `getVoucherBrands`, `getVoucherBrand`, `getVouchersByBrand`, `getVoucherProducts`, `getVoucherProductDetail`, and `getLocalVouchers`; the `service` endpoint variants are `previewServiceVoucherPurchase` and `purchaseServiceVoucher`. Local Moroccan phone numbers are normalized automatically. Voucher preview and confirmation report `ChariOperationType.VOUCHER` (operation code 23).

### Bill Payment (Fatourati)

The client follows the guided single-creditor flow (full guide: [BILL_PAYMENT_GUIDE.md](BILL_PAYMENT_GUIDE.md)):

```java
// 1. Creditors, grouped by category; each already embeds its receivables.
//    Safe to cache in your application for several hours.
ChariBillCreditorsResponse creditors = chari.getBillCreditors();

// 2. Refresh the services exposed by the selected creditor when needed.
ChariBillReceivablesResponse services = chari.getBillReceivables("1001");

// 3. Build your UI dynamically. Never submit fields where shouldSubmit() is false.
ChariBillFormResponse form = chari.getBillIdentificationForm("1001", "01");

List<ChariBillFieldValue> identification = List.of(
        ChariBillFieldValue.of("ND", "0669440735"),
        ChariBillFieldValue.of("montant", "10"));

// Validate the answers locally against the form before any network call.
ChariBillFormValidator.validate(form, identification);

// 4. Opens an EN_ATTENTE transaction valid for seven calendar days.
//    Optionally save the bill to the customer's favorites with alias/addToFavorites.
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1001",
        "01",
        ChariBillUnpaidItemsPayload.forFields(identification),
        "0669440735");

// Or look the bill up from a scanned QR code, without filling the form:
// ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItemsByQrCode(
//         "9999", "01", qrCodeContent, "0669440735");

// Select one or more articles; selecting a subset performs a partial payment.
List<ChariBillArticle> selected = List.of(unpaid.getArticles().getFirst());

// 5. Confirm. fromUnpaidItems echoes the transaction reference and ALL
//    global parameters (including technical ones) back to Fatourati.
ChariBillPaymentResponse payment = chari.confirmBillPayment(
        "0669440735",
        ChariBillPaymentPayload.fromUnpaidItems("1001", "01", unpaid, selected));

// 6. Download the receipt of the settled operation.
ChariBillReceiptResponse receipt = chari.getBillReceipt(
        payment.getOperationId(), "0669440735");
```

Always inspect the Fatourati business result, even when HTTP succeeds:

```java
if (payment.isReceiptAvailable()) {       // 000 or already processed 301
    Long operationId = payment.getOperationId();
    String authorization = payment.getAuthorizationCode();
} else if (payment.isAwaitingWebhookResolution()) { // 908, 909, or 910
    // Do not report a definitive failure; wait for the payment webhook.
}
```

`form.getFields()` exposes dynamic field type, requiredness, length, and select values, and `ChariBillFormValidator.check(form, values)` returns every input problem for your UI. `unpaid.getDisplayableGlobalParams()` removes technical parameters with empty labels. Article prices and totals are mapped to `BigDecimal`; `getTypedArticleType()` maps article codes 0–3.

### Error Handling

Non-2xx responses throw `ChariBaasException`. When Chari returns its error envelope, the exception includes the raw Chari code, description, and typed enum:

```java
try {
    chari.getCustomerStatus("0612345678");
} catch (ChariBaasException e) {
    Integer httpStatus = e.getHttpStatusCode();
    Integer errorCode = e.getErrorCode();
    ChariErrorCode known = e.getKnownErrorCode();

    if (e.hasErrorCode(ChariErrorCode.USER_NOT_FOUND)) {
        // register customer first
    } else if (e.isAccountLocked()) {
        // ask customer to contact support
    } else if (e.isAuthenticationFailure()) {
        // check configured API key
    } else if (e.isValidationError()) {
        // fix missing or malformed request fields
    }
}
```

Known Chari error codes are available in `ChariErrorCode`, including `MISSING_PARAMETERS`, `INVALID_PHONE_NUMBER_FORMAT`, `USER_NOT_FOUND`, `INCORRECT_PIN`, `BENEFICIARY_NOT_FOUND`, and `ACCOUNT_LOCKED`.

### Domain Enums

The SDK includes typed enums for official Chari codes:

`ChariCustomerStatus`, `ChariAccountLevel`, `ChariOperationType`, `ChariTransactionType`, `ChariOperationStatus`, `ChariDirection`, `ChariClosureReason`, `ChariDocumentType`, `ChariRequestOperationType`, `ChariRequestOperationStatus`, `ChariTelcoOperator`, `ChariTelcoRechargeType`, `ChariBillFormFieldType`, and `ChariBillArticleType`. `ChariOperationType.VOUCHER` maps operation code 23.

DTOs with raw integer IDs expose typed helpers where applicable, for example `getCustomerStatus()`, `getCurrentAccountLevel()`, `getTypedOperationType()`, `getTypedOperationStatus()`, `getTypedStatus()`, and `getTypedType()`.

### Customer Status

```java
ChariCustomerStatusResponse response = chari.getCustomerStatus("0612345678");
Integer status = response.getData().getStatus();
String message = response.getData().getMessage();
ChariCustomerStatus customerStatus = response.getData().getCustomerStatus();

if (response.getData().canTransact()) {
    // customer is Active
}
```

| Code | Message            | Meaning                                                         |
| ---: | ------------------ | --------------------------------------------------------------- |
|    0 | `Not exists`       | Number does not exist with ChariMoney.                          |
|    1 | `Not confirmed`    | Number exists with ChariMoney but OTP was not entered.          |
|    2 | `Confirmed`        | Number exists and is registered with Switch.                    |
|    3 | `Active`           | Registered with Switch and active with ChariMoney; PIN created. |
|    4 | `Locked temporary` | Number is temporarily blocked after max attempts.               |
|    5 | `Locked`           | Number is blocked.                                              |

### Default Wallet

```java
ChariDefaultWalletResponse response = chari.checkDefaultWallet("0612345678");
Boolean isDefaultWallet = response.getData().getIsDefaultWallet();

boolean simple = chari.isDefaultWallet("0612345678");
```

`true` means Chari is the default wallet for the customer at Switch level. `false` means Chari is not the default wallet.

### Customer Balance

```java
ChariBalanceResponse response = chari.getCustomerBalance("0612345678");
BigDecimal balance = response.getData().getBalance();
```

The SDK sends `GET /api/customers/balance?phoneNumber=+212...`.

### Customer Info

```java
ChariCustomerInfoResponse response = chari.getCustomerInfo("0612345678");

Integer rawStatus = response.getData().getCustomerStatus();
ChariCustomerStatus status = response.getData().getTypedCustomerStatus();
ChariAccountLevel level = response.getData().getCurrentAccountLevel();
String rib = response.getData().getRib();
```

The SDK maps the full customer profile, including `accountType`, `accountLevel`, `customerStatus`, `partnerId`, `levelInReview`, and nested `partner`.

### Unregister Customer

```java
ChariBooleanResponse response = chari.unregisterCustomer(
        "0612345678",
        ChariClosureReason.CLIENT_CONTRACT_CLOSURE);
```

The SDK sends `PUT /api/customers/unregister`, normalizes `phoneNumber`, and sends the closure reason code. Closure reasons are available in `ChariClosureReason`.

### ShareID KYC Authentication

For the full personal wallet and merchant wallet KYC/KYB sequence, including app/backend responsibilities, ShareID handoff, file upload validation, and review tracking, see [KYC_UPGRADE_GUIDE.md](KYC_UPGRADE_GUIDE.md).

```java
ChariShareIdAuthResponse response = chari.authenticateShareId("0612345678", ChariAccountLevel.KYC_LEVEL_2);

String baseUrl = response.getData().getBaseUrl();
String applicantId = response.getData().getApplicantId();
String token = response.getData().getToken();
```

The SDK sends `GET /api/kyc/shareid/auth?PhoneNumber=+212...&accountLevel=2` and maps `applicant_id` to
`applicantId`. `accountLevel` is required by Chari; the single-argument
`authenticateShareId(phoneNumber)` overload sends level 2.

### KYC Confirmation

```java
ChariBooleanResponse response = chari.confirmKyc(
        "0612345678",
        ChariAccountLevel.KYC_LEVEL_2);
```

The SDK sends `POST /api/customers/upgrade/request?PhoneNumber=+212...&AccountLevel=2` with no request body. An existing pending upgrade maps to `ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW`.

### Merchant KYC Upload

```java
ChariBooleanResponse response = chari.uploadMerchantKycDocuments(
        "0612345678",
        List.of(
                ChariMerchantKycUploadPayload.KycDocument.builder()
                        .docType(ChariDocumentType.IdentityCard)
                        .docFront(new FileSystemResource("cin-front.jpg"))
                        .docBack(new FileSystemResource("cin-back.jpg"))
                        .build(),
                ChariMerchantKycUploadPayload.KycDocument.builder()
                        .docType(ChariDocumentType.CommercialRegister)
                        .docFront(new FileSystemResource("commercial-register.pdf"))
                        .build()));
```

The SDK sends `POST /api/merchant/kyc/request?phoneNumber=+212...` as `multipart/form-data` with indexed fields like `KycDocuments[0].DocType`, `KycDocuments[0].DocFront`, and `KycDocuments[0].DocBack`. An existing pending upgrade maps to `ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW`.

`DocBack` is enforced by the SDK for `IdentityCard`, `DrivingLicense`, and `ResidencePermit`. Other document types may omit it.

#### KYB documents by professional client type

Every merchant-wallet application requires:

- The contract signatory's national identity card or passport (`IdentityCard`, DocType 1; or `Passport`, DocType 3).
- Bank-account proof: a RIB/bank-account certificate, void cheque, or cheque specimen.
- Each file uploaded separately with the matching document type supplied by Chari.

Additional documents depend on the client's legal status:

| Professional client type | Required documents |
|---|---|
| Legal entity (company or organization) | Company articles/statutes; latest General Assembly minutes confirming signing authority when the manager/legal representative is not the sole signatory named in the statutes; Commercial Register certificate (`CommercialRegister`, DocType 8) issued less than 90 days ago; Professional Tax registration certificate (Patente). |
| Individual professional (auto-entrepreneur, freelancer, or sole proprietor) | Auto-entrepreneur card or professional registration document; Professional Tax registration certificate (Patente); Commercial Register certificate (`CommercialRegister`, DocType 8) issued less than 90 days ago and company statutes when applicable. |
| Foundation or association | Latest General Assembly minutes confirming signing authority when the authorized representative is not clearly named in the statutes; list of authorized representatives or board members; association/foundation statute. |

The common identity and bank-account documents are required in addition to each row. Confirm the upload codes for document categories not represented by `ChariDocumentType` with Chari before submitting the KYB request.

### Register Customer

```java
ChariRegisterCustomerPayload payload = ChariRegisterCustomerPayload.builder()
        .phoneNumber("0612345678")
        .firstName("Mohammed")
        .lastName("Chairi")
        .cin("K000000")
        .walletType(WalletType.P)
        .closeLoopOnly(true)
        .build();

ChariBooleanResponse response = chari.registerCustomer(payload);
```

The SDK normalizes local Moroccan phone numbers to `+212...` before sending. `walletType` must be `P` for particular or `C` for merchant. `closeLoopOnly` is optional; when true, the customer is enrolled in CloseLoop mode only and the OTP is sent by Chari.

### Confirm Registration

```java
ChariCustomerConfirmPayload payload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365-768")
        .walletType(WalletType.P)
        .autoActivate(true)
        .build();

ChariBooleanResponse response = chari.confirmCustomer(payload);
```

The SDK sends `POST /api/customers/confirm`, normalizes `phoneNumber`, formats OTP codes as `XXX-XXX` when callers pass `XXXXXX`, and omits `autoActivate` when it is not set. If `autoActivate=true`, the wallet is activated after OTP validation without requiring a PIN.

### Resend OTP

```java
ChariBooleanResponse response = chari.resendCustomerOtp("0612345678");
```

The SDK sends `POST /api/customers/confirm/resend-otp?phoneNumber=+212...` with no request body.

### Login With PIN

```java
ChariLoginWithPinResponse response = chari.loginWithPin("0612345678", "0000");

Boolean logged = response.getData().getLogged();
Integer remainingAttempts = response.getData().getRemainingAttempts();
```

The SDK sends `POST /api/customers/login`, normalizes `phoneNumber`, and maps `logged` plus `remainingAttempts`. Incorrect PIN errors are exposed as `ChariErrorCode.INCORRECT_PIN`.

### Create PIN

```java
ChariBooleanResponse response = chari.createPin("0612345678", "0000");
```

The SDK sends `POST /api/customers/pin` and normalizes `phoneNumber` in the JSON body. `setCustomerPin(...)` remains available as a compatibility alias. PIN errors are exposed as `ChariErrorCode.PIN_ALREADY_SET` and `ChariErrorCode.INVALID_PIN_FORMAT`.

### Update PIN

```java
ChariBooleanResponse response = chari.updatePin("0612345678", "0000", "1111");
```

The SDK sends `PATCH /api/customers/pin/change` and normalizes `phoneNumber` in the JSON body. Incorrect old PIN and invalid new PIN format are exposed as `ChariErrorCode.INCORRECT_PIN` and `ChariErrorCode.INVALID_PIN_FORMAT`.

### Card Cash-In Preview

```java
ChariCardFundingPreviewResponse response =
        chari.previewCardFunding("0612345678", new BigDecimal("100"));

BigDecimal fees = response.getData().getFeesAmount();
Boolean openLoop = response.getData().getOpenLoop();
```

The SDK sends `POST /api/operations/cashin/card/preview?phoneNumber=+212...` with the official body `{ "amount": 100 }` and maps the nested operation fields.

### Card Cash-In Preview By Agent

```java
ChariCardFundingPreviewResponse response =
        chari.previewCardFundingByAgent("21011", new BigDecimal("100"));
```

The SDK sends `POST /api/operations/cashin/card/agent/preview?code=21011` with the official body `{ "amount": 100 }` and maps `operation.code`, `operation.phoneNumber`, `amount`, and `method`.

### Card Cash-In Execute

```java
ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("4918914107195005")
        .expiryDate("2505")
        .cvv("123")
        .amount(new BigDecimal("100"))
        .keepAlive(true)
        .cardName("my_cfg_card")
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

ChariCardFundingExecutionResponse response =
        chari.executeCardFunding("0612345678", payload);
```

The SDK sends `POST /api/operations/cashin/card?phoneNumber=+212...`. `acceptURL` and `declineURL` use payload values first, then `chari.baas.card-funding.accept-url` / `decline-url` if configured, and are omitted when both payload and config are null. After 3DS, validate `RESPONSE_CODE` and `REASON_CODE` from the redirect URL in your application.

### Card Cash-In Execute By Agent

```java
ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("1234567890123456")
        .expiryDate("2505")
        .cvv("123")
        .amount(new BigDecimal("100"))
        .keepAlive(true)
        .build();

ChariCardFundingExecutionResponse response =
        chari.executeCardFundingByAgent("11023", payload);
```

The SDK sends `POST /api/operations/cashin/card/agent?code=11023`. It uses the same card payload, expiry formatting, optional `acceptURL` / `declineURL` handling, and response DTO as phone-based card cash-in.

### Saved Card Cash-In Execute

```java
ChariSavedCardCashinPayload payload = ChariSavedCardCashinPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("200"))
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

ChariSavedCardCashinResponse response =
        chari.cashinWithSavedCard(123, "0612345678", payload);
```

The SDK sends `POST /api/operations/cashin/card/{cardId}?phoneNumber=+212...`. Saved-card `acceptURL` and `declineURL` follow the same rule as direct card cash-in: payload first, configured card-funding defaults second, omitted when null. After 3DS, validate `RESPONSE_CODE`, `REASON_CODE`, and `OPERATION` from the redirect URL.
