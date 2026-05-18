# Chari Webhook Usage

The SDK already provides the webhook endpoint. In your app, you only need to configure it and implement `ChariWebhookHandler`.

## 1. Configure Webhooks

```yaml
chari:
  baas:
    webhook-secret: ${CHARI_WEBHOOK_SECRET}
    webhook:
      enabled: true
      path: /webhooks/chari
```

Default endpoint:

```text
POST /webhooks/chari
```

Chari sends:

```text
x-chari-signature: <hmac sha256 hex>
x-chari-timestamp: <epoch millis>
```

Signature verification uses:

```text
HMAC_SHA256(webhookSecret, timestamp + "." + rawBody)
```

If `chari.baas.webhook-secret` is blank, the SDK accepts webhooks without signature verification. Use a real secret in production.

## 2. Implement A Handler

Create a Spring bean that implements `ChariWebhookHandler`.

```java
package com.example.payments;

import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChariWebhookProcessor implements ChariWebhookHandler {

    private static final Logger log = LoggerFactory.getLogger(ChariWebhookProcessor.class);

    @Override
    public void onPaymentReceived(WebhookData data) {
        log.info("Payment received: operationId={}, amount={}, from={}, to={}",
                data.getOperationId(),
                data.getAmount(),
                data.getPrimaryAccountNumber(),
                data.getSecondaryAccountNumber());

        // TODO: mark your local merchant order as paid.
    }

    @Override
    public void onCashInCardAuthorized(WebhookData data) {
        log.info("Cash-in card authorized: operationId={}, orderId={}, trackId={}, referenceId={}",
                data.getOperationId(),
                data.getGatewayOrderId(),
                data.getGatewayTrackId(),
                data.getGatewayReferenceId());

        // TODO: reconcile the card cash-in in your local system.
    }

    @Override
    public void onBankTransferCompleted(WebhookData data) {
        log.info("Bank transfer completed: operationId={}, status={}, amount={}, reference={}",
                data.getOperationId(),
                data.getTypedOperationStatus(),
                data.getAmount(),
                data.getReference());

        // TODO: inspect data.getOperationStatus() before marking final success/failure.
    }

    @Override
    public void onTransferReceived(WebhookData data) {
        log.info("Transfer received: operationId={}, amount={}, sender={}, receiver={}",
                data.getOperationId(),
                data.getAmount(),
                data.getPrimaryAccountNumber(),
                data.getSecondaryAccountNumber());

        // TODO: credit local ledger if needed.
    }

    @Override
    public void onCashInNetworkExecuted(WebhookData data) {
        log.info("Network cash-in executed: reference={}, amount={}, phone={}",
                data.getReference(),
                data.getAmount(),
                data.getPrimaryAccountNumber());

        // TODO: mark cash-in by reference as completed.
    }

    @Override
    public void onCashOutNetworkExecuted(WebhookData data) {
        log.info("Network cash-out executed: reference={}, amount={}, phone={}",
                data.getReference(),
                data.getAmount(),
                data.getPrimaryAccountNumber());

        // TODO: mark cash-out by reference as completed.
    }

    @Override
    public void onUnknown(WebhookData data) {
        log.info("Unknown Chari webhook: eventId={}, operationType={}, operationId={}",
                data.getEventId(),
                data.getTypedOperationType(),
                data.getOperationId());
    }
}
```

## 3. Available Handler Methods

Override only the methods you need.

```java
onCustomerKyc(WebhookData data)
onCustomerLevelUpdated(WebhookData data)
onOperationCreated(WebhookData data)
onOperationUpdated(WebhookData data)
onCashInCardAuthorized(WebhookData data)
onPaymentCardAuthorized(WebhookData data)
onPaymentReceived(WebhookData data)
onBankTransferInitiated(WebhookData data)
onBankTransferCompleted(WebhookData data)
onBankTransferFailed(WebhookData data)
onBankTransferReceived(WebhookData data)
onTransferReceived(WebhookData data)
onCashInNetworkExecuted(WebhookData data)
onCashOutNetworkExecuted(WebhookData data)
onUnknown(WebhookData data)
```

Generic fallback methods also exist:

```java
onCashIn(WebhookData data)
onCashOut(WebhookData data)
onTransfer(WebhookData data)
onPaymentPush(WebhookData data)
onBankTransfer(WebhookData data)
onPaymentCard(WebhookData data)
onPaymentRefund(WebhookData data)
onChargeback(WebhookData data)
```

## 4. Webhook Data Fields

Useful fields on `WebhookData`:

```java
data.getWebhookId();
data.getEventId();
data.getEventType();
data.getCRequestId();
data.getOperationId();
data.getTransactionId();
data.getOperationType();
data.getTypedOperationType();
data.getOperationStatus();
data.getTypedOperationStatus();
data.getCreatedAt();
data.getExecutedAt();
data.getAmount();
data.getFeeAmount();
data.getPrimaryAccountNumber();
data.getSecondaryAccountNumber();
data.getMethod();
data.getReference();
data.getGatewayTrackId();
data.getGatewayOrderId();
data.getGatewayReferenceId();
data.getNetworkName();
```

## 5. Local Test Without Signature

For local testing only, leave `webhook-secret` blank:

```yaml
chari:
  baas:
    webhook-secret:
```

Then send a test payload:

```bash
curl -X POST "http://localhost:8080/webhooks/chari" \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "WebhookId": "wh_123",
      "EventId": "payment.received",
      "CRequestId": "69906411-0aa24a89-ab2005ca-9d18dc15",
      "OperationId": 2181,
      "TransactionId": 2710,
      "OperationType": 5,
      "OperationStatus": 2,
      "CreatedAt": "2026-05-18T10:00:00Z",
      "ExecutedAt": "2026-05-18T10:00:05Z",
      "Amount": 100,
      "FeeAmount": 0,
      "PrimaryAccountNumber": "+2126xxxxxxxx",
      "SecondaryAccountNumber": "+2127xxxxxxxx",
      "Method": "Wallet"
    }
  }'
```

Expected response:

```json
{
  "status": "accepted"
}
```

## 6. Production Checklist

- Set `chari.baas.webhook-secret`.
- Expose your webhook endpoint over HTTPS.
- Make webhook processing idempotent using `WebhookId`, `CRequestId`, or `OperationId`.
- Return HTTP 200 quickly; move slow work to a queue if needed.
- Do not log secrets, PAN, CVV, PIN, or full customer identifiers.
- For `bank-transfer.completed`, inspect `OperationStatus` before treating the transfer as successful.
- Store raw webhook metadata for reconciliation: `EventId`, `CRequestId`, `OperationId`, `TransactionId`, `OperationStatus`.

