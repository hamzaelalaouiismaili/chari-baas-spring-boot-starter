# Chari BaaS Services Integration Guide

This guide explains how an external Spring Boot application can use these services:

- Telco Top-up for Maroc Telecom, Orange, and Inwi
- Digital vouchers and gift cards
- Fatourati bill payment
- Issued-card programs, applications, controls, and transactions
- Webhooks, especially asynchronous bill-payment results

The starter handles HTTP authentication headers, request IDs, JSON mapping, phone-number normalization, and HTTP error conversion. Your application works with `ChariBaasClient` and typed Java payloads and responses.

## Contents

1. [Quick start](#1-quick-start)
2. [Public method overview](#2-public-method-overview)
3. [Telco Top-up](#3-telco-top-up)
4. [Vouchers](#4-vouchers)
5. [Bill Payment with Fatourati](#5-bill-payment-with-fatourati)
6. [Card Management](#6-card-management)
7. [Webhooks](#7-webhooks)
8. [Errors and validation](#8-errors-and-validation)
9. [Go-live checklist](#9-go-live-checklist)

## 1. Quick start

### Requirements

- Java 21
- Spring Boot 4
- A Chari partner account and API key
- A principal agent code for Telco Top-up
- A public HTTPS webhook URL for asynchronous events

### Add the dependency

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
    <version>v1.0.1</version>
</dependency>
```

### Configure the starter

```yaml
chari:
  baas:
    base-url: https://sandbox.charimoney.com
    api-key: ${CHARI_API_KEY}
    timeout-ms: 10000
    webhook-secret: ${CHARI_WEBHOOK_SECRET:}
    webhook:
      enabled: true
      verify: true
      path: /webhooks/chari
```

Use the production base URL supplied by Chari when going live. Do not store API keys or webhook secrets in source control.

### Inject the client

```java
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import org.springframework.stereotype.Service;

@Service
public class ChariServices {

    private final ChariBaasClient chari;

    public ChariServices(ChariBaasClient chari) {
        this.chari = chari;
    }
}
```

Every outgoing request automatically contains:

```text
Chari-Api-Key: <configured API key>
C-Request-Id: <generated UUID>
```

Moroccan local mobile numbers such as `0661231234` are normalized to `+212661231234`.

## 2. Public method overview

| Module | Method | Purpose |
|---|---|---|
| Telco | `getSupportedTelcoOperators()` | List supported operator enums locally |
| Telco | `getTelcoCatalog(...)` | Retrieve enabled products for an operator and amount |
| Telco | `rechargeTelco(...)` | Execute the selected top-up |
| Vouchers | `getVoucherArticles(...)` | List voucher articles for a brand |
| Vouchers | `getVoucherBrands(...)` | List voucher brands |
| Vouchers | `getVoucherBrand(...)` | Retrieve one brand |
| Vouchers | `getVouchersByBrand(...)` | Call the vouchers-by-brand endpoint |
| Vouchers | `previewVoucherPurchase(...)` | Validate a purchase and calculate fees |
| Vouchers | `confirmVoucherPurchase(...)` | Buy the voucher and receive its code |
| Bill payment | `getBillCreditors()` | List available Fatourati creditors |
| Bill payment | `getBillReceivables(...)` | List a creditor's services |
| Bill payment | `getBillIdentificationForm(...)` | Retrieve the dynamic customer form |
| Bill payment | `getBillUnpaidItems(...)` | Open a transaction and retrieve unpaid articles |
| Bill payment | `getBillUnpaidItemsByQrCode(...)` | Same lookup from a scanned bill QR code |
| Bill payment | `confirmBillPayment(...)` | Pay selected articles |
| Bill payment | `getBillReceipt(...)` | Download the receipt of a settled operation |
| Card management | `getCardPrograms(...)` | List available issuing programs |
| Card management | `addCardApplication(...)` | Apply for a card program |
| Card management | `getCardApplications(...)` | List and filter applications |
| Card management | `validateCardApplication(...)` / `rejectCardApplication(...)` | Resolve an application |
| Card management | `getManagedCards(...)` / `getManagedCard(...)` | Find issued cards |
| Card management | `manageCard(...)` and action helpers | Run lifecycle actions |
| Card management | `updateCardUsageControl(...)` | Enable or disable card services |
| Card management | `getCardTransactions(...)` | List card transactions |

## 3. Telco Top-up

### Supported values

Use enums instead of hard-coded integer codes:

| Java value | API value | Meaning |
|---|---:|---|
| `ChariTelcoOperator.MAROC_TELECOM` | 1 | Maroc Telecom / IAM |
| `ChariTelcoOperator.ORANGE` | 2 | Orange |
| `ChariTelcoOperator.INWI` | 3 | Inwi |
| `ChariTelcoRechargeType.CLASSIC` | 0 | Classic recharge |
| `ChariTelcoRechargeType.PRODUCT` | 1 | Catalog product |

### Flow

Always retrieve the catalog first. Use an enabled `productCode` from that response when executing the recharge.

```java
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoRechargeType;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoCatalogResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoRechargeResponse;

ChariTelcoCatalogResponse catalog = chari.getTelcoCatalog(
        "0661231234",
        10,
        ChariTelcoOperator.ORANGE);

ChariTelcoCatalogResponse.TelcoProduct product = catalog.getEnabledProducts()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No enabled top-up product"));

ChariTelcoRechargeResponse recharge = chari.rechargeTelco(
        "0661231234",
        10,
        ChariTelcoOperator.ORANGE,
        ChariTelcoRechargeType.PRODUCT,
        product.getProductCode(),
        "12003"); // principal agent code supplied by Chari

var result = recharge.getData();
System.out.println(result.getAmount());
System.out.println(result.getFeesAmount());
System.out.println(result.getCheckedAt());
```

### Catalog payload

Use the convenience method above or build `ChariTelcoCatalogPayload`.

| Field | Java type | Required | Description |
|---|---|---:|---|
| `recipientPhoneNumber` | `String` | Yes | Moroccan mobile number |
| `amount` | `Integer` | Yes | Positive recharge amount |
| `operator` | `ChariTelcoOperator` | Yes | IAM, Orange, or Inwi |

```java
ChariTelcoCatalogPayload request = ChariTelcoCatalogPayload.builder()
        .recipientPhoneNumber("0661231234")
        .amount(10)
        .operator(ChariTelcoOperator.ORANGE)
        .build();

ChariTelcoCatalogResponse catalog = chari.getTelcoCatalog(request);
```

### Catalog response

`ChariTelcoCatalogResponse.getData()` is a list of products.

| Product getter | Description |
|---|---|
| `getProductCode()` | Code required by the recharge request |
| `getDescription()` | French product name |
| `getArDescription()` | Arabic product name |
| `getEnabled()` | Whether the product can currently be purchased |

Useful helpers:

```java
catalog.getEnabledProducts();
catalog.findProduct(3); // Optional<TelcoProduct>
```

### Recharge payload

| Field | Java type | Required | Description |
|---|---|---:|---|
| `recipientPhoneNumber` | `String` | Yes | Beneficiary mobile number |
| `amount` | `Integer` | Yes | Positive top-up amount |
| `operator` | `ChariTelcoOperator` | Yes | Selected operator |
| `rechargeType` | `ChariTelcoRechargeType` | Yes | `CLASSIC` or `PRODUCT` |
| `productCode` | `Integer` | Yes | Catalog product code; zero is allowed |
| `code` | `String` | Yes | Principal agent account debited by Chari |

Builder alternative:

```java
ChariTelcoRechargePayload request = ChariTelcoRechargePayload.builder()
        .recipientPhoneNumber("0661231234")
        .amount(10)
        .operator(ChariTelcoOperator.ORANGE)
        .rechargeType(ChariTelcoRechargeType.PRODUCT)
        .productCode(product.getProductCode())
        .code("12003")
        .build();

ChariTelcoRechargeResponse response = chari.rechargeTelco(request);
```

### Recharge response

| Getter | Java type | Description |
|---|---|---|
| `getOperationType()` | `Integer` | Raw operation code, normally 10 |
| `getTypedOperationType()` | `ChariOperationType` | `RECHARGE` |
| `getAmount()` | `BigDecimal` | Top-up amount |
| `getFeesAmount()` | `BigDecimal` | Applied fees |
| `getCheckedAt()` | `String` | API timestamp |
| `getOpenLoop()` | `Boolean` | Open-loop indicator |

## 4. Vouchers

Voucher purchase uses two steps: preview, then confirm. Pass the same payload to both methods.

### Browse the catalog

The short overload returns the first ten articles:

```java
ChariVoucherArticlesResponse articles = chari.getVoucherArticles(
        "0661231234",
        25);
```

Use `ChariVoucherCatalogQuery` for pagination:

```java
ChariVoucherCatalogQuery query = ChariVoucherCatalogQuery.builder()
        .phoneNumber("0661231234")
        .brandId(25)
        .page(1)
        .take(20)
        .build();

ChariVoucherArticlesResponse articles = chari.getVoucherArticles(query);
ChariVoucherBrandsResponse brands = chari.getVoucherBrands(query);
```

| Query field | Java type | Required | Description |
|---|---|---:|---|
| `phoneNumber` | `String` | Yes | Customer mobile number |
| `brandId` | `Integer` | Yes | Positive brand ID |
| `page` | `Integer` | No | Page number, starting at 1 |
| `take` | `Integer` | No | Positive page size |

Additional catalog calls:

```java
ChariVoucherBrandResponse brand = chari.getVoucherBrand(25, "0661231234");
ChariVoucherBrandResponse brandArticles = chari.getVouchersByBrand(25, "0661231234");
```

The current upstream contract maps `getVouchersByBrand(...)` to a `ChariVoucherBrandResponse`.

### Article response

Each `ChariVoucherArticlesResponse.VoucherArticle` exposes:

| Getter | Description |
|---|---|
| `getProviderSkuId()` | Provider article identifier used for purchase |
| `getProductName()` | Display name |
| `getImageUrl()` | Optional image URL |
| `getPrice()` | Price as `BigDecimal` |
| `getDescription()` | Optional description |
| `getProviderId()` | Provider ID used for purchase |
| `getBrandId()` | Brand ID |

### Preview and confirm a purchase

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
        .providerSkuId(article.getProviderSkuId())
        .providerId(article.getProviderId())
        .build();

ChariVoucherPreviewResponse preview = chari.previewVoucherPurchase(purchase);

BigDecimal fees = preview.getData().getFeesAmount();
BigDecimal total = preview.getData().getTotalAmount();

// Ask the customer to accept the displayed total before confirming.
ChariVoucherPurchaseResponse confirmed = chari.confirmVoucherPurchase(purchase);

String voucherCode = confirmed.getData().getOperation().getCode();
String activationUrl = confirmed.getData().getOperation().getUrlActivateCard();
```

### Purchase payload

| Field | Java type | Required | Description |
|---|---|---:|---|
| `customerPhoneNumber` | `String` | Yes | Chari customer who pays |
| `destinationPhoneNumber` | `String` | Yes | Voucher recipient |
| `beneficiaryName` | `String` | Yes | Recipient display name |
| `providerSkuId` | `String` | Yes | SKU from the selected article |
| `providerId` | `Integer` | Yes | Provider ID from the selected article |

### Preview response

| Getter from `preview.getData()` | Description |
|---|---|
| `getTypedOperationType()` | `ChariOperationType.VOUCHER` |
| `getOperation().getAmount()` | Voucher amount |
| `getFeesAmount()` | Fees |
| `getTotalAmount()` | Customer total |
| `getCheckedAt()` | Preview timestamp |
| `getOpenLoop()` | Open-loop indicator |

### Confirmation response

The redeemable value is under `confirmed.getData().getOperation()`.

| Getter | Description |
|---|---|
| `getVoucherName()` | Purchased voucher name |
| `getCode()` | Voucher code to deliver securely |
| `getDescription()` | Voucher description |
| `getAmount()` | Voucher amount |
| `getCashBack()` | Optional cashback |
| `getTotalAmount()` | Operation total |
| `getDestinationPhoneNumber()` | Recipient number |
| `getBeneficiaryName()` | Recipient name |
| `getUrlActivateCard()` | Optional activation URL |
| `getCheckedAt()` | Execution timestamp |

Treat voucher codes as sensitive values. Do not write them to application logs.

## 5. Bill Payment with Fatourati

> Full module guide: [BILL_PAYMENT_GUIDE.md](BILL_PAYMENT_GUIDE.md)

Bill payment is a guided flow for one creditor at a time:

1. List creditors (each creditor already embeds its receivables).
2. Optionally refresh the selected creditor's receivables.
3. Retrieve and render the dynamic identification form.
4. Submit identification values — or a scanned QR code — and retrieve unpaid articles.
5. Confirm all or a subset of those articles.
6. Download the receipt of the settled operation.

Do not hard-code creditor form fields. Fatourati can add or change creditors without an SDK release.

### Step 1: list creditors

```java
ChariBillCreditorsResponse response = chari.getBillCreditors();

List<ChariBillCreditorsResponse.Creditor> creditors = response.getAllCreditors();
ChariBillCreditorsResponse.Creditor iam = response.findCreditor("1002");
```

Creditors arrive grouped by category (`getCategories()`), each with `codeCreancier`, `nomCreancier`, `descriptionCreancier`, `logoPath`, `siteWeb`, and its embedded `receivables`. The creditor response changes infrequently and may be cached by your application for several hours.

### Step 2: list receivables

Each creditor from step 1 already carries `getReceivables()`. Call this endpoint only to refresh them:

```java
ChariBillReceivablesResponse response = chari.getBillReceivables("1002");

List<ChariBillReceivablesResponse.Receivable> services = response.getReceivables();
ChariBillReceivablesResponse.Receivable internet = response.findReceivable("03");
```

`codeCreance` is always sent as a two-character string such as `"01"`. Do not convert it to an integer and lose the leading zero.

### Step 3: build the dynamic identification form

```java
ChariBillFormResponse form = chari.getBillIdentificationForm("1001", "01");

for (ChariBillFormResponse.IdentificationField field : form.getFields()) {
    if (!field.shouldSubmit()) {
        // Display field.getLibelle() as static help text.
        continue;
    }

    ChariBillFormFieldType type = field.getTypedFieldType();
    boolean required = field.isRequired();

    // Render text/select/password using type, getAllowedValues(),
    // tailleMin, and tailleMax.
}
```

| Form getter | Meaning |
|---|---|
| `getLibelle()` | User-facing label |
| `getNomChamp()` | Name sent back to Fatourati |
| `getTypedFieldType()` | `TEXT`, `SELECT`, `PASSWORD`, or `LABEL` |
| `isSelect()` / `getAllowedValues()` | Options for a select field |
| `getFormatChamp()` | `1` string, `2` integer, `3` real |
| `getTailleMin()` / `getTailleMax()` | Input length constraints |
| `isRequired()` | Whether the customer must provide a value |
| `shouldSubmit()` | False for display-only `libelle` fields |

Never include a field where `shouldSubmit()` is false in the submitted values.

### Validate answers locally before calling the API

`ChariBillFormValidator` checks the customer's answers against the form — required fields, min/max lengths, and select options — so bad input fails fast without a network round-trip:

```java
List<ChariBillFieldValue> answers = List.of(
        ChariBillFieldValue.of("ND", "0669440735"),
        ChariBillFieldValue.of("montant", "10"));

// Either collect every problem for your UI...
List<String> problems = ChariBillFormValidator.check(form, answers);

// ...or throw an IllegalArgumentException listing all of them.
ChariBillFormValidator.validate(form, answers);
```

The five-argument `getBillUnpaidItems(creditorId, receivableId, form, payload, phoneNumber)` overload runs this validation automatically before sending the request.

### Step 4: retrieve unpaid items

Build the payload from the customer's answers with the `forFields` factory. Pass the customer's phone number when you want the lookup saved to their favorites:

```java
ChariBillUnpaidItemsPayload payload = ChariBillUnpaidItemsPayload.builder()
        .creditorValues(answers)
        .alias("My Orange line")     // optional
        .addToFavorites(true)        // optional, requires alias + phoneNumber
        .build();

ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1001", "01", payload, "0669440735");
```

Or look the bill up from a scanned QR code, without filling the form:

```java
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItemsByQrCode(
        "9999", "01", qrCodeContent, "0669440735");
```

Successful lookup data:

| Getter | Description |
|---|---|
| `getRefTxFatourati()` | Open transaction reference; echoed back automatically on confirmation |
| `getNbreCreances()` | Number of returned articles |
| `getMontantTotalTTC()` | Total including tax and fees |
| `getArticles()` | Articles the customer can select (alias of `getImpayesParams()`) |
| `findArticle(id)` | Article lookup by `idArticle` |
| `getGlobalParams()` | All global parameters; echoed back automatically on confirmation |
| `getDisplayableGlobalParams()` | Customer-safe global parameters only |

Do not display raw `globalParams` entries with an empty `libelle` (for example `contrPaiement`); they are technical Fatourati parameters — but they MUST be echoed back on confirmation, which `ChariBillPaymentPayload.fromUnpaidItems` handles for you.

Each `ChariBillArticle` provides `idArticle`, `description`, `dateFacture`, `prixTTC`, `typeArticle`, and optional `extraArticleParams`. `getTypedArticleType()` returns:

| Type | Code | Meaning |
|---|---:|---|
| `RECEIVABLE` | 0 | Normal bill/receivable |
| `FEE` | 1 | Fee |
| `MANDATORY` | 2 | Mandatory article |
| `STAMP_FEE` | 3 | Stamp fee |

The transaction reference remains valid for seven calendar days.

### Step 5: confirm selected articles

Selecting a subset of the articles performs a partial payment. Keep all selected articles under the same creditor and receivable. Build the payload with `fromUnpaidItems` so the transaction reference and all global parameters are echoed back correctly:

```java
List<ChariBillArticle> selectedArticles = List.of(unpaid.getArticles().getFirst());

ChariBillPaymentPayload paymentRequest = ChariBillPaymentPayload.fromUnpaidItems(
        "1001", "01", unpaid, selectedArticles);

ChariBillPaymentResponse payment = chari.confirmBillPayment(
        "0669440735", paymentRequest);
```

### Confirmation payload

| Field | Java type | Required | Description |
|---|---|---:|---|
| Method `phoneNumber` | `String` | Yes | Existing Chari Money customer |
| `creancierId` | `String` | Yes | Same four-digit creditor ID |
| `creanceId` | `String` | Yes | Same two-digit receivable ID |
| `refTxFatourati` | `String` | Yes | 12-digit reference returned by unpaid lookup |
| `globalParams` | `List<ChariBillFieldValue>` | Yes | ALL global parameters from the unpaid lookup, echoed verbatim |
| `selectedArticles` | `List<ChariBillArticle>` | Yes | Non-empty subset of unpaid articles |

The SDK serializes `globalParams` as `globalParams` and `selectedArticles` as `ListeArticleSelectionnes`. The client validates all of these locally and throws `IllegalArgumentException` before any network call when something is missing or malformed.

### Handle the business response

The confirmation returns the wallet operation and the Fatourati business outcome:

```java
if (payment.isReceiptAvailable()) {
    // fatouratiErrorCode 000 or 301
    Long operationId = payment.getOperationId();
    String authorization = payment.getAuthorizationCode();
    BigDecimal total = payment.getData().getTotalAmount();
    BigDecimal fees = payment.getData().getFeesAmount();
} else if (payment.isAwaitingWebhookResolution()) {
    // 908, 909, or 910: not a definitive failure.
    // Keep the operation pending and wait for its webhook.
} else {
    String code = payment.getFatouratiErrorCode();
    String reason = payment.getData().getReason();
}
```

| Helper | Meaning |
|---|---|
| `isSuccessful()` | `fatouratiErrorCode` is `000` |
| `hasCode(code)` | Tests an exact Fatourati business code |
| `isAlreadyProcessed()` | Code is `301`; treat as successful |
| `isReceiptAvailable()` | Code is `000` or `301` |
| `isAwaitingWebhookResolution()` | Code is `908`, `909`, or `910` |

`getData()` also exposes `operationType`, `amount`, `checkedAt`, `creditor`, `debt`, `categoryCode`, and `category` for display on the receipt screen.

### Step 6: download the bill receipt

```java
ChariBillReceiptResponse receipt = chari.getBillReceipt(
        payment.getOperationId(), "0669440735");

Map<String, Object> fields = receipt.getFields();
String reference = receipt.getString("refReglement");
```

Receipt content is creditor-dependent, so it is exposed as a key/value map with `getString(key)` convenience access.

## 6. Card Management

Card Management covers cards issued through Chari programs. It is separate from saved/tokenized-card APIs, which manage external bank cards saved for payment or cash-in.

The usual lifecycle is: select a program, create an application, resolve the application, retrieve the issued card, configure it, and monitor transactions.

`Get Program by ID` is not exposed because the upstream operation is marked as coming soon.

### Programs

```java
ChariCardProgramsResponse programs = chari.getCardPrograms(); // 10 results, page 1
ChariCardProgramsResponse page = chari.getCardPrograms(20, 2);

ChariCardProgramsResponse.CardProgram program = programs.getData()
        .getCollection()
        .stream()
        .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No active card program"));
```

Program responses include the program ID/name, BIN range, typed card type, scheme, currency, validity, PIN limit, default usage controls, price, and active/total card counts.

### Applications

Adding an application has no body. The phone number and selected program are sent as query parameters.

```java
ChariCardApplicationCreatedResponse created = chari.addCardApplication(
        "0665638046",
        program.getCardProgramId());

ChariCardApplicationsResponse applications = chari.getCardApplications(
        ChariCardApplicationsQuery.builder()
                .pageSize(10)
                .pageNumber(1)
                .status(ChariCardApplicationStatus.PENDING)
                .build());
```

| Application status | API code |
|---|---:|
| `PENDING` | 1 |
| `VALIDATED` | 2 |
| `REJECTED` | 3 |

The upstream customer-applications endpoint currently has pagination but no phone-number parameter:

```java
ChariCardApplicationsResponse customerApplications =
        chari.getCardApplicationsByCustomer(10, 1);
```

Resolve an application:

```java
ChariCardApplicationResponse validated = chari.validateCardApplication(
        333L, "0665638046");

ChariCardApplicationResponse rejected = chari.rejectCardApplication(
        333L, "0665638046");
```

Validation and rejection have no request body. The documented rejection endpoint does not accept a rejection reason. Use `getTypedApplicationStatus()` on the returned application.

### Find issued cards

```java
ChariManagedCardsResponse cards = chari.getManagedCards(
        ChariManagedCardsQuery.builder()
                .pageSize(10)
                .pageNumber(1)
                .phoneNumber("0665638046")
                .cardProgramId(1L)
                .deliveryStatus(ChariCardDeliveryStatus.DELIVERED)
                .cardStatus(ChariIssuedCardStatus.ACTIVATED)
                .build());

ChariManagedCard card = chari.getManagedCard(123L, "0665638046").getData();
```

All `ChariManagedCardsQuery` filters are optional.

| Card status | Code | Delivery status | Code |
|---|---:|---|---:|
| `ISSUED` | 1 | `PENDING` | 1 |
| `ACTIVATED` | 2 | `SENT_TO_PERSONALIZATION` | 2 |
| `BLOCKED` | 3 | `READY_FOR_DELIVERY` | 3 |
| `SUSPENDED` | 4 | `DELIVERED` | 4 |
| `EXPIRED` | 5 |  |  |
| `CANCELLED` | 6 |  |  |

Useful helpers are `getTypedCardType()`, `getTypedCardStatus()`, and `getTypedDeliveryStatus()`. Limits are returned as `ChariCardLimit` values.

Treat `cardToken` as sensitive. Display only `maskedPan`.

### Lifecycle actions

```java
chari.activateCard(123L, "0665638046");
chari.blockCard(123L, "0665638046");
chari.suspendCard(123L, "0665638046");
chari.reactivateCard(123L, "0665638046");
chari.cancelCard(123L, "0665638046");
chari.unblockCardPin(123L, "0665638046");
chari.resetCardPin(123L, "0665638046");
```

The generic typed alternative is:

```java
ChariBooleanResponse result = chari.manageCard(
        123L,
        "0665638046",
        ChariCardAction.BLOCK);
```

Every action returns `ChariBooleanResponse`. `CANCEL` is permanent; use `BLOCK` or `SUSPEND` for reversible restrictions.

### Usage control

All four payload fields are required. Send the complete desired state.

```java
ChariCardUsageControlPayload controls = ChariCardUsageControlPayload.builder()
        .allowAtm(true)
        .allowOnline(false)
        .allowPos(true)
        .contactlessEnabled(false)
        .build();

ChariBooleanResponse updated = chari.updateCardUsageControl(
        123L,
        "0665638046",
        controls);
```

| Field | Meaning |
|---|---|
| `allowAtm` | ATM withdrawals |
| `allowOnline` | Online/e-commerce transactions |
| `allowPos` | Point-of-sale payments |
| `contactlessEnabled` | Contactless payments |

`allowInternational` is returned on programs/cards but is not part of the documented update payload.

### Transactions

```java
ChariCardTransactionsResponse transactions = chari.getCardTransactions(
        123L,
        ChariCardTransactionsQuery.builder()
                .pageSize(10)
                .pageNumber(1)
                .phoneNumber("0665638046")
                .from("2026-05-01T00:00:00Z")
                .to("2026-05-31T23:59:59Z")
                .build());
```

Phone and date filters are optional. Use an ISO-8601 date format accepted by your Chari environment. Each transaction includes masked PAN, merchant data, contactless flag, amount/currency, raw method/status/type codes, timestamps, and its related operation ID.

## 7. Webhooks

The starter registers this endpoint by default:

```text
POST /webhooks/chari
```

Chari sends `x-chari-signature` and `x-chari-timestamp`. When a webhook secret is configured and verification is enabled, the starter verifies the HMAC signature before dispatching the event.

### Implement a handler

Create one Spring bean and override only the callbacks your application needs:

```java
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import org.springframework.stereotype.Component;

@Component
public class ChariWebhookProcessor implements ChariWebhookHandler {

    @Override
    public void onBillPaymentConfirmed(WebhookData data) {
        // Mark the pending bill payment as confirmed.
        // Reconcile using operationId/reference and store the final state idempotently.
    }

    @Override
    public void onBillPaymentCancelled(WebhookData data) {
        // Mark the payment as cancelled. Do not issue a receipt.
    }

    @Override
    public void onBillPaymentRefunded(WebhookData data) {
        // Mark the payment as refunded and reconcile your ledger.
    }

    @Override
    public void onBillPaymentFailed(WebhookData data) {
        // Mark the payment as failed after final resolution.
    }

    @Override
    public void onUnknown(WebhookData data) {
        // Record unknown event types for investigation without failing delivery.
    }
}
```

You may override the generic `onBillPayment(WebhookData)` instead. The four specific bill callbacks delegate to it by default.

### Bill-payment events

| Event ID | Handler | Final state |
|---|---|---|
| `payment.confirmed` | `onBillPaymentConfirmed` | Confirmed |
| `payment.cancelled` | `onBillPaymentCancelled` | Cancelled |
| `payment.refunded` | `onBillPaymentRefunded` | Refunded |
| `payment.failed` | `onBillPaymentFailed` | Failed |

Telco and voucher methods return synchronous typed responses. Generic operation callbacks such as `onOperationCreated` and `onOperationUpdated` can be handled when those events are enabled for your partner account.

### Useful webhook data

```java
data.getWebhookEventId();
data.getWebhookId();
data.getEventId();
data.getEventType();
data.getOperationId();
data.getTypedOperationType();
data.getTypedOperationStatus();
data.getAmount();
data.getFeeAmount();
data.getReference();
data.getCreatedAt();
data.getExecutedAt();
```

### Production rules

- Make handlers idempotent. Chari may retry the same delivery.
- Prefer `WebhookEventId`; fall back to `WebhookId` or `OperationId` as a deduplication key.
- Return quickly. Persist the event, then perform slow work asynchronously.
- Do not trust an unsigned webhook in production.
- Do not mark Fatourati codes `908`, `909`, or `910` as failed before final webhook resolution.
- Do not log voucher codes, API keys, PINs, or other sensitive values.

## 8. Errors and validation

The SDK validates required fields before making an HTTP request. Invalid input throws `IllegalArgumentException`, for example an invalid Moroccan phone number, non-positive amount, malformed creditor ID, or empty article selection.

Non-successful HTTP responses throw `ChariBaasException`:

```java
try {
    chari.getTelcoCatalog("0661231234", 10, ChariTelcoOperator.ORANGE);
} catch (IllegalArgumentException e) {
    // Fix invalid local input; do not retry unchanged.
} catch (ChariBaasException e) {
    Integer httpStatus = e.getHttpStatusCode();
    Integer errorCode = e.getErrorCode();
    String description = e.getErrorDescription();
}
```

Fatourati is different: an HTTP 2xx response can contain a rejected business result. Inspect `codeRetour` using the helpers described above.

## 9. Go-live checklist

- Replace the sandbox URL with the production URL supplied by Chari.
- Store the API key and webhook secret in a secret manager.
- Confirm the principal agent code used for Telco debits.
- Render Fatourati identification forms dynamically.
- Keep Fatourati transaction references until final resolution.
- Confirm voucher purchases only after displaying preview fees and total.
- Store and deliver voucher codes securely.
- Make webhook handling idempotent and verify signatures.
- Test duplicate confirmation code `301` and pending codes `908`–`910`.
- Keep one creditor per bill-payment transaction.
