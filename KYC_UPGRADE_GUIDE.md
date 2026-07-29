# KYC Upgrade Guide for Personal and Merchant Wallets

This guide explains how application developers should integrate KYC upgrades with the Chari BaaS Spring Boot starter. It covers:

- Personal wallet KYC upgrades with ShareID.
- Merchant wallet KYC for the owner or signatory and KYB for the business, using ShareID and document uploads.
- Backend endpoints your application should expose to mobile/web clients.
- Document upload format, validation rules, status tracking, and error handling.

The SDK methods used in this guide are exposed by `ChariBaasClient`.

## API Map

| Step | SDK method | Chari endpoint used by SDK | Purpose |
|---|---|---|---|
| Start ShareID session | `authenticateShareId(phoneNumber)` | `GET /api/kyc/shareid/auth?PhoneNumber=+212...` | Gets `baseUrl`, `applicantId`, and `token` for the ShareID SDK. |
| Confirm personal KYC upgrade | `confirmKyc(phoneNumber, accountLevel)` | `POST /api/customers/upgrade/request?PhoneNumber=+212...&AccountLevel=2` | Tells Chari that the personal KYC flow finished and requests the target account level. |
| Submit merchant KYC documents | `requestKyc(phoneNumber, documents)` | `POST /api/customers/merchant/kyc/request?phoneNumber=+212...` | Sends the owner or authorized signatory's identity documents as `multipart/form-data`. |
| Submit merchant KYB documents | `requestKyb(phoneNumber, documents)` | `POST /api/customers/merchant/kyb/request?phoneNumber=+212...` | Sends the merchant's business documents as `multipart/form-data`. |
| Check wallet state | `getCustomerStatus(phoneNumber)` | `GET /api/customers/status?phoneNumber=+212...` | Checks whether the wallet exists, needs OTP confirmation, is active, or is locked. |
| Check account level/review | `getCustomerInfo(phoneNumber)` | `GET /api/customers/info?phoneNumber=+212...` | Reads `accountLevel`, `levelInReview`, customer status, balance, and profile data. |

The SDK normalizes Moroccan local numbers such as `0612345678` to `+212612345678` before calling Chari.

## Wallet Types

Use the correct wallet type during registration:

| Wallet type | SDK enum | API value | Meaning |
|---|---|---|---|
| Personal wallet | `WalletType.P` | `P` | Individual customer wallet. |
| Merchant wallet | `WalletType.C` | `C` | Business or professional wallet. |

KYC upgrade should be started only after the wallet exists and the OTP registration flow is complete. In most applications, the user should also have a PIN and an active wallet before paid operations are enabled.

## Account Levels

The SDK exposes target levels in `ChariAccountLevel`.

| Level | SDK value | Code | Requirement summary | Balance limit |
|---|---:|---:|---|---|
| Basic | `BASIC` | `1` | Name, valid phone number, CIN number. | `1 000 MAD` |
| KYC level 2 | `KYC_LEVEL_2` | `2` | Full KYC through ShareID, usually CIN plus selfie or document scan. | `4 000 MAD` |
| KYC level 3 | `KYC_LEVEL_3` | `3` | Verified ID plus interview and digital customer record. | `20 000 MAD` |
| KYC level 4 | `KYC_LEVEL_4` | `4` | Full KYC plus interview, digital customer record, proof of income, and proof of address. | `100 000 MAD` |
| Merchant | `MERCHANT` | Chari-managed | Full KYB and business registration documents. | Negotiated |

For personal wallets, pass the target numeric level through `confirmKyc`. For merchant wallets, submit the owner or signatory's identity files with `requestKyc`, then submit the business files with `requestKyb`. Confirm the exact business account target and required document types with Chari.

## Personal Wallet KYC Flow

Use this flow for a wallet registered with `WalletType.P`.

### 1. Verify Wallet Readiness

Before launching ShareID, check the wallet state:

```java
ChariCustomerStatusResponse status = chari.getCustomerStatus("0612345678");

if (status.getData().needsRegistration()) {
    // Register the customer first.
}

if (status.getData().needsOtpConfirmation()) {
    // Ask the user for the OTP and call confirmCustomer(...).
}

if (status.getData().isLocked()) {
    // Stop the flow and ask support or Chari operations to review the account.
}
```

If the customer is not registered yet, register and confirm the wallet:

```java
ChariRegisterCustomerPayload registerPayload = ChariRegisterCustomerPayload.builder()
        .phoneNumber("0612345678")
        .firstName("Mohammed")
        .lastName("Chairi")
        .cin("K000000")
        .walletType(WalletType.P)
        .build();

chari.registerCustomer(registerPayload);

ChariCustomerConfirmPayload confirmPayload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365768")
        .walletType(WalletType.P)
        .autoActivate(true)
        .build();

chari.confirmCustomer(confirmPayload);
```

### 2. Create a ShareID Session

Your backend should request a ShareID session only when the user is ready to start identity verification. The token is sensitive and should not be logged or stored longer than needed.

```java
ChariShareIdAuthResponse auth = chari.authenticateShareId("0612345678");

String baseUrl = auth.getData().getBaseUrl();
String applicantId = auth.getData().getApplicantId();
String token = auth.getData().getToken();
```

Return only the fields needed by the mobile/web app:

```json
{
  "baseUrl": "https://v2.shareid.net",
  "applicantId": "a-o-251103-142417-pro-0003-c46cfaf1a",
  "token": "eyJhbGciO..."
}
```

Recommended application endpoint:

```text
POST /api/wallets/{phoneNumber}/kyc/shareid-session
```

The mobile/web app uses the returned `baseUrl`, `applicantId`, and `token` to launch the ShareID SDK. Keep the Chari API key on your backend only.

### 3. Run ShareID in the App

The application should:

1. Launch the ShareID SDK with `baseUrl`, `applicantId`, and `token`.
2. Ask the customer to complete the identity capture screens.
3. Receive the ShareID success/failure result from the ShareID SDK.
4. Send the result to your backend.
5. Store the local KYC state for support and audit, for example `SHAREID_STARTED`, `SHAREID_COMPLETED`, or `SHAREID_FAILED`.

Do not call `confirmKyc` before ShareID reports success. If the user abandons the flow, create a new ShareID session when they retry.

### 4. Confirm the Upgrade Request

After ShareID succeeds, call `confirmKyc` with the target level:

```java
ChariBooleanResponse response = chari.confirmKyc(
        "0612345678",
        ChariAccountLevel.KYC_LEVEL_2);

if (Boolean.TRUE.equals(response.getData())) {
    // Mark the local KYC request as submitted or under review.
}
```

Recommended application endpoint:

```text
POST /api/wallets/{phoneNumber}/kyc/personal/confirm
```

Request body example:

```json
{
  "targetAccountLevel": 2,
  "applicantId": "a-o-251103-142417-pro-0003-c46cfaf1a"
}
```

`applicantId` is not sent by the SDK to Chari during `confirmKyc`, but it is useful for your own support and traceability.

### 5. Track Review Status

After submitting the request, call `getCustomerInfo` to read the current level and the level in review:

```java
ChariCustomerInfoResponse info = chari.getCustomerInfo("0612345678");

ChariAccountLevel currentLevel = info.getData().getCurrentAccountLevel();
ChariAccountLevel levelInReview = info.getData().getLevelInReviewValue();
```

Use these fields to drive your UI:

| Local UI state | Suggested condition |
|---|---|
| KYC not started | No local ShareID session and no `levelInReview`. |
| ShareID in progress | A ShareID session was created but no success result was received. |
| Upgrade under review | `confirmKyc` returned `data=true`, or `levelInReview` is the requested level. |
| Upgrade approved | `accountLevel` is equal to or higher than the requested level. |
| Needs support | Chari returns a business error, locked account, or the review does not progress within your operational SLA. |

The SDK does not expose a separate KYC status endpoint. Use `getCustomerInfo`, local state, and any operational callbacks agreed with Chari.

## Merchant Wallet KYB/KYC Flow

Use this flow for a wallet registered with `WalletType.C`.

Merchant onboarding normally has three parts:

1. Verify the identity of the owner or authorized signatory through ShareID.
2. Submit the owner or authorized signatory's identity documents through the merchant KYC endpoint.
3. Submit the merchant's business documents through the KYB endpoint.

### 1. Register and Confirm the Merchant Wallet

```java
ChariRegisterCustomerPayload registerPayload = ChariRegisterCustomerPayload.builder()
        .phoneNumber("0612345678")
        .firstName("Sara")
        .lastName("Alami")
        .cin("K000000")
        .walletType(WalletType.C)
        .build();

chari.registerCustomer(registerPayload);

ChariCustomerConfirmPayload confirmPayload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365768")
        .walletType(WalletType.C)
        .autoActivate(true)
        .build();

chari.confirmCustomer(confirmPayload);
```

### 2. Verify the Signatory with ShareID

Use the same ShareID session flow as personal wallets:

```java
ChariShareIdAuthResponse auth = chari.authenticateShareId("0612345678");
```

The mobile/web app launches ShareID with the returned credentials. When ShareID succeeds, keep the `applicantId` in your local merchant onboarding record.

### 3. Collect Required Merchant Documents

Separate the files according to the request that will receive them:

| Request | Files |
|---|---|
| Merchant KYC | The owner or contract signatory's national identity card, passport, or other identity files required by Chari. |
| Merchant KYB | Bank account proof and all documents that establish and authorize the business. |

Every file is uploaded separately with the document type supplied by Chari. Bank account proof can be a RIB, bank-account certificate, void cheque, or cheque specimen, subject to Chari's requirements.

Additional documents depend on the merchant legal type:

| Professional client type | Required documents |
|---|---|
| Legal entity, company, or organization | Company articles/statutes; latest General Assembly minutes confirming signing authority when the manager or legal representative is not the sole signatory named in the statutes; Commercial Register certificate issued less than 90 days ago; Professional Tax registration certificate, also called Patente. |
| Individual professional, auto-entrepreneur, freelancer, or sole proprietor | Auto-entrepreneur card or professional registration document; Professional Tax registration certificate, also called Patente; Commercial Register certificate issued less than 90 days ago and company statutes when applicable. |
| Foundation or association | Latest General Assembly minutes confirming signing authority when the authorized representative is not clearly named in the statutes; list of authorized representatives or board members; association or foundation statute. |

Confirm any document type codes not represented by `ChariDocumentType` with Chari before submission.

### 4. Map Files to Chari Document Types

The SDK supports these document types:

| SDK enum | Code | Description | `DocBack` required |
|---|---:|---|---|
| `IdentityCard` | `1` | National identity card | Yes |
| `DrivingLicense` | `2` | Driving license | Yes |
| `Passport` | `3` | Passport | No |
| `ResidencePermit` | `4` | Residence permit | Yes |
| `ProofOfIncome` | `5` | Proof of income | No |
| `ProofOfResidence` | `6` | Proof of residence | No |
| `Selfie` | `7` | Selfie or face photo | No |
| `CommercialRegister` | `8` | Commercial register | No |

The SDK validates that:

- At least one document is present.
- Each document has a `DocType`.
- Each document has a `DocFront`.
- `DocBack` is present for `IdentityCard`, `DrivingLicense`, and `ResidencePermit`.

### 5. Build the Shared Multipart Parameters

`requestKyc` and `requestKyb` accept exactly the same Java parameters:

```java
requestKyc(String phoneNumber, List<KycDocument> documents)
requestKyb(String phoneNumber, List<KycDocument> documents)
```

Both methods also accept a `ChariMerchantKycUploadPayload` containing `phoneNumber` and `kycDocuments`. The payload class name is retained for SDK compatibility and can be used for either request.

For files already stored on disk:

```java
List<ChariMerchantKycUploadPayload.KycDocument> kycDocuments = List.of(
        ChariMerchantKycUploadPayload.KycDocument.builder()
                .docType(ChariDocumentType.IdentityCard)
                .docFront(new FileSystemResource("/secure/kyc/cin-front.jpg"))
                .docBack(new FileSystemResource("/secure/kyc/cin-back.jpg"))
                .build());

List<ChariMerchantKycUploadPayload.KycDocument> kybDocuments = List.of(
        ChariMerchantKycUploadPayload.KycDocument.builder()
                .docType(ChariDocumentType.CommercialRegister)
                .docFront(new FileSystemResource("/secure/kyb/commercial-register.pdf"))
                .build());
```

For files received from a Spring `MultipartFile`, use a `Resource` that keeps the original filename:

```java
private Resource toResource(MultipartFile file) throws IOException {
    return new ByteArrayResource(file.getBytes()) {
        @Override
        public String getFilename() {
            return file.getOriginalFilename();
        }
    };
}
```

For large files, write the upload to a controlled temporary location and use `FileSystemResource` instead of loading the full file into memory.

### 6. Submit Merchant KYC and KYB

Submit the owner or authorized signatory's identity documents to the KYC endpoint:

```java
ChariBooleanResponse kycResponse = chari.requestKyc(
        "0612345678",
        kycDocuments);

if (Boolean.TRUE.equals(kycResponse.getData())) {
    // Mark merchant KYC as submitted or under review.
}
```

The SDK sends:

```text
POST /api/customers/merchant/kyc/request?phoneNumber=+212612345678
Content-Type: multipart/form-data
```

Submit the business documents to the KYB endpoint using the same parameter format:

```java
ChariMerchantKycUploadPayload kybPayload = ChariMerchantKycUploadPayload.builder()
        .phoneNumber("0612345678")
        .kycDocuments(kybDocuments)
        .build();

ChariBooleanResponse kybResponse = chari.requestKyb(kybPayload);

if (Boolean.TRUE.equals(kybResponse.getData())) {
    // Mark merchant KYB as submitted or under review.
}
```

The SDK sends:

```text
POST /api/customers/merchant/kyb/request?phoneNumber=+212612345678
Content-Type: multipart/form-data
```

`uploadMerchantKycDocuments(...)` remains available as a compatibility alias for `requestKyc(...)`, but new integrations should use the explicit `requestKyc` and `requestKyb` methods.

Recommended application endpoints:

```text
POST /api/wallets/{phoneNumber}/kyc/merchant/documents
Content-Type: multipart/form-data

POST /api/wallets/{phoneNumber}/kyb/documents
Content-Type: multipart/form-data
```

Recommended incoming fields for the merchant KYC request:

```text
documents[0].docType=1
documents[0].front=<cin-front.jpg>
documents[0].back=<cin-back.jpg>
```

The merchant KYB request uses the same incoming field names with its own business-document list:

```text
documents[0].docType=8
documents[0].front=<commercial-register.pdf>
```

Your backend maps each request's fields to SDK `KycDocument` objects and calls the matching method. Both SDK calls send Chari indexed multipart fields in this exact shape:

```text
KycDocuments[0].DocType
KycDocuments[0].DocFront
KycDocuments[0].DocBack
```

### 7. Track Merchant Review Status

After each of `requestKyc` and `requestKyb` returns `data=true`, track that part of the merchant application as submitted. Use `getCustomerInfo` to check:

- `accountLevel`: the current approved level.
- `levelInReview`: the requested level currently under Chari review.
- `customerStatus` or `status`: the customer account status.

Keep your local merchant onboarding record with:

- Wallet phone number.
- Wallet type `C`.
- ShareID `applicantId`.
- Submitted KYC and KYB document lists with document type, filename, and checksum.
- Separate KYC and KYB submission timestamps.
- Each Chari request result.
- Current internal review state.

Do not store raw document files longer than required by your legal and operational policy.

## Suggested Backend Contract for App Developers

Expose your own backend endpoints instead of letting the mobile/web app call Chari directly.

| App endpoint | Called by | Backend action |
|---|---|---|
| `GET /api/wallets/{phoneNumber}/kyc/status` | Mobile/web app | Calls `getCustomerInfo` and returns current KYC/KYB state from Chari plus your local state. |
| `POST /api/wallets/{phoneNumber}/kyc/shareid-session` | Mobile/web app | Calls `authenticateShareId` and returns ShareID launch credentials. |
| `POST /api/wallets/{phoneNumber}/kyc/personal/confirm` | Mobile/web app after ShareID success | Validates local ShareID result, then calls `confirmKyc`. |
| `POST /api/wallets/{phoneNumber}/kyc/merchant/documents` | Mobile/web app after signatory files are selected | Validates identity files and document types, then calls `requestKyc`. |
| `POST /api/wallets/{phoneNumber}/kyb/documents` | Mobile/web app after business files are selected | Validates business files and document types, then calls `requestKyb`. |
| `POST /api/wallets/{phoneNumber}/kyc/retry` | Mobile/web app | Clears abandoned local state and creates a new ShareID session or document request when allowed. |

The app should never receive the Chari API key and should never call Chari endpoints directly.

## End-to-End Developer Checklist

1. Configure `chari.baas.base-url` and `chari.baas.api-key` on the backend.
2. Register the customer with `WalletType.P` for personal wallets or `WalletType.C` for merchant wallets.
3. Confirm the customer OTP with `confirmCustomer`.
4. Check wallet state with `getCustomerStatus` and `getCustomerInfo`.
5. Add a backend endpoint that creates ShareID sessions through `authenticateShareId`.
6. Integrate the ShareID SDK in the mobile/web app using `baseUrl`, `applicantId`, and `token`.
7. Save local ShareID state and `applicantId`.
8. For personal wallets, call `confirmKyc(phoneNumber, ChariAccountLevel.KYC_LEVEL_2)` or the target level agreed with Chari.
9. For merchant wallets, collect and validate the owner or signatory's identity files, map them to `ChariDocumentType`, and call `requestKyc`.
10. Collect and validate the merchant's business files using the same parameter format, then call `requestKyb`.
11. Handle `data=true` as "submitted", not immediately approved.
12. Poll or refresh `getCustomerInfo` to display `accountLevel` and `levelInReview`.
13. Block duplicate submissions while an upgrade is under review.
14. Keep audit records without logging ShareID tokens, API keys, or raw personal documents.
15. Agree operational SLAs, rejection handling, and additional document codes with Chari before production launch.

## Error Handling

The SDK throws `ChariBaasException` for Chari API errors. For local validation errors, `requestKyc` and `requestKyb` can throw `IllegalArgumentException` before the HTTP call.

| Error | Meaning | Recommended app behavior |
|---|---|---|
| `ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW` / code `32000` | Another KYC/KYB upgrade request is already under review. | Do not resubmit. Show "under review" and refresh status with `getCustomerInfo`. |
| `ChariErrorCode.INVALID_PHONE_NUMBER_FORMAT` / code `20000` | Phone number format is invalid. | Ask the user to correct the phone number. |
| `ChariErrorCode.USER_NOT_FOUND` / code `20005` | Wallet does not exist for the phone number. | Start registration before KYC. |
| `ChariErrorCode.MISSING_PARAMETERS` / code `10001` | Required request data is missing. | Check server-side validation and logs. |
| `ChariErrorCode.UNAUTHORIZED` / code `401` | API key is invalid or not authorized. | Stop the flow and fix backend configuration. |
| `ChariErrorCode.ACCOUNT_LOCKED` / code `423` | Customer wallet is locked. | Stop the flow and escalate to support. |
| Timeout or connection error | Chari API could not be reached. | Retry with backoff. Do not create duplicate submissions without checking status. |
| `IllegalArgumentException` for `DocBack` | A two-sided document is missing the back image. | Ask the user to upload both sides of the document. |

Example:

```java
try {
    chari.confirmKyc("0612345678", ChariAccountLevel.KYC_LEVEL_2);
} catch (ChariBaasException ex) {
    if (ex.hasErrorCode(ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW)) {
        // Return HTTP 409 from your backend and show an under-review state.
    } else if (ex.isAuthenticationFailure()) {
        // Alert backend operations. The Chari API key is not valid.
    } else {
        // Log request stage and known error code without sensitive payloads.
    }
}
```

## File Validation Rules for Your Application

The SDK validates required multipart fields, but the application should validate user uploads before calling the SDK:

- Accept only approved file types, for example PDF, JPEG, and PNG, as agreed with Chari.
- Set a maximum file size.
- Virus-scan uploaded files when your platform requires it.
- Reject empty files.
- Keep original filenames or assign traceable generated filenames.
- Do not expose uploaded files through public URLs.
- Delete temporary files after successful submission unless retention is required.
- Hash each file and store the checksum for support.

## Production Readiness

Before production, confirm these items with Chari:

- Target account levels available to your application.
- ShareID SDK success/failure callback format for your mobile/web platform.
- Accepted merchant document formats and maximum sizes.
- Document type codes for Patente, statutes, General Assembly minutes, RIB proof, auto-entrepreneur documents, and association documents if they are not covered by `ChariDocumentType`.
- Review SLA and rejection process.
- Whether any webhook events should be used in addition to `getCustomerInfo`.

## Minimal Sequence Summary

Personal wallet:

```text
Register wallet as P
Confirm OTP
Create ShareID session
Run ShareID in app
On ShareID success, call confirmKyc
Track accountLevel and levelInReview
```

Merchant wallet:

```text
Register wallet as C
Confirm OTP
Create ShareID session for signatory
Run ShareID in app
Collect signatory KYC and merchant KYB files
Submit signatory identity files with requestKyc
Submit business files with requestKyb
Track accountLevel and levelInReview
```
