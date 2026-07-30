# Bill Payment Module (Fatourati)

Complete integration guide for the bill payment module of the Chari BaaS Spring Boot Starter. It covers the full Fatourati flow — discovering creditors, rendering the dynamic identification form, retrieving unpaid bills (by form values or QR code), confirming payment, and downloading the receipt — together with the local validation the SDK runs before every network call.

Every step below explains **the use case** (why the API exists and when to call it), shows **the real wire format** (what actually goes over HTTP), and gives **the SDK code** to use.

## Table of Contents

1. [Overview — What Is the Fatourati Flow?](#1-overview--what-is-the-fatourati-flow)
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
13. [Common Mistakes](#13-common-mistakes)

---

## 1. Overview — What Is the Fatourati Flow?

Fatourati is Morocco's centralized bill platform. Through it a customer can pay a **bill or service of any registered creditor**: an Orange/IAM/inwi phone bill or recharge, a water & electricity bill (SRM, Amendis, Redal…), a tax (DGI vignette, TGR), school fees, tolls (Jawaz), donations, and so on.

Because every creditor asks for different customer information (a phone number, a contract number, a car registration plate…), the flow is **discovery-driven**: your application never hard-codes what a creditor needs — it asks the API, renders what it gets, and echoes back what the customer typed.

The flow is always the same six steps, for **one creditor at a time**:

```
getBillCreditors()                              "What can the customer pay?"
        │  creditors grouped by category, each embedding its receivables
        ▼
getBillReceivables(creancierId)                 "Which services does this creditor offer?" (optional refresh)
        │
        ▼
getBillIdentificationForm(creancierId, creanceId)   "What must the customer type to be identified?"
        │  dynamic form: text / select / password / label fields
        ▼
ChariBillFormValidator.validate(form, answers)  ← local, no network
        │
        ▼
getBillUnpaidItems(...)  or  getBillUnpaidItemsByQrCode(...)   "What does the customer owe?"
        │  opens an EN_ATTENTE transaction (refTxFatourati, valid 7 days)
        ▼
confirmBillPayment(phoneNumber, payload)        "Pay the selected items from the wallet."
        │  wallet operation + Fatourati outcome (operationId, fees, …)
        ▼
getBillReceipt(operationId, phoneNumber)        "Give me the proof of payment."
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

**Use case.** This is your "Pay a bill" catalog screen. The customer opens the bill payment section of your app and must choose *who* to pay. This endpoint returns every active creditor, already grouped by display category (Téléphone et Internet, Eau et électricité, Frais administratifs…) and already embedding each creditor's services — so one call is enough to build the whole catalog UI.

**When to call it.** Once when the customer enters the bill payment section. The list changes rarely; your application may cache it for several hours.

**Wire example.**

```
GET /api/bills/creanciers
```

```json
{
  "data": [
    {
      "categoryName": "Téléphone et Internet",
      "categoryCode": "101",
      "order": 1,
      "listeCreanciersListe": [
        {
          "codeCreancier": "1003",
          "nomCreancier": "Orange Recharges et Catalogue Pass",
          "logoPath": "/upload/logos/logo_meditel.png",
          "siteWeb": "http://www.orange.ma/",
          "listeCreances": [
            { "codeCreance": "01", "nomCreance": "Orange recharge Sim" }
          ],
          "params": [
            { "nomChamp": "publicDomain", "valChamp": "https://www.fatourati.ma" },
            { "nomChamp": "categorieCode", "valChamp": "101" }
          ]
        }
      ]
    }
  ]
}
```

**SDK usage.**

```java
ChariBillCreditorsResponse creditors = chari.getBillCreditors();

// Build the catalog screen — one section per category:
for (ChariBillCreditorsResponse.CreditorCategory category : creditors.getCategories()) {
    // category.getCategoryName(), category.getOrder(), category.getCreditors()
}

// Or work with a flat list / a specific creditor:
List<ChariBillCreditorsResponse.Creditor> all = creditors.getAllCreditors();
ChariBillCreditorsResponse.Creditor orange = creditors.findCreditor("1003");
```

Each creditor exposes `codeCreancier`, `nomCreancier`, `descriptionCreancier`, `logoPath`, `siteWeb`, its embedded `getReceivables()`, and technical `params` (metadata such as `publicDomain` — you normally ignore these).

## 4. Step 2 — List Receivables

**Use case.** A *receivable* (`creance`) is one **service** of a creditor. One creditor can offer several: IAM Factures (1002) has "Produit Fixe Sim" (01), "Produit Mobile Sim" (02), "Produit Internet Sim" (03)… The customer picked a creditor in step 1 and must now pick *which service* to pay.

**When to call it.** Usually never — step 1 already embeds each creditor's receivables in `listeCreances`. Call this endpoint only when you want to **refresh** a single creditor's services without re-fetching the whole catalog (for example, you cached step 1 for hours but want fresh services at selection time).

**Wire example.**

```
GET /api/bills/creances?creancierId=1002
```

```json
{
  "data": {
    "collection": [
      { "codeCreance": "01", "nomCreance": "Produit Fixe Sim" },
      { "codeCreance": "02", "nomCreance": "Produit Mobile Sim" },
      { "codeCreance": "03", "nomCreance": "Produit Internet Sim" },
      { "codeCreance": "04", "nomCreance": "Payer une avance" }
    ],
    "count": 4
  }
}
```

**SDK usage.**

```java
ChariBillReceivablesResponse services = chari.getBillReceivables("1002");

List<ChariBillReceivablesResponse.Receivable> list = services.getReceivables();
int count = services.getCount();
ChariBillReceivablesResponse.Receivable internet = services.findReceivable("03");
```

⚠️ `codeCreance` is always a two-character string such as `"01"`. Never convert it to an integer — you would lose the leading zero and the API would reject it.

## 5. Step 3 — Dynamic Identification Form

**Use case.** Fatourati must know *which customer account* to look up, and every creditor asks for different things:

| Creditor | Service | Form fields returned |
|---|---|---|
| Orange Recharge (1003/01) | Recharge a SIM | `ND` (phone number, text) + `montant` (amount, **select**: 10/20/30/50/100/200/300) |
| IAM Factures (1002/01) | Fixed-line bill | `ND` (fixed-line number, text) + `Fidelio` (4-digit confidential code) |
| IAM Internet (1002/03) | Internet bill | `ND` (payment identifier, text 1–100 chars) |
| DGI vignette (1022/01) | Car tax | `matricule`, `puissance_fiscale`, `carburant_type`, `periode_annee` |

This endpoint returns that field list so your app can render the right inputs. **This is why you never hard-code forms** — a creditor added tomorrow works in your app with zero code changes.

**When to call it.** Right after the customer picks a creditor + service, to build the identification screen.

**Wire example** — Orange Recharge (`1003/01`):

```
GET /api/bills/form?creancierId=1003&creanceId=01
```

```json
{
  "data": {
    "collection": [
      {
        "libelle": "Numéro de téléphone",
        "nomChamp": "ND",
        "typeChamp": "text",
        "formatChamp": "2",
        "tailleMin": 10,
        "tailleMax": 10,
        "contrainte": "1",
        "listVals": []
      },
      {
        "libelle": "Montant",
        "nomChamp": "montant",
        "typeChamp": "select",
        "formatChamp": "1",
        "tailleMin": 0,
        "tailleMax": 10,
        "contrainte": "1",
        "listVals": ["10", "20", "30", "50", "100", "200", "300"]
      }
    ],
    "count": 2
  }
}
```

Reading: render a **required 10-character text input** labeled "Numéro de téléphone", and a **required dropdown** labeled "Montant" with the seven listed amounts.

**SDK usage.**

```java
ChariBillFormResponse form = chari.getBillIdentificationForm("1003", "01");

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

**Use case.** The unpaid-items call opens a real transaction on Fatourati's side, and Fatourati answers bad input with a generic error (`Fatourati – System error`) that tells the customer nothing. Validating locally means your UI can highlight *exactly which field* is wrong — instantly, without a network round-trip, and without opening a junk transaction.

`ChariBillFormValidator` checks the customer's answers against the dynamic form:

- required fields have a non-blank value;
- value lengths respect `tailleMin`/`tailleMax`;
- select fields only accept one of their allowed values;
- no unknown field names are submitted.

```java
List<ChariBillFieldValue> answers = List.of(
        ChariBillFieldValue.of("ND", "0669440735"),
        ChariBillFieldValue.of("montant", "20"));

// Option A — collect every problem for your UI:
List<String> problems = ChariBillFormValidator.check(form, answers);
// e.g. ["Field 'Montant' (montant) must be one of [10, 20, 30, 50, 100, 200, 300]"]

// Option B — throw IllegalArgumentException listing all of them:
ChariBillFormValidator.validate(form, answers);
```

The five-argument overload of the lookup runs it automatically:

```java
ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1003", "01", form, payload, "0669440735");
```

## 7. Step 4 — Retrieve Unpaid Items

**Use case.** This is the heart of the flow: "here is who the customer is — **what do they owe?**". Fatourati identifies the account, opens a pending (`EN_ATTENTE`) transaction, and returns the list of payable *articles*. Depending on the creditor an article is a monthly bill (IAM returns one per unpaid month), a recharge option (Orange returns "Recharge classique", "Pass Internet"…), or a computed tax. Your app shows these articles and lets the customer select what to pay.

The returned `refTxFatourati` is the handle to that open transaction — it is valid for **seven calendar days** and must be echoed back on confirmation.

**When to call it.** After the customer fills the identification form (or scans a bill QR code) and taps "Continue".

### By form values

**Wire example** — Orange recharge of 20 MAD for line `0669440735`:

```
POST /api/bills/impayes?creancierId=1003&creanceId=01&phoneNumber=+212669440735
```

```json
{
  "creancierVals": [
    { "nomChamp": "ND",      "valChamp": "0669440735" },
    { "nomChamp": "montant", "valChamp": "20" }
  ],
  "Alias": "My Orange line",
  "AddToFavorites": true
}
```

```json
{
  "data": {
    "refTxFatourati": "100003754540",
    "nbreCreances": 1,
    "montantTotalTTCField": "20.00",
    "globalParams": [
      { "libelle": "Numéro de ligne à recharger", "nomChamp": "ND",            "valeurChamp": "0669440735" },
      { "libelle": "",                            "nomChamp": "colAffiche",    "valeurChamp": "3,4" },
      { "libelle": "",                            "nomChamp": "contrPaiement", "valeurChamp": "3" }
    ],
    "impayesParams": [
      {
        "idArticle": "0:0",
        "description": "Recharge classique",
        "dateFacture": null,
        "prixTTC": "20",
        "typeArticle": 0
      }
    ]
  }
}
```

**SDK usage.**

```java
ChariBillUnpaidItemsPayload payload = ChariBillUnpaidItemsPayload.builder()
        .creditorValues(answers)
        .alias("My Orange line")   // optional: favorites display name
        .addToFavorites(true)      // optional: requires alias + phoneNumber
        .build();

ChariBillUnpaidItemsResponse unpaid = chari.getBillUnpaidItems(
        "1003", "01", payload, "0669440735");
```

Without favorites, `ChariBillUnpaidItemsPayload.forFields(answers)` and the three-argument overload (no phone number) are enough.

> ℹ️ The SDK writes each value under **both** JSON keys (`valChamp` and `valeurChamp`) because Chari reads `valChamp` here but `valeurChamp` on the confirm step. You never deal with this — `ChariBillFieldValue.of(name, value)` handles it.

### By QR code

**Use case.** Paper bills of some creditors carry a QR code that encodes the whole identification. Scanning it skips steps 3 and 6 entirely — no form, no typing.

```json
{ "qrcodecontent": "ZI3qynFphbr00HgLL164D+opiMtTapDYaACl1dP2sGx/..." }
```

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

⚠️ Parameters with an empty `libelle` (for example `contrPaiement`, `colAffiche` above) are **technical**: never display them, but **always send them back on confirmation** — dropping them makes the payment fail. `getDisplayableGlobalParams()` is for your UI; `getGlobalParams()` is for the confirm payload.

Each `ChariBillArticle` provides `idArticle`, `description`, `dateFacture`, `prixTTC` (`BigDecimal`, sent on the wire as a string like `"168.00"`), `typeArticle`, and optional `extraArticleParams`. `getTypedArticleType()` maps the code:

| Type | Code | Meaning |
|---|---:|---|
| `RECEIVABLE` | 0 | Normal bill/receivable |
| `FEE` | 1 | Fee |
| `MANDATORY` | 2 | Mandatory article |
| `STAMP_FEE` | 3 | Stamp fee |

## 8. Step 5 — Confirm Payment

**Use case.** The customer reviewed the articles, selected what to pay (all of them, or a subset for a **partial payment** — e.g. pay 2 of 3 unpaid IAM months), and tapped "Pay". This call debits the customer's Chari wallet, settles with Fatourati, and returns both the wallet operation (`operationId`, amounts, fees) and the Fatourati business outcome (`fatouratiErrorCode`).

**When to call it.** Once per open transaction, after explicit customer confirmation. Keep all selected articles under the same creditor and receivable.

**Wire example** — paying the Orange recharge from step 4:

```
POST /api/bills/confirm?phoneNumber=+212669440735
```

```json
{
  "creancierId": "1003",
  "creanceId": "01",
  "refTxFatourati": "100003754540",
  "globalParams": [
    { "libelle": "Numéro de ligne à recharger", "nomChamp": "ND",            "valeurChamp": "0669440735" },
    { "libelle": "",                            "nomChamp": "colAffiche",    "valeurChamp": "3,4" },
    { "libelle": "",                            "nomChamp": "contrPaiement", "valeurChamp": "3" }
  ],
  "ListeArticleSelectionnes": [
    {
      "idArticle": "0:0",
      "description": "Recharge classique",
      "dateFacture": null,
      "prixTTC": "20",
      "typeArticle": 0
    }
  ]
}
```

Note how `refTxFatourati`, all three `globalParams` (including the two technical ones), and the selected article are **echoes of the step 4 response** — nothing is invented client-side.

```json
{
  "data": {
    "operationType": 25,
    "operationId": 14326,
    "amount": 20,
    "feesAmount": 1,
    "totalAmount": 20,
    "checkedAt": "2026-07-29T19:35:31.42Z",
    "creditor": "Orange Recharges et Catalogue Pass",
    "debt": "Orange recharge Sim",
    "categoryCode": "101",
    "category": "Téléphone et Internet",
    "authorizationCode": "d55994",
    "fatouratiErrorCode": "000"
  }
}
```

**SDK usage.**

```java
List<ChariBillArticle> selected = List.of(unpaid.getArticles().getFirst());

ChariBillPaymentPayload payload = ChariBillPaymentPayload.fromUnpaidItems(
        "1003", "01", unpaid, selected);

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

**Use case.** Proof of payment. The customer wants a reference to show the creditor ("I paid, here is my règlement number"), and your support team needs it for disputes. The receipt content is **creditor-dependent** — Orange returns different fields than the DGI — so the SDK exposes it as a key/value map instead of a fixed class.

**When to call it.** After a confirmation where `isReceiptAvailable()` is true (`000` or `301`), using the `operationId` from the confirmation. Also useful later, from your operation history screen.

**Wire example.**

```
GET /api/bills/bill-receipt/14326?phoneNumber=+212669440735
```

```json
{
  "data": {
    "refReglement": "XH4195",
    "montantTotalTTC": "20.00"
  }
}
```

**SDK usage.**

```java
ChariBillReceiptResponse receipt = chari.getBillReceipt(
        payment.getOperationId(), "0669440735");

Map<String, Object> fields = receipt.getFields();
String reference = receipt.getString("refReglement");
```

Render the whole map generically (label per key) rather than expecting fixed fields.

## 10. Business Codes

**Use case.** A `200 OK` from `confirm` does **not** mean the payment succeeded — Fatourati reports the business outcome inside the response (`fatouratiErrorCode`). Your app must branch on it: show a receipt, keep the operation pending, or show a failure.

| Code | Helper | Meaning | Action |
|---|---|---|---|
| `000` | `isSuccessful()` | Payment accepted | Show the receipt |
| `301` | `isAlreadyProcessed()` | Already processed earlier | Treat as success; show the receipt |
| `908` / `909` / `910` | `isAwaitingWebhookResolution()` | Digital-channel state pending | Keep pending; resolve via webhook |
| other | `hasCode(code)` | Creditor-specific rejection | Show the failure with `getData().getReason()` |

HTTP-level failures (4xx/5xx) throw `ChariBaasException` as everywhere else in the SDK. Its message is self-contained — stage, HTTP status, numeric code, the documented meaning of that code, and the raw provider text:

```
[GET_BILL_UNPAID_ITEMS] Chari API error 35008 (HTTP 400): Fatourati has no bill to
pay for this account: nothing is outstanding, or the bill was already settled. Ask
the customer to check the identification values (or try again later for a new
billing period). Chari reported: "Fatourati – No bill to pay".
```

The two lookup rejections you will actually hit:

| `errorCode` | `ChariErrorCode` | Helper | Meaning |
|---|---|---|---|
| `35008` | `BILL_NO_BILL_TO_PAY` | `isNoBillToPay()` | Nothing outstanding for that account, or already paid |
| `35026` | `BILL_SYSTEM_ERROR` | `hasErrorCode(BILL_SYSTEM_ERROR)` | Identification values did not match a real account (wrong number/format) |

Both are covered by `isBillLookupFailure()` — a user-fixable input problem rather than an outage, so send the customer back to the identification form instead of retrying:

```java
try {
    unpaid = chari.getBillUnpaidItems("1003", "01", payload, phone);
} catch (ChariBaasException e) {
    if (e.isNoBillToPay()) {
        return "Aucune facture à payer pour ce compte.";
    }
    if (e.isBillLookupFailure()) {
        return "Vérifiez les informations saisies.";
    }
    throw e;
}
```

Unmapped codes keep the raw provider description in the message and expose it via `getErrorCode()` / `getErrorDescription()`, so nothing is lost when Chari adds a new code.

## 11. Model Reference

| Class | Role |
|---|---|
| `ChariBillCreditorsResponse` | Creditor categories; `getAllCreditors()`, `findCreditor(code)` |
| `ChariBillReceivablesResponse` | `data.collection` envelope; `getReceivables()`, `findReceivable(code)` |
| `ChariBillFormResponse` | Dynamic form; `getFields()`, `findField(name)` |
| `ChariBillFormResponse.IdentificationField` | One form field with typing and constraint helpers |
| `ChariBillFieldValue` | `nomChamp`/`valeurChamp` pair; `ChariBillFieldValue.of(name, value)`; reads and writes both `valChamp` and `valeurChamp` (Chari reads `valChamp` on impayes and `valeurChamp` on confirm) |
| `ChariBillUnpaidItemsPayload` | Lookup body; `forFields(...)`, `forQrCode(...)`, `alias`, `addToFavorites` |
| `ChariBillUnpaidItemsResponse` | Open transaction; `getArticles()`, `findArticle(id)`, `getDisplayableGlobalParams()` |
| `ChariBillArticle` | Payable article; `getTypedArticleType()`; `prixTTC` serialized as a string |
| `ChariBillPaymentPayload` | Confirmation body; `fromUnpaidItems(...)` factory |
| `ChariBillPaymentResponse` | Wallet operation + Fatourati outcome; business-code helpers |
| `ChariBillReceiptResponse` | Creditor-dependent receipt; `getFields()`, `getString(key)` |
| `ChariBillFormValidator` | Local pre-send validation; `check(...)`, `validate(...)` |
| `ChariBillFormFieldType` | `TEXT`, `SELECT`, `PASSWORD`, `LABEL`, `UNKNOWN` |
| `ChariBillArticleType` | `RECEIVABLE`, `FEE`, `MANDATORY`, `STAMP_FEE`, `UNKNOWN` |

## 12. End-to-End Example

The complete scenario: a customer recharges their Orange line `0669440735` with 20 MAD.

```java
// 1–2. Discover: Orange recharge (creditor 1003, receivable 01).
ChariBillCreditorsResponse creditors = chari.getBillCreditors();
ChariBillCreditorsResponse.Creditor orange = creditors.findCreditor("1003");

// 3. Fetch and render the dynamic form
//    (returns: ND = phone number, montant = select 10/20/.../300).
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

// 5. Confirm — fromUnpaidItems echoes refTxFatourati + ALL globalParams.
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

## 13. Common Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Hard-coding form fields for a creditor | Breaks when Fatourati changes the form | Always render `getBillIdentificationForm(...)` |
| Converting `creanceId`/`codeCreance` to `int` | `"01"` becomes `1` → `IllegalArgumentException` or API rejection | Keep IDs as strings |
| Dropping technical `globalParams` (empty `libelle`) on confirm | Payment rejected by Fatourati | Use `fromUnpaidItems(...)`; echo **all** params |
| Building the confirm payload from scratch | Missing/incorrect `refTxFatourati` echo | Use `ChariBillPaymentPayload.fromUnpaidItems(...)` |
| Treating HTTP 200 on confirm as success | Failed payments shown as paid | Branch on `isSuccessful()` / `isAwaitingWebhookResolution()` |
| Reusing a `refTxFatourati` older than 7 days | Fatourati rejects the confirmation | Redo the unpaid-items lookup |
| Displaying `getGlobalParams()` to the customer | Technical values (`contrPaiement`…) on screen | Display `getDisplayableGlobalParams()` only |
| Skipping local validation | Generic `Fatourati – System error` (35026) with no field info | Run `ChariBillFormValidator` before the lookup |
