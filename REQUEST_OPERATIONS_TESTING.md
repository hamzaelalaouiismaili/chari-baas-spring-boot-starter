# Testing `GET /api/operations/requests`

How to exercise the request-operations listing added in **v1.0.24**, and exactly what comes back.

The endpoint lists the **cash-in / cash-out requests** created for a customer — the `reference` flow
(`requestCashinByReference` / `requestCashoutByReference` / `requestFatouratiCashin`) — together with
the operation that an agent produced when executing each request.

| | |
|---|---|
| Chari endpoint | `GET /api/operations/requests` |
| SDK method | `ChariBaasClient.getRequestOperations(phoneNumber, pageSize, pageNumber)` |
| Response type | `ChariRequestOperationsResponse` |
| Audit stage | `GET_REQUEST_OPERATIONS` |
| Since | `v1.0.24` |

---

## 1. Prerequisites

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

The leading `v` matters — JitPack publishes under the git tag name. If your build cached an older
copy, refresh it once with `mvn -U clean install` (or `gradle --refresh-dependencies`).

```yaml
chari:
  baas:
    base-url: https://sandbox.charimoney.com   # or https://api.chari.ma
    api-key: ${CHARI_API_KEY}
```

---

## 2. Three ways to test

### a. Raw curl — is the data there at all?

```bash
curl --location \
  'https://sandbox.charimoney.com/api/operations/requests?pageSize=100&phoneNumber=%2B212665638046' \
  --header 'accept: text/plain' \
  --header 'Chari-Api-Key: '"$CHARI_API_KEY"
```

Note `%2B` — the `+` must be percent-encoded in a shell-quoted URL. The SDK handles this for you.

### b. Through the example test app

`examples/chari-baas-test-app` exposes the call on port 8081:

```bash
cd examples/chari-baas-test-app
CHARI_API_KEY=your-key mvn spring-boot:run
```

```bash
curl 'http://localhost:8081/test/chari/operations/requests?phoneNumber=%2B212665638046&pageSize=10'
```

The controller method behind it:

```java
@GetMapping("/operations/requests")
public Object getRequestOperations(@RequestParam String phoneNumber,
        @RequestParam(required = false) Integer pageSize,
        @RequestParam(required = false) Integer pageNumber) {
    return chari.getRequestOperations(phoneNumber, pageSize, pageNumber);
}
```

Watch the app log — the audit logger prints the outgoing call as a multi-line ASCII banner
carrying the stage `GET_REQUEST_OPERATIONS` and the resolved URL. For a single grep-able line
instead, switch the format:

```yaml
chari:
  baas:
    audit:
      enabled: true
      format: KV   # default is BANNER
```

```
[CHARI-AUDIT] event=request stage=GET_REQUEST_OPERATIONS method=GET
              url=https://sandbox.charimoney.com/api/operations/requests?phoneNumber=+212665638046&pageSize=10
```

### c. From your own Spring application

```java
@Service
@RequiredArgsConstructor
public class RequestOperationsService {

    private final ChariBaasClient chari;

    public void print(String phoneNumber) {
        ChariRequestOperationsResponse res = chari.getRequestOperations(phoneNumber, 100, null);

        System.out.println("total matching requests = " + res.getData().getCount());

        for (var item : res.getData().getCollection()) {
            if (item.isOpen()) {
                System.out.printf("%s | created %s | PENDING%n",
                        item.getReference(), item.getCreatedAt());
                continue;
            }
            var op = item.getOperation();
            System.out.printf("%s | %s | %s | %s MAD (fees %s) | %s | %s%n",
                    item.getReference(),
                    item.getTypedOperationType(),    // CASHIN / CASHOUT
                    item.getTypedOperationStatus(),  // COMPLETED
                    op.getAmount(),
                    op.getFeesAmount(),
                    op.getPrimaryAccountNumber(),
                    item.getEntity());
        }
    }
}
```

**Parameters**

| Parameter | Required | Notes |
|---|---|---|
| `phoneNumber` | yes | Accepts `0665638046`, `212665638046` or `+212665638046` — normalized to E.164 by the SDK, masked in logs |
| `pageSize` | no | Omitted from the query string when `null` |
| `pageNumber` | no | Omitted from the query string when `null` |

---

## 3. Expected response

Envelope: `data.collection` (the page) + `data.count` (**total matching requests, not the page size**).

### A request that has been executed

```json
{
  "data": {
    "collection": [
      {
        "operationRequestId": 2208,
        "createdAt": "2026-08-28T14:14:16.843644",
        "closedAt": "2026-08-28T15:15:13.123515",
        "reference": "1575006433",
        "phoneNumber": null,
        "code": null,
        "accountId": 0,
        "operationType": 0,
        "operationStatus": 0,
        "partnerId": 0,
        "amount": 0,
        "description": null,
        "networkId": 1,
        "entity": "Partner(121)-Agent(11210550)",
        "operationId": null,
        "customData": "{\"notificationUrl\":null,\"externalReference\":\"string-ee---e\"}",
        "operation": {
          "accountId": 550,
          "primaryAccountNumber": "+212600000000",
          "secondaryAccountNumber": "+212600000000",
          "feesAmount": 0,
          "operationDate": "2026-08-28T15:15:12.963236",
          "openLoop": false,
          "operationStatus": 2,
          "nonExistentUser": false,
          "transactions": null,
          "operationRequest": {
            "operationRequestId": 2208,
            "createdAt": "2026-08-28T14:14:16.843644",
            "closedAt": "2026-08-28T15:15:13.123515",
            "reference": "1575006433",
            "networkId": 1,
            "entity": "Partner(121)-Agent(11210550)",
            "networkName": "Internal"
          },
          "operationId": 16224,
          "transactionId": 0,
          "operationType": 1,
          "transactionType": 0,
          "method": "agent",
          "amount": 0,
          "sens": null,
          "partnerId": 121,
          "description": "retest agent cashout",
          "note": null,
          "images": null
        }
      }
    ],
    "count": 2197
  }
}
```

### A request still waiting for an agent

```json
{
  "operationRequestId": 2206,
  "createdAt": "2026-08-27T11:45:51.697167",
  "closedAt": null,
  "reference": "2264511325",
  "phoneNumber": null,
  "code": null,
  "accountId": 0,
  "operationType": 0,
  "operationStatus": 0,
  "partnerId": 0,
  "amount": 0,
  "description": null,
  "networkId": null,
  "entity": null,
  "operationId": null,
  "customData": null,
  "operation": null
}
```

`operation`, `closedAt`, `networkId` and `entity` are all null until the request is executed.
In a real sandbox page of 46 rows, 13 were executed and 33 were still pending.

---

## 4. Field reference

### Request item — `ChariRequestOperationsResponse.RequestOperationItem`

| JSON field | Java type | Meaning |
|---|---|---|
| `operationRequestId` | `Long` | Chari's internal id for the request |
| `createdAt` | `String` | ISO-8601 without offset, e.g. `2026-08-28T14:14:16.843644` |
| `closedAt` | `String` | Set when executed or canceled; `null` while open |
| `reference` | `String` | The 10-digit reference the customer/agent uses |
| `phoneNumber` | `String` | Often `null` on this endpoint — see gotchas |
| `code` | `String` | Fatourati code when applicable |
| `accountId` | `Long` | `0` at request level — see gotchas |
| `operationType` | `Integer` | `0` at request level — use `getTypedOperationType()` |
| `operationStatus` | `Integer` | `0` at request level — use `getTypedOperationStatus()` |
| `partnerId` | `Integer` | `0` at request level |
| `amount` | `BigDecimal` | `0` at request level — the real amount is on `operation` |
| `description` | `String` | Usually `null` at request level |
| `networkId` | `Integer` | Executing network; `null` while open |
| `entity` | `String` | `Partner(121)-Agent(11210550)`; `null` while open |
| `operationId` | `Long` | `null` — the executed id lives on `operation.operationId` |
| `customData` | `String` | **Raw JSON string** you sent at request time |
| `operation` | `ExecutedOperation` | `null` while open |

Helper methods (computed, not wire fields):

| Method | Returns |
|---|---|
| `getTypedOperationType()` | `CASHIN` / `CASHOUT` / `UNKNOWN` — falls back to `operation` |
| `getTypedOperationStatus()` | `OPEN` / `COMPLETED` / `FAILED` / `CANCELED` / `UNKNOWN` — falls back to `operation` |
| `isOpen()` | `true` when `closedAt == null && operation == null` |

### Executed operation — `ChariRequestOperationsResponse.ExecutedOperation`

| JSON field | Java type | Meaning |
|---|---|---|
| `operationId` | `Long` | Id of the resulting operation (`16224`) |
| `transactionId` | `Long` | `0` when no ledger transaction is attached |
| `accountId` | `Long` | Customer account that was debited/credited |
| `primaryAccountNumber` | `String` | Customer MSISDN |
| `secondaryAccountNumber` | `String` | Counterparty MSISDN |
| `amount` | `BigDecimal` | Executed amount |
| `feesAmount` | `BigDecimal` | Fees applied |
| `operationDate` | `String` | When the agent executed it |
| `openLoop` | `Boolean` | Open-loop operation flag |
| `nonExistentUser` | `Boolean` | True when the counterparty is not a Chari customer |
| `operationType` | `Integer` | `1` = cash-in, `2` = cash-out |
| `operationStatus` | `Integer` | `2` = completed |
| `transactionType` | `Integer` | Ledger transaction type |
| `sens` | `Integer` | Direction; `null` on this endpoint |
| `partnerId` | `Integer` | Partner that executed it |
| `method` | `String` | e.g. `agent` |
| `description` | `String` | Free text, e.g. `retest agent cashout` |
| `note` | `String` | Usually `null` |
| `transactions` | `List<JsonNode>` | Usually `null` on this endpoint |
| `images` | `JsonNode` | Usually `null` on this endpoint |
| `operationRequest` | `OperationRequestSummary` | Back-reference to this request |

Typed getters: `getTypedOperationType()`, `getTypedOperationStatus()`, `getTypedSens()`.

### Back-reference — `OperationRequestSummary`

`operationRequestId`, `createdAt`, `closedAt`, `reference`, `networkId`, `entity`,
`networkName` (e.g. `Internal`).

---

## 5. What *your* users receive

If you return the SDK object straight from your controller (as the example app does), Jackson
serializes it with `@JsonInclude(NON_NULL)` — **null fields disappear** and the shape is otherwise
identical to section 3. The typed helpers are `@JsonIgnore`, so `getTypedOperationType()`,
`getTypedOperationStatus()` and `isOpen()` are **not** in that JSON.

To expose them, map to your own DTO:

```java
public record RequestOperationView(
        Long id, String reference, String type, String status,
        boolean pending, BigDecimal amount, String customerPhone,
        String createdAt, String executedAt, String agent) {

    static RequestOperationView from(ChariRequestOperationsResponse.RequestOperationItem item) {
        var op = item.getOperation();
        return new RequestOperationView(
                item.getOperationRequestId(),
                item.getReference(),
                item.getTypedOperationType().name(),
                item.getTypedOperationStatus().name(),
                item.isOpen(),
                op == null ? null : op.getAmount(),
                op == null ? null : op.getPrimaryAccountNumber(),
                item.getCreatedAt(),
                op == null ? null : op.getOperationDate(),
                item.getEntity());
    }
}
```

Which yields, for your users:

```json
[
  {
    "id": 2208,
    "reference": "1575006433",
    "type": "CASHIN",
    "status": "COMPLETED",
    "pending": false,
    "amount": 250.50,
    "customerPhone": "+212600000000",
    "createdAt": "2026-08-28T14:14:16.843644",
    "executedAt": "2026-08-28T15:15:12.963236",
    "agent": "Partner(121)-Agent(11210550)"
  },
  {
    "id": 2206,
    "reference": "2264511325",
    "type": "UNKNOWN",
    "status": "UNKNOWN",
    "pending": true,
    "amount": null,
    "customerPhone": null,
    "createdAt": "2026-08-27T11:45:51.697167",
    "executedAt": null,
    "agent": null
  }
]
```

---

## 6. Gotchas worth checking in your test

1. **Request-level `operationType` / `operationStatus` / `amount` / `accountId` / `partnerId` come
   back as `0`.** Chari only fills them on the nested `operation`. That is why the typed getters
   fall back to `operation`, and why you should read amounts from `item.getOperation().getAmount()`,
   never `item.getAmount()`.
2. **A pending request has no type.** With `operation == null` there is nothing to fall back to, so
   `getTypedOperationType()` returns `UNKNOWN`. Use `isOpen()` to branch, not the enum.
3. **`count` is the total, not the page.** A page of 46 rows can report `count: 2197`. Paginate with
   `pageNumber` until you have `count` rows.
4. **`phoneNumber` and `code` are usually `null`** at request level — the customer MSISDN is
   `operation.primaryAccountNumber`. Don't key your reconciliation on `item.getPhoneNumber()`.
5. **`customData` is a JSON *string*, not an object.** Parse it yourself if you need the
   `externalReference` you sent:
   ```java
   JsonNode custom = new ObjectMapper().readTree(item.getCustomData());
   String externalReference = custom.path("externalReference").asText(null);
   ```
6. **Timestamps carry no timezone** (`2026-08-28T14:14:16.843644`). They are exposed as `String`;
   parse with `LocalDateTime.parse(...)` and apply your own zone.
7. **Unknown enum codes never throw** — they map to `UNKNOWN`, so a new Chari status won't break you.

---

## 7. Errors

Failures raise `ChariBaasException` with the stage `GET_REQUEST_OPERATIONS`:

```java
try {
    chari.getRequestOperations(phone, 100, null);
} catch (ChariBaasException e) {
    e.getStage();            // GET_REQUEST_OPERATIONS
    e.getHttpStatusCode();   // 401, 404, 500...
    e.getErrorCode();
    e.getErrorDescription();
    e.getKnownErrorCode();   // ChariErrorCode enum
}
```

Common cases: `401` — bad or missing `Chari-Api-Key`; `404` — unknown customer. A customer with no
requests is expected to return `200` with an empty `collection` — worth confirming against your own
sandbox data, since the sample response used here contained only populated pages.

---

## 8. Quick checklist

- [ ] Dependency on `v1.0.24`, JitPack repository declared, build refreshed
- [ ] `curl` returns `data.collection` + `data.count`
- [ ] Example app endpoint returns the same payload
- [ ] Audit log shows `stage=GET_REQUEST_OPERATIONS` with the phone masked
- [ ] An executed row maps `getTypedOperationType()` to `CASHIN`/`CASHOUT`, not `UNKNOWN`
- [ ] A pending row reports `isOpen() == true` and `getOperation() == null`
- [ ] Amounts read from `operation`, not from the request item
- [ ] `count` used for pagination, not `collection.size()`
