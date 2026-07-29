# Bill Payment Module (Fatourati)

Complete integration guide for the bill payment module of the Chari BaaS Spring Boot Starter. It covers the full Fatourati flow — discovering creditors, rendering the dynamic identification form, retrieving unpaid bills (by form values or QR code), confirming payment, and downloading the receipt — together with the local validation the SDK runs before every network call.

## Table of Contents

1. [Overview](#1-overview)
2. [Endpoints and SDK Methods](#2-endpoints-and-sdk-methods)
3. [Step 1 — List Creditors](#3-step-1--list-creditors)
4. [Step 2 — List Receivables](#4-step-2--list-receivables)
5. [Step 3 — Dynamic Identification Form](#5-step-3--dynamic-identification-form)
6. [Local Validation Before Sending](#6-local-validation-before-sending)
7. [Step 4 — Retrieve Unpaid Items](#7-step-4--retrieve-unpaid-items)
8. [Step 5 — Confirm Payment](#8-step-5--confirm-payment)
9. [Step 6 — Download the Receipt](#9-step-6--download-the-receipt)
10. [Business Codes](#10-business-codes)
11. [Model Reference](#11-model-reference)
12. [End-to-End Example](#12-end-to-end-example)

---

## 1. Overview

Bill payment is a guided flow for **one creditor at a time**:

```
getBillCreditors()
        │  creditors grouped by category, each embedding its receivables
        ▼
getBillReceivables(creancierId)            (optional refresh)
        │
        ▼
getBillIdentificationForm(creancierId, creanceId)
        │  dynamic form: text / select / password / label fields
        ▼
ChariBillFormValidator.validate(form, answers)     ← local, no network
        │
        ▼
getBillUnpaidItems(...)  or  getBillUnpaidItemsByQrCode(...)
        │  opens an EN_ATTENTE transaction (refTxFatourati, 7 days)
        ▼
confirmBillPayment(phoneNumber, payload)
        │  wallet operation + Fatourati outcome (operationId, fees, …)
        ▼
getBillReceipt(operationId, phoneNumber)
```

Key principles:

- **Never hard-code creditor form fields.** Fatourati can add or change creditors without an SDK release; always render the dynamic form.
- **Fail fast locally.** Every SDK method validates its inputs and throws `IllegalArgumentException` with a precise message *before* any HTTP call.
- **Echo back what you received.** The confirmation must return the transaction reference and **all** global parameters from the unpaid lookup — including technical ones with an empty label. `ChariBillPaymentPayload.fromUnpaidItems(...)` does this for you.

## 2. Endpoints and SDK Methods

| Step | HTTP endpoint | `ChariBaasClient` method |
|---|---|---|
| 1 | `GET /api/bills/creanciers` | `getBillCreditors()` |
| 2 | `GET /api/bills/creances?creancierId=` | `getBillReceivables(creancierId)` |
| 3 | `GET /api/bills/form?creancierId=&creanceId=` | `getBillIdentificationForm(creancierId, creanceId)` |
| 4 | `POST /api/bills/impayes?creancierId=&creanceId=[&phoneNumber=]` | `getBillUnpaidItems(...)` / `getBillUnpaidItemsByQrCode(...)` |
| 5 | `POST /api/bills/confirm?phoneNumber=` | `confirmBillPayment(phoneNumber, payload)` |
| 6 | `GET /api/bills/bill-receipt/{operationId}?phoneNumber=` | `getBillReceipt(operationId, phoneNumber)` |

All requests are authenticated with the `Chari-Api-Key` header, handled automatically by the starter.

Local input rules enforced by the SDK:

| Input | Rule |
|---|---|
| `creancierId` | Four digits, at least `1000` |
| `creanceId` | Exactly two digits (keep the leading zero: `"01"`) |
| `refTxFatourati` | Exactly twelve digits |
| `phoneNumber` | Valid Moroccan Chari Money number; normalized to `+212…` |
| Field values | Every entry needs `nomChamp` and `valeurChamp` |
| Articles | Non-empty; each needs `idArticle`, `prixTTC ≥ 0`, `typeArticle` |
| Favorites | `addToFavorites = true` requires an `alias` and a phone number |
| Impayes body | Either identification values or a QR code content |

## 3. Step 1 — List Creditors

```java
ChariBillCreditorsResponse creditors = chari.getBillCreditors();

// Grouped by category for display:
for (ChariBillCreditorsResponse.CreditorCategory category : creditors.getCategories()) {
    // category.getCategoryName(), category.getOrder(), category.getCreditors()
}

// Or flattened / by code:
List<ChariBillCreditorsResponse.Creditor> all = creditors.getAllCreditors();
ChariBillCreditorsResponse.Creditor orange = creditors.findCreditor("1003");
```

Each creditor exposes `codeCreancier`, `nomCreancier`, `descriptionCreancier`, `logoPath`, `siteWeb`, its embedded `getReceivables()`, and technical `params`. The response changes infrequently and may be cached by your application for several hours.

## 4. Step 2 — List Receivables

Creditors from step 1 already embed their receivables, so this endpoint is only needed to refresh them:

```java
ChariBillReceivablesResponse services = chari.getBillReceivables("1002");

List<ChariBillReceivablesResponse.Receivable> list = services.getReceivables();
int count = services.getCount();
ChariBillReceivablesResponse.Receivable internet = services.findReceivable("03");
```

`codeCreance` is always a two-character string such as `"01"`. Never convert it to an integer — you would lose the leading zero.

## 5. Step 3 — Dynamic Identification Form

```java
ChariBillFormResponse form = chari.getBillIdentificationForm("1001", "01");

for (ChariBillFormResponse.IdentificationField field : form.getFields()) {
    if (!field.shouldSubmit()) {
        // typeChamp "libelle": display field.getLibelle() as static help text.
        continue;
    }
    switch (field.getTypedFieldType()) {
        case TEXT, PASSWORD -> renderInput(field.getLibelle(),
                field.getTailleMin(), field.getTailleMax(), field.isRequired());
        case SELECT -> renderSelect(field.getLibelle(), field.getAllowedValues());
        default -> renderInput(field.getLibelle(), null, null, field.isRequired());
    }
}
```

| `IdentificationField` getter | Meaning |
|---|---|
| `getLibelle()` | User-facing label |
| `getNomChamp()` | Technical name sent back to Fatourati |
| `getTypedFieldType()` | `TEXT`, `SELECT`, `PASSWORD`, or `LABEL` |
| `isSelect()` / `getAllowedValues()` | Select options (`listVals`) |
| `getFormatChamp()` | `1` string, `2` integer, `3` real |
| `getTailleMin()` / `getTailleMax()` | Length constraints |
| `isRequired()` | `contrainte == "1"` |
| `shouldSubmit()` | `false` for display-only `libelle` fields |

`form.findField("montant")` looks a field up by its technical name.

## 6. Local Validation Before Sending

`ChariBillFormValidator` checks the customer's answers against the dynamic form so bad input never reaches the network:

- required fields have a non-blank value;
- value lengths respect `tailleMin`/`tailleMax`;
- select fields only accept one of their allowed values;
- no unknown field names are submitted.

```java
List<ChariBillFieldValue> answers = List.of(
        ChariBillFieldValue.of("ND", "0669440735"),
        ChariBillFieldValue.of("montant", "10"));

// Collect every problem for your UI:
List<String> problems = ChariBillFormValidator.check(form, answers);

// Or throw IllegalArgumentException listing all of them:
ChariBillFormValidator.validate(form, answers);
```

The five-argument overload runs it automatically:

```java
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1001", "01", form, payload, "0669440735");
```

## 7. Step 4 — Retrieve Unpaid Items

### By form values

```java
ChariBillUnpaidItemsPayload payload = ChariBillUnpaidItemsPayload.builder()
        .creditorValues(answers)
        .alias("My Orange line")   // optional: favorites display name
        .addToFavorites(true)      // optional: requires alias + phoneNumber
        .build();

ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1001", "01", payload, "0669440735");
```

Without favorites, `ChariBillUnpaidItemsPayload.forFields(answers)` and the three-argument overload (no phone number) are enough.

### By QR code

```java
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItemsByQrCode(
        "9999", "01", qrCodeContent, "0670770743");
```

The QR content replaces the form values entirely (`qrcodecontent` in the request body).

### Reading the response

| Getter | Description |
|---|---|
| `getRefTxFatourati()` | Open transaction reference, valid for seven calendar days |
| `getNbreCreances()` | Number of returned articles |
| `getMontantTotalTTC()` | Total including tax (`montantTotalTTCField`) |
| `getArticles()` | Selectable articles (alias of `getImpayesParams()`) |
| `findArticle(id)` | Article lookup by `idArticle` |
| `getGlobalParams()` | ALL global parameters — echo them back on confirm |
| `getDisplayableGlobalParams()` | Only parameters safe to show to the customer |

Parameters with an empty `libelle` (for example `contrPaiement`, `colAffiche`) are technical: never display them, but always send them back on confirmation.

Each `ChariBillArticle` provides `idArticle`, `description`, `dateFacture`, `prixTTC` (`BigDecimal`), `typeArticle`, and optional `extraArticleParams`. `getTypedArticleType()` maps the code:

| Type | Code | Meaning |
|---|---:|---|
| `RECEIVABLE` | 0 | Normal bill/receivable |
| `FEE` | 1 | Fee |
| `MANDATORY` | 2 | Mandatory article |
| `STAMP_FEE` | 3 | Stamp fee |

## 8. Step 5 — Confirm Payment

Selecting a subset of the articles performs a partial payment. Keep all selected articles under the same creditor and receivable.

```java
List<ChariBillArticle> selected = List.of(unpaid.getArticles().getFirst());

ChariBillPaymentPayload payload = ChariBillPaymentPayload.fromUnpaidItems(
        "1001", "01", unpaid, selected);

ChariBillPaymentResponse payment = chari.confirmBillPayment("0669440735", payload);
```

`fromUnpaidItems` copies `refTxFatourati` and every global parameter from the lookup, which is exactly what the API expects. If you build the payload manually:

| Field | JSON key | Required | Description |
|---|---|---:|---|
| `creancierId` | `creancierId` | Yes | Same four-digit creditor ID as the lookup |
| `creanceId` | `creanceId` | Yes | Same two-digit receivable ID |
| `refTxFatourati` | `refTxFatourati` | Yes | Twelve-digit reference from the lookup |
| `globalParams` | `globalParams` | Yes | ALL global parameters, echoed verbatim |
| `selectedArticles` | `ListeArticleSelectionnes` | Yes | Non-empty subset of unpaid articles |

### Reading the confirmation

```java
if (payment.isReceiptAvailable()) {          // 000 or already processed 301
    Long operationId = payment.getOperationId();
    String authorization = payment.getAuthorizationCode();
    BigDecimal total = payment.getData().getTotalAmount();
    BigDecimal fees = payment.getData().getFeesAmount();
} else if (payment.isAwaitingWebhookResolution()) {   // 908 / 909 / 910
    // Not a definitive failure — keep the operation pending
    // and wait for the payment webhook.
} else {
    String code = payment.getFatouratiErrorCode();
    String reason = payment.getData().getReason();
}
```

`getData()` also exposes `operationType`, `amount`, `checkedAt`, `creditor` (display name), `debt` (receivable name), `categoryCode`, and `category` for the receipt screen.

## 9. Step 6 — Download the Receipt

```java
ChariBillReceiptResponse receipt = chari.getBillReceipt(
        payment.getOperationId(), "0669440735");

Map<String, Object> fields = receipt.getFields();
String reference = receipt.getString("refReglement");
```

Receipt content is creditor-dependent, so it is exposed as a key/value map with a `getString(key)` convenience accessor.

## 10. Business Codes

Fatourati returns business outcomes inside successful HTTP responses (`fatouratiErrorCode` on confirmation). Always inspect them:

| Code | Helper | Meaning | Action |
|---|---|---|---|
| `000` | `isSuccessful()` | Payment accepted | Show the receipt |
| `301` | `isAlreadyProcessed()` | Already processed earlier | Treat as success; show the receipt |
| `908` / `909` / `910` | `isAwaitingWebhookResolution()` | Digital-channel state pending | Keep pending; resolve via webhook |
| other | `hasCode(code)` | Creditor-specific rejection | Show the failure with `getData().getReason()` |

HTTP-level failures (4xx/5xx) throw `ChariBaasException` as everywhere else in the SDK.

## 11. Model Reference

| Class | Role |
|---|---|
| `ChariBillCreditorsResponse` | Creditor categories; `getAllCreditors()`, `findCreditor(code)` |
| `ChariBillReceivablesResponse` | `data.collection` envelope; `getReceivables()`, `findReceivable(code)` |
| `ChariBillFormResponse` | Dynamic form; `getFields()`, `findField(name)` |
| `ChariBillFormResponse.IdentificationField` | One form field with typing and constraint helpers |
| `ChariBillFieldValue` | `nomChamp`/`valeurChamp` pair; `ChariBillFieldValue.of(name, value)`; reads both `valChamp` and `valeurChamp` |
| `ChariBillUnpaidItemsPayload` | Lookup body; `forFields(...)`, `forQrCode(...)`, `alias`, `addToFavorites` |
| `ChariBillUnpaidItemsResponse` | Open transaction; `getArticles()`, `findArticle(id)`, `getDisplayableGlobalParams()` |
| `ChariBillArticle` | Payable article; `getTypedArticleType()` |
| `ChariBillPaymentPayload` | Confirmation body; `fromUnpaidItems(...)` factory |
| `ChariBillPaymentResponse` | Wallet operation + Fatourati outcome; business-code helpers |
| `ChariBillReceiptResponse` | Creditor-dependent receipt; `getFields()`, `getString(key)` |
| `ChariBillFormValidator` | Local pre-send validation; `check(...)`, `validate(...)` |
| `ChariBillFormFieldType` | `TEXT`, `SELECT`, `PASSWORD`, `LABEL`, `UNKNOWN` |
| `ChariBillArticleType` | `RECEIVABLE`, `FEE`, `MANDATORY`, `STAMP_FEE`, `UNKNOWN` |

## 12. End-to-End Example

```java
// 1–2. Discover: Orange recharge (creditor 1003, receivable 01).
ChariBillCreditorsResponse creditors = chari.getBillCreditors();
ChariBillCreditorsResponse.Creditor orange = creditors.findCreditor("1003");

// 3. Render the dynamic form.
ChariBillFormResponse form = chari.getBillIdentificationForm("1003", "01");

// Collect the customer's answers and validate them locally.
List<ChariBillFieldValue> answers = List.of(
        ChariBillFieldValue.of("ND", "0669440735"),
        ChariBillFieldValue.of("montant", "20"));
ChariBillFormValidator.validate(form, answers);

// 4. Open the transaction and list what can be paid.
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1003", "01",
        ChariBillUnpaidItemsPayload.forFields(answers),
        "0669440735");

// Show unpaid.getDisplayableGlobalParams() and unpaid.getArticles(),
// let the customer pick what to pay.
List<ChariBillArticle> selected = List.of(unpaid.getArticles().getFirst());

// 5. Confirm.
ChariBillPaymentResponse payment = chari.confirmBillPayment(
        "0669440735",
        ChariBillPaymentPayload.fromUnpaidItems("1003", "01", unpaid, selected));

if (payment.isReceiptAvailable()) {
    // 6. Receipt.
    ChariBillReceiptResponse receipt = chari.getBillReceipt(
            payment.getOperationId(), "0669440735");
} else if (payment.isAwaitingWebhookResolution()) {
    // Keep pending; the payment webhook delivers the final state.
}
```

For the rest of the SDK (customers, transfers, cards, vouchers, telco, webhooks), see [SERVICES_INTEGRATION_GUIDE.md](SERVICES_INTEGRATION_GUIDE.md) and [SDK_USAGE.md](SDK_USAGE.md).
