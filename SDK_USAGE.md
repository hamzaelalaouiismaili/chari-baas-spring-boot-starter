# Chari BaaS SDK Usage Guide

This guide shows how to use `ChariBaasClient`, the main public facade exposed by the Spring Boot starter.

Official API reference: https://baas.ma/en/api-docs

This guide follows the ChariBaaS API documentation version 1.9, last updated 14 April 2026.

## Installation

For JitPack:

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

For local development, install the SDK first:

```bash
JAVA_HOME=/path/to/jdk-21 mvn clean install
```

Then use version `1.0.1` in the consuming project.

## Configuration

```yaml
chari:
  baas:
    base-url: ${CHARI_BASE_URL:https://sandbox.charimoney.com}
    api-key: ${CHARI_API_KEY}
    webhook-secret: ${CHARI_WEBHOOK_SECRET:}
    timeout-ms: 10000
    principal-agent-id: ${CHARI_PRINCIPAL_AGENT_ID:}
    principal-agent-rib: ${CHARI_PRINCIPAL_AGENT_RIB:}
    card-funding:
      accept-url: ${CHARI_ACCEPT_URL:}
      decline-url: ${CHARI_DECLINE_URL:}
    webhook:
      enabled: true
      path: /webhooks/chari
    audit:
      enabled: true
      mask-sensitive: true
```

The SDK sends these headers automatically:

```text
Chari-Api-Key: <configured API key>
C-Request-Id: <generated UUID>
```

Use HTTPS URLs for `accept-url`, `decline-url`, and webhook endpoints in production.

## Inject The Client

```java
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final ChariBaasClient chari;

    public WalletService(ChariBaasClient chari) {
        this.chari = chari;
    }
}
```

## Customer Registration

```java
ChariCustomerStatusResponse status = chari.getCustomerStatus("+2126xxxxxxxx");
ChariDefaultWalletResponse defaultWallet = chari.checkDefaultWallet("+2126xxxxxxxx");
boolean isDefault = chari.isDefaultWallet("+2126xxxxxxxx");
ChariBalanceResponse balance = chari.getCustomerBalance("+2126xxxxxxxx");
ChariCustomerInfoResponse info = chari.getCustomerInfo("+2126xxxxxxxx");
```

Register a customer:

```java
ChariBooleanResponse response = chari.registerCustomer(
        ChariRegisterCustomerPayload.builder()
                .phoneNumber("+2126xxxxxxxx")
                .firstName("Mohammed")
                .lastName("Chairi")
                .cin("K000000")
                .walletType("P")
                .closeLoopOnly(false)
                .build()
);
```

Confirm OTP:

```java
ChariBooleanResponse response = chari.confirmCustomer(
        ChariCustomerConfirmPayload.builder()
                .phoneNumber("+2126xxxxxxxx")
                .code("365-768")
                .walletType("P")
                .autoActivate(false)
                .build()
);
```

The SDK accepts both `365-768` and `365768`, and sends the OTP in the `XXX-XXX` format expected by the API.

PIN operations:

```java
chari.resendCustomerOtp("+2126xxxxxxxx");
chari.loginWithPin("+2126xxxxxxxx", "0000");
chari.createPin("+2126xxxxxxxx", "0000");
chari.updatePin("+2126xxxxxxxx", "0000", "1111");
```

Unregister:

```java
chari.unregisterCustomer("+2126xxxxxxxx", ChariClosureReason.CLIENT_CONTRACT_CLOSURE);
```

## KYC

ShareID authentication:

```java
ChariShareIdAuthResponse auth = chari.authenticateShareId("+2126xxxxxxxx");
```

Confirm KYC upgrade:

```java
chari.confirmKyc("+2126xxxxxxxx", ChariAccountLevel.KYC_LEVEL_2);
```

Merchant KYC upload:

```java
KycDocument document = KycDocument.builder()
        .docType(ChariDocumentType.IdentityCard.getCode())
        .docFront(frontMultipartFile)
        .docBack(backMultipartFile)
        .build();

chari.uploadMerchantKycDocuments("+2126xxxxxxxx", List.of(document));
```

`docBack` is required for identity card, driving license, and residence permit.

## Transfers

Wallet-to-wallet transfer preview and execution:

```java
ChariTransferPayload payload = ChariTransferPayload.builder()
        .customerPhoneNumber("+2126xxxxxxxx")
        .recipientPhoneNumber("+2127xxxxxxxx")
        .amount(new BigDecimal("10"))
        .reason("test transfer")
        .build();

ChariTransferPreviewResponse preview = chari.previewTransfer(payload);
ChariTransferResponse executed = chari.executeTransfer(payload);
```

Bank transfer:

```java
ChariBankTransferPayload payload = ChariBankTransferPayload.builder()
        .customerPhoneNumber("+2126xxxxxxxx")
        .amount(new BigDecimal("10"))
        .reason("bank transfer")
        .rib("827640000010000000001234")
        .beneficiaryName("Beneficiary Name")
        .build();

chari.previewBankTransfer(payload, "+2126xxxxxxxx");
chari.executeBankTransfer(payload, "+2126xxxxxxxx");
```

Principal-agent bank transfer:

```java
chari.previewBankTransferFromAP(payload);
chari.executeBankTransferFromAP(payload);
```

## Card Cash-In

Preview by phone:

```java
chari.previewCardFunding("+2126xxxxxxxx", new BigDecimal("100"));
```

Execute with a new card:

```java
ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("4918914107195005")
        .cvv("123")
        .expiryDate("08/26")
        .amount(new BigDecimal("100"))
        .keepAlive(true)
        .cardName("sandbox_card")
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

ChariCardFundingExecutionResponse result =
        chari.executeCardFunding("+2126xxxxxxxx", payload);
```

If `result.getData().getRedirect()` is true, redirect the user to `redirectionURL`.

Execute with a saved card:

```java
ChariSavedCardCashinPayload payload = ChariSavedCardCashinPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("200"))
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

chari.cashinWithSavedCard(123, "+2126xxxxxxxx", payload);
```

Sandbox test card:

```text
PAN: 4918914107195005
CVV: 123
Expiry: 08/26 or future date
3DS code: 555
```

## Merchant Payments

By phone:

```java
ChariMerchantPaymentByPhonePayload payload = ChariMerchantPaymentByPhonePayload.builder()
        .customerPhoneNumber("+2126xxxxxxxx")
        .recipientPhoneNumber("+2127xxxxxxxx")
        .amount(new BigDecimal("10"))
        .reason("merchant payment")
        .build();

chari.previewMerchantPaymentByPhone(payload);
chari.executeMerchantPaymentByPhone(payload);
```

By QR code:

```java
ChariQrCodePaymentPayload payload = ChariQrCodePaymentPayload.builder()
        .customerPhoneNumber("+2126xxxxxxxx")
        .qrCodeContent("00020101021126xxxxxx")
        .amount(new BigDecimal("10"))
        .build();

chari.previewQrCodePayment(payload);
chari.executeQrCodePayment(payload);
```

By card:

```java
ChariMerchantCardPaymentPayload payload = ChariMerchantCardPaymentPayload.builder()
        .firstName("John")
        .lastName("Doe")
        .cvv("123")
        .amount(new BigDecimal("250"))
        .pan("4918914107195005")
        .expiryDate("08/26")
        .keepAlive(true)
        .threeDSecure(true)
        .autoCapture(true)
        .notificationUrl("https://merchant.example.com/webhook")
        .acceptUrl("https://merchant.example.com/success")
        .declineUrl("https://merchant.example.com/fail")
        .externalReference("ORDER-1001")
        .build();

chari.previewMerchantCardPayment("+2126xxxxxxxx", new BigDecimal("250"));
chari.executeMerchantCardPayment("+2126xxxxxxxx", payload);
```

By tokenized card:

```java
ChariMerchantTokenizedCardPaymentPayload payload =
        ChariMerchantTokenizedCardPaymentPayload.builder()
                .cvv("123")
                .amount(new BigDecimal("188"))
                .build();

chari.executeMerchantTokenizedCardPayment(277, "+2126xxxxxxxx", payload);
```

QR generation:

```java
chari.generateStaticQrCode("+2126xxxxxxxx");
chari.generateStaticQrCode("+2126xxxxxxxx", true);

chari.generateQrCode(
        "+2126xxxxxxxx",
        ChariGenerateQrCodePayload.builder()
                .amount(new BigDecimal("100"))
                .build()
);
```

## Chargeback And Refund

Chargeback preview:

```java
ChariChargebackPayload payload = ChariChargebackPayload.builder()
        .sourcePhoneNumber("+2126xxxxxxxx")
        .destinationPhoneNumber("+2127xxxxxxxx")
        .amount(new BigDecimal("10"))
        .description("chargeback reason")
        .originalOperationId(12345L)
        .build();

chari.previewChargeback(payload);
```

Refund:

```java
ChariRefundPayload payload = ChariRefundPayload.builder()
        .phoneNumber("+2126xxxxxxxx")
        .operationId(1234L)
        .refundAmount(new BigDecimal("200"))
        .orderId("b981430df1")
        .transactionTrackId("230648378573")
        .build();

chari.previewRefund(payload);
chari.executeRefund(payload);
```

## Beneficiaries

```java
chari.getBeneficiaries("+2126xxxxxxxx");

ChariBeneficiaryResponse beneficiary = chari.addBeneficiary(
        "+2126xxxxxxxx",
        ChariBeneficiaryPayload.builder()
                .name("Test")
                .phoneNumber("+2127xxxxxxxx")
                .email("user@example.com")
                .build()
);

chari.deleteBeneficiary(beneficiary.getData().getId(), "+2126xxxxxxxx");
```

At least one of beneficiary `phoneNumber` or `rib` should be provided.

## Tokenized Cards

```java
chari.listSavedCards("+2126xxxxxxxx");
chari.listSavedCards("+2126xxxxxxxx", 5, 1);
chari.getSavedCard(48, "+2126xxxxxxxx");
chari.deleteSavedCard(48, "+2126xxxxxxxx");
```

## Request Operations

Cash-in and cash-out by reference:

```java
ChariCashinByReferenceResponse cashin = chari.requestCashinByReference(
        ChariCashinByReferencePayload.builder()
                .phoneNumber("+2126xxxxxxxx")
                .amount(new BigDecimal("10"))
                .build()
);

chari.getCashinByReference(cashin.getData().getReference());

chari.executeCashinByReference(
        ChariExecuteRequestOperationByReferencePayload.builder()
                .code("123")
                .reference(cashin.getData().getReference())
                .build()
);
```

Cash-out uses the same pattern:

```java
chari.requestCashoutByReference(cashoutPayload);
chari.getCashoutByReference("1122334456");
chari.executeCashoutByReference(executePayload);
```

## Retail And Principal Agents

```java
chari.getRetailAgents("123", 10, 1);
chari.getRetailAgentByCode("123");

chari.addRetailAgent(
        ChariRetailAgentPayload.builder()
                .phoneNumber("+2126xxxxxxxx")
                .name("Agent Detaillant Tanger")
                .firstName("Agent")
                .lastName("Detaillant")
                .cin("AB123456")
                .email("agent@example.com")
                .address("10, Nejma Tanger")
                .build()
);

chari.getPrincipalAgentInfo("11098");
chari.getPrincipalAgentWallet();
```

## Operations History

Get customer operations:

```java
ChariOperationsByCustomerQuery query = ChariOperationsByCustomerQuery.builder()
        .phoneNumber("+2126xxxxxxxx")
        .pageSize(10)
        .pageNumber(1)
        .operationTypes(List.of(ChariOperationType.CASHIN, ChariOperationType.CASHOUT))
        .transactionStatus(ChariOperationStatus.COMPLETED)
        .sens(ChariSens.CREDIT)
        .build();

chari.getOperationsByCustomer(query);
```

Get partner operations:

```java
chari.getAllOperationsByPartner(query);
```

Get operation by ID:

```java
chari.getOperationById(123L, "+2126xxxxxxxx");
```

## Webhooks

The starter registers this endpoint by default:

```text
POST /webhooks/chari
```

Implement `ChariWebhookHandler` and override only the events your app needs:

```java
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookEvent.WebhookData;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import org.springframework.stereotype.Component;

@Component
public class MyChariWebhookHandler implements ChariWebhookHandler {

    @Override
    public void onPaymentReceived(WebhookData data) {
        // update local order or ledger
    }

    @Override
    public void onCashInCardAuthorized(WebhookData data) {
        // reconcile card cash-in
    }

    @Override
    public void onBankTransferCompleted(WebhookData data) {
        // inspect data.getOperationStatus()
    }
}
```

Configure `chari.baas.webhook-secret` in production to enable signature verification.

## Error Handling

Non-2xx Chari responses throw `ChariBaasException`.

```java
try {
    chari.getCustomerStatus("+2126xxxxxxxx");
} catch (ChariBaasException e) {
    Integer httpStatus = e.getHttpStatusCode();
    Integer errorCode = e.getErrorCode();
    String description = e.getErrorDescription();
    ChariErrorCode known = e.getKnownErrorCode();

    if (e.isAuthenticationFailure()) {
        // check API key
    } else if (e.isValidationError()) {
        // fix payload
    } else if (e.isAccountLocked()) {
        // stop retries and contact support
    }
}
```

## Enums

The SDK includes typed enums for Chari integer codes:

```text
ChariCustomerStatus
ChariAccountLevel
ChariOperationType
ChariTransactionType
ChariOperationStatus
ChariSens
ChariClosureReason
ChariDocumentType
ChariRequestOperationType
ChariRequestOperationStatus
WalletType
```

Prefer these enums over hard-coded integer codes where builder overloads or response helpers are available.

## Public Method Reference

| Area | Methods |
|---|---|
| Customer | `getCustomerStatus`, `checkDefaultWallet`, `isDefaultWallet`, `getCustomerInfo`, `getCustomerBalance`, `registerCustomer`, `confirmCustomer`, `resendCustomerOtp`, `loginWithPin`, `setCustomerPin`, `createPin`, `updatePin`, `unregisterCustomer` |
| KYC | `authenticateShareId`, `confirmKyc`, `uploadMerchantKycDocuments` |
| Wallet and agents | `getWallet`, `getPrincipalAgentWallet`, `getPrincipalAgentInfo`, `getConfiguredPrincipalAgentId`, `getPrincipalAgentId`, `getPrincipalAgentRib` |
| Transfer | `previewTransfer`, `executeTransfer` |
| Bank transfer | `previewBankTransfer`, `executeBankTransfer`, `previewBankTransferFromAP`, `executeBankTransferFromAP` |
| Card cash-in | `previewCardFunding`, `previewCardFundingByAgent`, `executeCardFunding`, `executeCardFundingByAgent`, `cashinWithSavedCard` |
| Merchant payments | `previewMerchantPaymentByPhone`, `executeMerchantPaymentByPhone`, `previewQrCodePayment`, `executeQrCodePayment`, `previewMerchantCardPayment`, `executeMerchantCardPayment`, `executeMerchantTokenizedCardPayment` |
| QR generation | `generateStaticQrCode`, `generateQrCode` |
| Chargeback and refund | `previewChargeback`, `previewRefund`, `executeRefund` |
| Beneficiaries | `getBeneficiaries`, `addBeneficiary`, `deleteBeneficiary` |
| Tokenized cards | `saveCard`, `listSavedCards`, `getSavedCard`, `deleteSavedCard` |
| Request operations | `requestCashinByReference`, `requestCashoutByReference`, `getCashinByReference`, `executeCashinByReference`, `getCashoutByReference`, `executeCashoutByReference` |
| Retail agents | `getRetailAgents`, `getRetailAgentByCode`, `addRetailAgent` |
| Operation history | `getOperationsByCustomer`, `getAllOperationsByPartner`, `getOperationById` |
