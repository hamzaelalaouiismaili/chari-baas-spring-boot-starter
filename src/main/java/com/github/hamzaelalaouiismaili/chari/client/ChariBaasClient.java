package com.github.hamzaelalaouiismaili.chari.client;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariBeneficiaryClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariBillPaymentClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariCardManagementClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariCustomerRegistrationClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariKycClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariRetailAgentClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariTokenizedCardClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariTelcoTopUpClient;
import com.github.hamzaelalaouiismaili.chari.client.api.ChariVoucherClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariBankTransferClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariCashInCardClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariChargebackClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariMerchantPaymentClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariOperationsClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariRefundClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariNetworkOperationClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariRequestOperationClient;
import com.github.hamzaelalaouiismaili.chari.client.api.operations.ChariTransferClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariAccountLevel;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariClosureReason;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardAction;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoRechargeType;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantKycUploadPayload.KycDocument;
import com.github.hamzaelalaouiismaili.chari.model.payload.*;
import com.github.hamzaelalaouiismaili.chari.model.response.*;
import com.github.hamzaelalaouiismaili.chari.util.NumericIdentifierUtil;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Core service for communicating with Chari BaaS API.
 * This is the low-level HTTP client wrapper for all Chari API calls.
 */
@Slf4j
public class ChariBaasClient {

    private final ChariBaasProperties properties;
    private final ChariHttpClient httpClient;
    private final ChariCustomerRegistrationClient customerRegistrationClient;
    private final ChariKycClient kycClient;
    private final ChariBeneficiaryClient beneficiaryClient;
    private final ChariRetailAgentClient retailAgentClient;
    private final ChariTransferClient transferClient;
    private final ChariBankTransferClient bankTransferClient;
    private final ChariCashInCardClient cashInCardClient;
    private final ChariMerchantPaymentClient merchantPaymentClient;
    private final ChariChargebackClient chargebackClient;
    private final ChariTokenizedCardClient tokenizedCardClient;
    private final ChariRequestOperationClient requestOperationClient;
    private final ChariNetworkOperationClient networkOperationClient;
    private final ChariOperationsClient operationsClient;
    private final ChariRefundClient refundClient;
    private final ChariTelcoTopUpClient telcoTopUpClient;
    private final ChariVoucherClient voucherClient;
    private final ChariBillPaymentClient billPaymentClient;
    private final ChariCardManagementClient cardManagementClient;

    public ChariBaasClient(
            @Qualifier("chariBaasRestTemplate") RestTemplate restTemplate,
            ChariBaasProperties properties) {
        this.properties = properties;
        this.httpClient = new ChariHttpClient(restTemplate, properties);
        this.customerRegistrationClient = new ChariCustomerRegistrationClient(httpClient);
        this.kycClient = new ChariKycClient(httpClient);
        this.beneficiaryClient = new ChariBeneficiaryClient(httpClient);
        this.retailAgentClient = new ChariRetailAgentClient(httpClient);
        this.transferClient = new ChariTransferClient(httpClient);
        this.bankTransferClient = new ChariBankTransferClient(httpClient, properties);
        this.cashInCardClient = new ChariCashInCardClient(httpClient, properties);
        this.merchantPaymentClient = new ChariMerchantPaymentClient(httpClient);
        this.chargebackClient = new ChariChargebackClient(httpClient);
        this.tokenizedCardClient = new ChariTokenizedCardClient(httpClient);
        this.requestOperationClient = new ChariRequestOperationClient(httpClient);
        this.networkOperationClient = new ChariNetworkOperationClient(httpClient);
        this.operationsClient = new ChariOperationsClient(httpClient);
        this.refundClient = new ChariRefundClient(httpClient);
        this.telcoTopUpClient = new ChariTelcoTopUpClient(httpClient);
        this.voucherClient = new ChariVoucherClient(httpClient);
        this.billPaymentClient = new ChariBillPaymentClient(httpClient);
        this.cardManagementClient = new ChariCardManagementClient(httpClient);
    }

    // ==================== Customer Operations ====================

    /**
     * Get customer status by phone number.
     *
     * @param phoneNumber the customer's phone number
     * @return customer status response
     */
    public ChariCustomerStatusResponse getCustomerStatus(String phoneNumber) {
        return customerRegistrationClient.getCustomerStatus(phoneNumber);
    }

    /**
     * Check whether Chari is the customer's default wallet at Switch level.
     *
     * @param phoneNumber the customer's phone number
     * @return default wallet response
     */
    public ChariDefaultWalletResponse checkDefaultWallet(String phoneNumber) {
        return customerRegistrationClient.checkDefaultWallet(phoneNumber);
    }

    /**
     * Convenience helper for {@link #checkDefaultWallet(String)}.
     *
     * @param phoneNumber the customer's phone number
     * @return true when Chari is the customer's default wallet
     */
    public boolean isDefaultWallet(String phoneNumber) {
        return customerRegistrationClient.isDefaultWallet(phoneNumber);
    }

    /**
     * Get customer information by phone number.
     *
     * @param phoneNumber the customer's phone number
     * @return customer info response
     */
    public ChariCustomerInfoResponse getCustomerInfo(String phoneNumber) {
        return customerRegistrationClient.getCustomerInfo(phoneNumber);
    }

    // ==================== Beneficiary Operations ====================

    /**
     * Get beneficiaries for a customer.
     *
     * @param phoneNumber customer phone number
     * @return beneficiaries response
     */
    public ChariBeneficiariesResponse getBeneficiaries(String phoneNumber) {
        return beneficiaryClient.getBeneficiaries(phoneNumber);
    }

    /**
     * Add a beneficiary for a customer.
     *
     * @param phoneNumber customer phone number
     * @param payload     beneficiary payload
     * @return beneficiary response
     */
    public ChariBeneficiaryResponse addBeneficiary(String phoneNumber, ChariBeneficiaryPayload payload) {
        return beneficiaryClient.addBeneficiary(phoneNumber, payload);
    }

    /**
     * Delete a beneficiary for a customer.
     *
     * @param beneficiaryId beneficiary ID
     * @param phoneNumber   customer phone number
     * @return boolean response
     */
    public ChariBooleanResponse deleteBeneficiary(Long beneficiaryId, String phoneNumber) {
        return beneficiaryClient.deleteBeneficiary(beneficiaryId, phoneNumber);
    }

    /**
     * Get customer balance by phone number.
     *
     * @param phoneNumber the customer's phone number
     * @return balance response
     */
    public ChariBalanceResponse getCustomerBalance(String phoneNumber) {
        return customerRegistrationClient.getCustomerBalance(phoneNumber);
    }

    /**
     * Unregister a customer from the platform.
     *
     * @param phoneNumber the customer's phone number
     * @param reason      closure reason code
     * @return boolean response
     */
    public ChariBooleanResponse unregisterCustomer(String phoneNumber, ChariClosureReason reason) {
        return customerRegistrationClient.unregisterCustomer(phoneNumber, reason);
    }

    /**
     * Unregister a customer from the platform.
     *
     * @param payload unregister payload
     * @return boolean response
     */
    public ChariBooleanResponse unregisterCustomer(ChariUnregisterCustomerPayload payload) {
        return customerRegistrationClient.unregisterCustomer(payload);
    }

    // ==================== KYC Operations ====================

    /**
     * Obtain a short-lived ShareID token for launching the ShareID SDK, targeting
     * the default account level ({@link ChariAccountLevel#KYC_LEVEL_2}).
     *
     * @param phoneNumber customer phone number
     * @return ShareID auth response
     */
    public ChariShareIdAuthResponse authenticateShareId(String phoneNumber) {
        return kycClient.authenticateShareId(phoneNumber);
    }

    /**
     * Obtain a short-lived ShareID token for launching the ShareID SDK.
     *
     * @param phoneNumber  customer phone number
     * @param accountLevel account level the customer is upgrading to; falls back to
     *                     {@link ChariAccountLevel#KYC_LEVEL_2} when null or level-less
     * @return ShareID auth response
     */
    public ChariShareIdAuthResponse authenticateShareId(String phoneNumber, ChariAccountLevel accountLevel) {
        return kycClient.authenticateShareId(phoneNumber, accountLevel);
    }

    /**
     * Obtain a short-lived ShareID token for launching the ShareID SDK.
     *
     * @param phoneNumber  customer phone number
     * @param accountLevel account level code; falls back to level 2 when null
     * @return ShareID auth response
     */
    public ChariShareIdAuthResponse authenticateShareId(String phoneNumber, Integer accountLevel) {
        return kycClient.authenticateShareId(phoneNumber, accountLevel);
    }

    /**
     * Signal that the KYC flow finished and request an account-level upgrade.
     *
     * @param phoneNumber  customer phone number
     * @param accountLevel target account level code
     * @return boolean response
     */
    public ChariBooleanResponse confirmKyc(String phoneNumber, Integer accountLevel) {
        return kycClient.confirmKyc(phoneNumber, accountLevel);
    }

    /**
     * Signal that the KYC flow finished and request an account-level upgrade.
     */
    public ChariBooleanResponse confirmKyc(String phoneNumber, ChariAccountLevel accountLevel) {
        return kycClient.confirmKyc(phoneNumber, accountLevel);
    }

    /**
     * Upload merchant KYC documents to request an account upgrade.
     *
     * @param payload merchant KYC multipart payload
     * @return boolean response
     */
    public ChariBooleanResponse uploadMerchantKycDocuments(ChariMerchantKycUploadPayload payload) {
        return kycClient.uploadMerchantKycDocuments(payload);
    }

    /**
     * Upload merchant KYC documents to request an account upgrade.
     *
     * @param phoneNumber  merchant phone number
     * @param kycDocuments document list to upload
     * @return boolean response
     */
    public ChariBooleanResponse uploadMerchantKycDocuments(String phoneNumber, List<KycDocument> kycDocuments) {
        return kycClient.uploadMerchantKycDocuments(phoneNumber, kycDocuments);
    }

    /**
     * Submit a KYC request using the merchant verification document payload.
     *
     * @param payload KYC multipart payload
     * @return boolean response
     */
    public ChariBooleanResponse requestKyc(ChariMerchantKycUploadPayload payload) {
        return kycClient.requestKyc(payload);
    }

    /**
     * Submit a KYC request using the merchant verification document parameters.
     *
     * @param phoneNumber  customer phone number
     * @param kycDocuments document list to upload
     * @return boolean response
     */
    public ChariBooleanResponse requestKyc(String phoneNumber, List<KycDocument> kycDocuments) {
        return kycClient.requestKyc(phoneNumber, kycDocuments);
    }

    /**
     * Submit a KYB request using the same payload accepted by
     * {@link #requestKyc(ChariMerchantKycUploadPayload)}.
     *
     * @param payload KYB multipart payload
     * @return boolean response
     */
    public ChariBooleanResponse requestKyb(ChariMerchantKycUploadPayload payload) {
        return kycClient.requestKyb(payload);
    }

    /**
     * Submit a KYB request using the same parameters accepted by
     * {@link #requestKyc(String, List)}.
     *
     * @param phoneNumber  merchant phone number
     * @param kycDocuments document list to upload
     * @return boolean response
     */
    public ChariBooleanResponse requestKyb(String phoneNumber, List<KycDocument> kycDocuments) {
        return kycClient.requestKyb(phoneNumber, kycDocuments);
    }

    /**
     * Register a new customer.
     *
     * @param payload registration payload
     * @return boolean response
     */
    public ChariBooleanResponse registerCustomer(ChariRegisterCustomerPayload payload) {
        return customerRegistrationClient.registerCustomer(payload);
    }

    /**
     * Confirm customer registration with OTP.
     *
     * @param payload confirmation payload with OTP
     * @return boolean response
     */
    public ChariBooleanResponse confirmCustomer(ChariCustomerConfirmPayload payload) {
        return customerRegistrationClient.confirmCustomer(payload);
    }

    /**
     * Resend OTP to customer.
     *
     * @param phoneNumber the customer's phone number
     * @return boolean response
     */
    public ChariBooleanResponse resendCustomerOtp(String phoneNumber) {
        return customerRegistrationClient.resendCustomerOtp(phoneNumber);
    }

    /**
     * Authenticate an active customer with their 4-digit PIN.
     *
     * @param payload login payload
     * @return login result with remaining attempts
     */
    public ChariLoginWithPinResponse loginWithPin(ChariLoginWithPinPayload payload) {
        return customerRegistrationClient.loginWithPin(payload);
    }

    /**
     * Convenience overload for customer PIN login.
     */
    public ChariLoginWithPinResponse loginWithPin(String phoneNumber, String pin) {
        return customerRegistrationClient.loginWithPin(phoneNumber, pin);
    }

    /**
     * Set customer PIN.
     *
     * @param phoneNumber the customer's phone number
     * @param pin         the 4-digit PIN
     * @return boolean response
     */
    public ChariBooleanResponse setCustomerPin(String phoneNumber, String pin) {
        return createPin(phoneNumber, pin);
    }

    /**
     * Create a secure PIN for a registered customer.
     *
     * @param phoneNumber the customer's phone number
     * @param pin         the 4-digit PIN
     * @return boolean response
     */
    public ChariBooleanResponse createPin(String phoneNumber, String pin) {
        return customerRegistrationClient.createPin(phoneNumber, pin);
    }

    /**
     * Create a secure PIN for a registered customer.
     *
     * @param payload create PIN payload
     * @return boolean response
     */
    public ChariBooleanResponse createPin(ChariCreatePinPayload payload) {
        return customerRegistrationClient.createPin(payload);
    }

    /**
     * Update an existing customer PIN.
     *
     * @param phoneNumber the customer's phone number
     * @param oldPin      the current PIN
     * @param newPin      the new 4-digit PIN
     * @return boolean response
     */
    public ChariBooleanResponse updatePin(String phoneNumber, String oldPin, String newPin) {
        return customerRegistrationClient.updatePin(phoneNumber, oldPin, newPin);
    }

    /**
     * Update an existing customer PIN.
     *
     * @param payload update PIN payload
     * @return boolean response
     */
    public ChariBooleanResponse updatePin(ChariUpdatePinPayload payload) {
        return customerRegistrationClient.updatePin(payload);
    }

    // ==================== Wallet Operations ====================

    /**
     * Get wallet information by phone number.
     * Uses /api/customers/info endpoint which returns wallet data.
     *
     * @param phoneNumber the wallet owner's phone number
     * @return wallet response
     */
    public BaasWalletResponse getWallet(String phoneNumber) {
        String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
        log.debug("Getting wallet for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

        String url = UriComponentsBuilder.fromPath("/api/customers/info")
                .queryParam("phoneNumber", normalizedPhone)
                .toUriString();

        return executeGet(url, BaasWalletResponse.class, "GET_WALLET");
    }

    /**
     * Get the configured principal agent wallet.
     *
     * @return principal agent wallet response
     */
    public BaasWalletResponse getPrincipalAgentWallet() {
        return getWallet(getPrincipalAgentId());
    }

    /**
     * Get principal agent information.
     *
     * @param principalAgentId optional principal agent ID (uses configured ID if
     *                         null)
     * @return principal agent response
     */
    public ChariPrincipalAgentResponse getPrincipalAgentInfo(String principalAgentId) {
        String id = NumericIdentifierUtil
                .normalize(principalAgentId != null ? principalAgentId : getPrincipalAgentId());
        log.debug("Getting principal agent info for ID: {}", id);

        String url = UriComponentsBuilder.fromPath("/api/agents/principal/{id}")
                .buildAndExpand(id)
                .toUriString();

        return executeGet(url, ChariPrincipalAgentResponse.class, "GET_PRINCIPAL_AGENT");
    }

    // ==================== Transfer Operations ====================

    /**
     * Preview a wallet-to-wallet transfer.
     *
     * @param payload transfer payload
     * @return transfer preview response
     */
    public ChariTransferPreviewResponse previewTransfer(ChariTransferPayload payload) {
        return transferClient.previewTransfer(payload);
    }

    /**
     * Execute a wallet-to-wallet transfer.
     *
     * @param payload transfer payload
     * @return transfer response
     */
    public ChariTransferResponse executeTransfer(ChariTransferPayload payload) {
        return transferClient.executeTransfer(payload);
    }

    // ==================== Bank Transfer Operations ====================

    /**
     * Preview a bank transfer.
     *
     * @param payload     bank transfer payload
     * @param phoneNumber customer phone number for query param
     * @return bank transfer preview response
     */
    public ChariBankTransferPreviewResponse previewBankTransfer(ChariBankTransferPayload payload, String phoneNumber) {
        return bankTransferClient.previewBankTransfer(payload, phoneNumber);
    }

    /**
     * Execute a bank transfer.
     *
     * @param payload     bank transfer payload
     * @param phoneNumber customer phone number for query param
     * @return bank transfer response
     */
    public ChariBankTransferResponse executeBankTransfer(ChariBankTransferPayload payload, String phoneNumber) {
        return bankTransferClient.executeBankTransfer(payload, phoneNumber);
    }

    /**
     * Preview a bank transfer from Agent Principal.
     * Uses AgentCode instead of CustomerPhoneNumber.
     *
     * @param payload bank transfer payload (uses AP as source)
     * @return bank transfer preview response
     */
    public ChariBankTransferPreviewResponse previewBankTransferFromAP(ChariBankTransferPayload payload) {
        return bankTransferClient.previewBankTransferFromAP(payload);
    }

    /**
     * Execute a bank transfer from Agent Principal.
     * Uses AgentCode instead of CustomerPhoneNumber.
     *
     * @param payload bank transfer payload (uses AP as source)
     * @return bank transfer response
     */
    public ChariBankTransferResponse executeBankTransferFromAP(ChariBankTransferPayload payload) {
        return bankTransferClient.executeBankTransferFromAP(payload);
    }

    // ==================== Card Funding Operations ====================

    /**
     * Preview a card funding (cash-in) operation.
     *
     * @param customerPhoneNumber the customer's phone number
     * @param amount              the amount to fund
     * @return card funding preview response
     */
    public ChariCardFundingPreviewResponse previewCardFunding(String customerPhoneNumber, BigDecimal amount) {
        return cashInCardClient.previewByPhone(customerPhoneNumber, amount);
    }

    /**
     * Preview a card funding (cash-in) operation by agent code.
     *
     * @param code   the agent code
     * @param amount the amount to fund
     * @return card funding preview response
     */
    public ChariCardFundingPreviewResponse previewCardFundingByAgent(String code, BigDecimal amount) {
        return cashInCardClient.previewByAgent(code, amount);
    }

    /**
     * Execute a card funding (cash-in) operation.
     *
     * @param customerPhoneNumber the customer's phone number
     * @param payload             card payment details
     * @return card funding execution response
     */
    public ChariCardFundingExecutionResponse executeCardFunding(
            String customerPhoneNumber, ChariCardCashinPayload payload) {
        return cashInCardClient.executeByPhone(customerPhoneNumber, payload);
    }

    /**
     * Execute a card funding (cash-in) operation by agent code.
     *
     * @param code    the agent code
     * @param payload card payment details
     * @return card funding execution response
     */
    public ChariCardFundingExecutionResponse executeCardFundingByAgent(
            String code, ChariCardCashinPayload payload) {
        return cashInCardClient.executeByAgent(code, payload);
    }

    // ==================== QR Code Payment Operations ====================

    /**
     * Preview a merchant payment by recipient phone number.
     *
     * @param payload merchant payment payload
     * @return merchant payment preview response
     */
    public ChariMerchantPaymentByPhonePreviewResponse previewMerchantPaymentByPhone(
            ChariMerchantPaymentByPhonePayload payload) {
        return merchantPaymentClient.previewByPhone(payload);
    }

    /**
     * Execute a merchant payment by recipient phone number.
     *
     * @param payload merchant payment payload
     * @return merchant payment response
     */
    public ChariMerchantPaymentByPhoneResponse executeMerchantPaymentByPhone(
            ChariMerchantPaymentByPhonePayload payload) {
        return merchantPaymentClient.executeByPhone(payload);
    }

    /**
     * Preview a QR code payment.
     *
     * @param payload QR payment payload
     * @return QR payment preview response
     */
    public ChariQrCodePaymentPreviewResponse previewQrCodePayment(ChariQrCodePaymentPayload payload) {
        return merchantPaymentClient.previewQrCodePayment(payload);
    }

    /**
     * Execute a QR code payment.
     *
     * @param payload QR payment payload
     * @return QR payment response
     */
    public ChariQrCodePaymentResponse executeQrCodePayment(ChariQrCodePaymentPayload payload) {
        return merchantPaymentClient.executeQrCodePayment(payload);
    }

    /**
     * Preview a merchant card payment.
     *
     * @param phoneNumber merchant phone number
     * @param amount      payment amount
     * @return merchant card payment preview response
     */
    public ChariMerchantCardPaymentPreviewResponse previewMerchantCardPayment(
            String phoneNumber, BigDecimal amount) {
        return merchantPaymentClient.previewCardPayment(phoneNumber, amount);
    }

    /**
     * Execute a merchant card payment.
     *
     * @param phoneNumber merchant phone number
     * @param payload     card payment payload
     * @return merchant card payment response
     */
    public ChariMerchantCardPaymentResponse executeMerchantCardPayment(
            String phoneNumber, ChariMerchantCardPaymentPayload payload) {
        return merchantPaymentClient.executeCardPayment(phoneNumber, payload);
    }

    /**
     * Execute a merchant payment with a tokenized card.
     *
     * @param cardId      tokenized card ID
     * @param phoneNumber merchant phone number
     * @param payload     tokenized card payment payload
     * @return merchant card payment response
     */
    public ChariMerchantCardPaymentResponse executeMerchantTokenizedCardPayment(
            Integer cardId, String phoneNumber, ChariMerchantTokenizedCardPaymentPayload payload) {
        return merchantPaymentClient.executeTokenizedCardPayment(cardId, phoneNumber, payload);
    }

    /**
     * Preview a merchant card payment via the direct (non-push) card endpoint.
     *
     * @param phoneNumber merchant phone number
     * @param amount      payment amount
     * @return merchant card payment preview response
     */
    public ChariMerchantCardPaymentPreviewResponse previewMerchantCardPaymentDirect(
            String phoneNumber, BigDecimal amount) {
        return merchantPaymentClient.previewCardPaymentDirect(phoneNumber, amount);
    }

    /**
     * Capture an authorized merchant card payment (autoCapture=false flow).
     *
     * @param payload capture payload targeting the transaction via orderId/transactionTrackId
     * @return merchant card lifecycle response
     */
    public ChariMerchantCardLifecycleResponse captureMerchantCardPayment(
            ChariMerchantCardCapturePayload payload) {
        return merchantPaymentClient.captureCardPayment(payload);
    }

    /**
     * Reverse an uncaptured merchant card authorization, releasing the funds.
     *
     * @param payload reverse payload targeting the transaction via orderId/transactionTrackId
     * @return merchant card lifecycle response
     */
    public ChariMerchantCardLifecycleResponse reverseMerchantCardPayment(
            ChariMerchantCardCapturePayload payload) {
        return merchantPaymentClient.reverseCardPayment(payload);
    }

    /**
     * Refund a captured merchant card payment. A refundAmount below the captured
     * amount performs a partial refund.
     *
     * @param payload refund payload with phoneNumber, operationId, refundAmount, orderId, transactionTrackId
     * @return merchant card lifecycle response
     */
    public ChariMerchantCardLifecycleResponse refundMerchantCardPayment(
            ChariMerchantCardRefundPayload payload) {
        return merchantPaymentClient.refundCardPayment(payload);
    }

    /**
     * Preview a chargeback operation.
     *
     * @param payload chargeback payload
     * @return chargeback preview response
     */
    public ChariChargebackPreviewResponse previewChargeback(ChariChargebackPayload payload) {
        return chargebackClient.previewChargeback(payload);
    }

    /**
     * Generate a dynamic QR code for a specific amount.
     *
     * @param phoneNumber customer's phone number
     * @param payload     QR code generation payload
     * @return QR code response
     */
    public ChariGenerateQrCodeResponse generateQrCode(String phoneNumber, ChariGenerateQrCodePayload payload) {
        return merchantPaymentClient.generateQrCode(phoneNumber, payload);
    }

    /**
     * Generate a dynamic QR code for a specific amount.
     *
     * @param phoneNumber  merchant phone number
     * @param maskedNumber whether to mask the merchant phone number in QR content
     * @param payload      QR code generation payload
     * @return QR code response
     */
    public ChariGenerateQrCodeResponse generateQrCode(
            String phoneNumber, Boolean maskedNumber, ChariGenerateQrCodePayload payload) {
        return merchantPaymentClient.generateQrCode(phoneNumber, maskedNumber, payload);
    }

    /**
     * Generate a static QR code for a merchant.
     *
     * @param phoneNumber merchant phone number
     * @return QR code response
     */
    public ChariGenerateQrCodeResponse generateStaticQrCode(String phoneNumber) {
        return merchantPaymentClient.generateStaticQrCode(phoneNumber);
    }

    /**
     * Generate a static QR code for a merchant.
     *
     * @param phoneNumber  merchant phone number
     * @param maskedNumber whether to mask the merchant phone number in QR content
     * @return QR code response
     */
    public ChariGenerateQrCodeResponse generateStaticQrCode(String phoneNumber, Boolean maskedNumber) {
        return merchantPaymentClient.generateStaticQrCode(phoneNumber, maskedNumber);
    }

    /**
     * Generate a static QR code.
     *
     * @param phoneNumber customer's phone number
     * @param payload     ignored for the static QR endpoint
     * @return QR code response
     * @deprecated use {@link #generateStaticQrCode(String)} or
     *             {@link #generateStaticQrCode(String, Boolean)}
     */
    @Deprecated
    public ChariGenerateQrCodeResponse generateStaticQrCode(String phoneNumber, ChariGenerateQrCodePayload payload) {
        return merchantPaymentClient.generateStaticQrCode(phoneNumber);
    }

    // ==================== Saved Card Operations ====================

    /**
     * Save a card to the customer's account for future use.
     *
     * @param phoneNumber customer's phone number
     * @param payload     card details to save
     * @return boolean response
     */
    public ChariBooleanResponse saveCard(String phoneNumber, ChariSaveCardPayload payload) {
        return tokenizedCardClient.saveCard(phoneNumber, payload);
    }

    /**
     * List all saved cards for a customer.
     *
     * @param phoneNumber customer's phone number
     * @return list of saved cards
     */
    public ChariListSavedCardsResponse listSavedCards(String phoneNumber) {
        return tokenizedCardClient.listSavedCards(phoneNumber);
    }

    /**
     * List saved cards for a customer.
     *
     * @param phoneNumber customer's phone number
     * @param pageSize    results per page
     * @param pageNumber  page number
     * @return list of saved cards
     */
    public ChariListSavedCardsResponse listSavedCards(String phoneNumber, Integer pageSize, Integer pageNumber) {
        return tokenizedCardClient.listSavedCards(phoneNumber, pageSize, pageNumber);
    }

    /**
     * Get a saved card by ID.
     *
     * @param cardId      the customerBankCardId of the card
     * @param phoneNumber customer's phone number
     * @return saved card response
     */
    public ChariSavedCardResponse getSavedCard(Integer cardId, String phoneNumber) {
        return tokenizedCardClient.getSavedCard(cardId, phoneNumber);
    }

    /**
     * Delete a saved card from customer's account.
     *
     * @param cardId      the customerBankCardId of the card to delete
     * @param phoneNumber customer's phone number
     * @return boolean response
     */
    public ChariBooleanResponse deleteSavedCard(Integer cardId, String phoneNumber) {
        return tokenizedCardClient.deleteSavedCard(cardId, phoneNumber);
    }

    /**
     * Perform a cashin operation using a saved card.
     *
     * @param cardId      the customerBankCardId of the saved card
     * @param phoneNumber customer's phone number
     * @param payload     cashin details (amount, CVV, etc.)
     * @return cashin response with redirect URL if 3DS is required
     */
    public ChariSavedCardCashinResponse cashinWithSavedCard(
            Integer cardId, String phoneNumber, ChariSavedCardCashinPayload payload) {
        return cashInCardClient.executeWithSavedCard(cardId, phoneNumber, payload);
    }

    // ==================== Cash-in / Cash-out by Reference Operations
    // ====================

    /**
     * Request a cash-in by reference.
     * Creates a pending cash-in operation that can be completed at a Chari agent
     * point.
     *
     * @param payload cash-in request payload (phone number + amount)
     * @return cash-in by reference response with operation reference
     */
    public ChariCashinByReferenceResponse requestCashinByReference(ChariCashinByReferencePayload payload) {
        return requestOperationClient.requestCashinByReference(payload);
    }

    /**
     * Request a cash-out by reference.
     * Creates a pending cash-out operation that can be completed at a Chari agent
     * point.
     *
     * @param payload cash-out request payload (phone number + amount)
     * @return cash-out by reference response with operation reference
     */
    public ChariCashoutByReferenceResponse requestCashoutByReference(ChariCashoutByReferencePayload payload) {
        return requestOperationClient.requestCashoutByReference(payload);
    }

    public ChariCashinByReferenceResponse getCashinByReference(String reference) {
        return requestOperationClient.getCashinByReference(reference);
    }

    public ChariCashinByReferenceResponse executeCashinByReference(
            ChariExecuteRequestOperationByReferencePayload payload) {
        return requestOperationClient.executeCashinByReference(payload);
    }

    public ChariCashoutByReferenceResponse getCashoutByReference(String reference) {
        return requestOperationClient.getCashoutByReference(reference);
    }

    public ChariCashoutByReferenceResponse executeCashoutByReference(
            ChariExecuteRequestOperationByReferencePayload payload) {
        return requestOperationClient.executeCashoutByReference(payload);
    }

    /**
     * Request the dedicated Fatourati cash-in (FATREF- reference flow).
     *
     * @param payload code, amount, and optional feesPercent/description
     * @return cash-in-by-reference response carrying the generated reference
     */
    public ChariCashinByReferenceResponse requestFatouratiCashin(
            ChariFatouratiCashinRequestPayload payload) {
        return requestOperationClient.requestFatouratiCashin(payload);
    }

    /**
     * List the cash-in / cash-out request operations of a customer.
     *
     * @param phoneNumber customer phone number
     * @param pageSize    page size, optional
     * @param pageNumber  page number, optional
     * @return paginated request operations response
     */
    public ChariRequestOperationsResponse getRequestOperations(String phoneNumber, Integer pageSize,
            Integer pageNumber) {
        return requestOperationClient.getRequestOperations(phoneNumber, pageSize, pageNumber);
    }

    // ==================== Network Operations (sandbox simulation) ====================

    /**
     * Simulate a network CashIn by reference (sandbox network agent step).
     *
     * @param payload     reference (required) and optional entity
     * @param withContext when non-null, returns the result with its context if it exists
     * @return network operation response
     */
    public ChariNetworkOperationResponse simulateNetworkCashin(
            ChariNetworkOperationPayload payload, Boolean withContext) {
        return networkOperationClient.simulateCashin(payload, withContext);
    }

    /**
     * Simulate a network CashOut by reference (sandbox network agent step).
     *
     * @param payload     reference (required) and optional entity
     * @param withContext when non-null, returns the result with its context if it exists
     * @return network operation response
     */
    public ChariNetworkOperationResponse simulateNetworkCashout(
            ChariNetworkOperationPayload payload, Boolean withContext) {
        return networkOperationClient.simulateCashout(payload, withContext);
    }

    // ==================== Retail Agent Operations ====================

    public ChariRetailAgentsResponse getRetailAgents(String code, Integer pageSize, Integer pageNumber) {
        return retailAgentClient.getRetailAgents(code, pageSize, pageNumber);
    }

    public ChariRetailAgentResponse getRetailAgentByCode(String code) {
        return retailAgentClient.getRetailAgentByCode(code);
    }

    public ChariRetailAgentCreatedResponse addRetailAgent(ChariRetailAgentPayload payload) {
        return retailAgentClient.addRetailAgent(payload);
    }

    /**
     * Get paginated operations for a customer.
     *
     * @param query customer operation filters
     * @return paginated operations response
     */
    public ChariOperationsResponse getOperationsByCustomer(ChariOperationsByCustomerQuery query) {
        return operationsClient.getOperationsByCustomer(query);
    }

    /**
     * Get paginated operations for the partner.
     *
     * @param query operation filters
     * @return paginated operations response
     */
    public ChariOperationsResponse getAllOperationsByPartner(ChariOperationsByCustomerQuery query) {
        return operationsClient.getAllOperationsByPartner(query);
    }

    /**
     * Get a specific operation by ID for a customer.
     *
     * @param id          operation ID
     * @param phoneNumber customer phone number
     * @return operation response
     */
    public ChariOperationResponse getOperationById(Long id, String phoneNumber) {
        return operationsClient.getOperationById(id, phoneNumber);
    }

    // ==================== Telco Top-up Operations ====================

    /**
     * Returns every Moroccan mobile operator supported by Telco top-up.
     */
    public List<ChariTelcoOperator> getSupportedTelcoOperators() {
        return List.of(ChariTelcoOperator.values());
    }

    /**
     * Retrieves recharge products for a phone number, amount, and operator.
     */
    public ChariTelcoCatalogResponse getTelcoCatalog(ChariTelcoCatalogPayload payload) {
        return telcoTopUpClient.getCatalog(payload);
    }

    /**
     * Convenience overload for retrieving recharge products without building a
     * payload.
     */
    public ChariTelcoCatalogResponse getTelcoCatalog(
            String recipientPhoneNumber, Integer amount, ChariTelcoOperator operator) {
        return getTelcoCatalog(ChariTelcoCatalogPayload.builder()
                .recipientPhoneNumber(recipientPhoneNumber)
                .amount(amount)
                .operator(operator)
                .build());
    }

    /**
     * Executes a Telco top-up using a product returned by {@link #getTelcoCatalog}.
     */
    public ChariTelcoRechargeResponse rechargeTelco(ChariTelcoRechargePayload payload) {
        return telcoTopUpClient.recharge(payload);
    }

    /**
     * Convenience overload for executing a Telco top-up without building a payload.
     */
    public ChariTelcoRechargeResponse rechargeTelco(
            String recipientPhoneNumber,
            Integer amount,
            ChariTelcoOperator operator,
            ChariTelcoRechargeType rechargeType,
            Integer productCode,
            String principalAgentCode) {
        return rechargeTelco(ChariTelcoRechargePayload.builder()
                .recipientPhoneNumber(recipientPhoneNumber)
                .amount(amount)
                .operator(operator)
                .rechargeType(rechargeType)
                .productCode(productCode)
                .code(principalAgentCode)
                .build());
    }

    // ==================== Voucher Operations ====================

    /** Retrieves a paginated voucher article catalog for a brand. */
    public ChariVoucherArticlesResponse getVoucherArticles(ChariVoucherCatalogQuery query) {
        return voucherClient.getArticles(query);
    }

    /** Retrieves the first ten voucher articles for a brand. */
    public ChariVoucherArticlesResponse getVoucherArticles(
            String phoneNumber, Integer brandId) {
        return getVoucherArticles(voucherCatalogQuery(phoneNumber, brandId, 1, 10));
    }

    /** Retrieves the current paginated voucher brand catalog. */
    public ChariVoucherBrandsResponse getVoucherBrands(ChariVoucherCatalogQuery query) {
        return voucherClient.getBrands(query);
    }

    /** Retrieves the first ten voucher brands using the required brand filter. */
    public ChariVoucherBrandsResponse getVoucherBrands(
            String phoneNumber, Integer brandId) {
        return getVoucherBrands(voucherCatalogQuery(phoneNumber, brandId, 1, 10));
    }

    /** Retrieves one voucher brand by ID. */
    public ChariVoucherBrandResponse getVoucherBrand(Integer id, String phoneNumber) {
        return voucherClient.getBrand(id, phoneNumber);
    }

    /** Calls the provider's vouchers-by-brand endpoint. */
    public ChariVoucherBrandResponse getVouchersByBrand(Integer id, String phoneNumber) {
        return voucherClient.getVouchersByBrand(id, phoneNumber);
    }

    /** Retrieves the paginated Click Apporter / Blackhawk product catalog. */
    public ChariVoucherArticlesResponse getVoucherProducts(Integer page, Integer take) {
        return voucherClient.getProducts(page, take);
    }

    /** Retrieves detailed Blackhawk product information by config ID. */
    public ChariVoucherProductResponse getVoucherProductDetail(String configId) {
        return voucherClient.getProductDetail(configId);
    }

    /** Lists local vouchers for a phone number (optional brandId/keyword filters). */
    public ChariVoucherArticlesResponse getLocalVouchers(ChariVoucherCatalogQuery query) {
        return voucherClient.getLocalVouchers(query);
    }

    /** Lists the first ten local vouchers for a phone number. */
    public ChariVoucherArticlesResponse getLocalVouchers(String phoneNumber) {
        return getLocalVouchers(ChariVoucherCatalogQuery.builder()
                .phoneNumber(phoneNumber)
                .page(1)
                .take(10)
                .build());
    }

    /** Previews voucher pricing and fees without purchasing it. */
    public ChariVoucherPreviewResponse previewVoucherPurchase(ChariVoucherPurchasePayload payload) {
        return voucherClient.previewPurchase(payload);
    }

    /** Confirms the purchase and returns the redeemable voucher code. */
    public ChariVoucherPurchaseResponse confirmVoucherPurchase(ChariVoucherPurchasePayload payload) {
        return voucherClient.confirmPurchase(payload);
    }

    /** Previews a service voucher purchase (service endpoint variant). */
    public ChariVoucherPreviewResponse previewServiceVoucherPurchase(ChariVoucherPurchasePayload payload) {
        return voucherClient.previewServicePurchase(payload);
    }

    /** Purchases a service voucher (service endpoint variant). */
    public ChariVoucherPurchaseResponse purchaseServiceVoucher(ChariVoucherPurchasePayload payload) {
        return voucherClient.purchaseServiceVoucher(payload);
    }

    private ChariVoucherCatalogQuery voucherCatalogQuery(
            String phoneNumber, Integer brandId, Integer page, Integer take) {
        return ChariVoucherCatalogQuery.builder()
                .phoneNumber(phoneNumber)
                .brandId(brandId)
                .page(page)
                .take(take)
                .build();
    }

    // ==================== Bill Payment Operations ====================

    /** Step 1: lists active creditors available through Fatourati. */
    public ChariBillCreditorsResponse getBillCreditors() {
        return billPaymentClient.getCreditors();
    }

    /** Step 2: lists active receivables for one creditor. */
    public ChariBillReceivablesResponse getBillReceivables(String creditorId) {
        return billPaymentClient.getReceivables(creditorId);
    }

    /** Step 3: retrieves the dynamic customer-identification form. */
    public ChariBillFormResponse getBillIdentificationForm(
            String creditorId, String receivableId) {
        return billPaymentClient.getIdentificationForm(creditorId, receivableId);
    }

    /** Step 4: opens a Fatourati transaction and retrieves unpaid items. */
    public ChariBillUnpaidItemsResponse getBillUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillUnpaidItemsPayload payload) {
        return billPaymentClient.getUnpaidItems(creditorId, receivableId, payload);
    }

    /**
     * Step 4 with the customer's phone number, required when saving the bill
     * to favorites ({@code alias}/{@code addToFavorites}).
     */
    public ChariBillUnpaidItemsResponse getBillUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillUnpaidItemsPayload payload,
            String phoneNumber) {
        return billPaymentClient.getUnpaidItems(creditorId, receivableId, payload, phoneNumber);
    }

    /**
     * Step 4 with a local pre-check of the submitted values against the
     * dynamic identification form, failing fast before any network call.
     */
    public ChariBillUnpaidItemsResponse getBillUnpaidItems(
            String creditorId,
            String receivableId,
            ChariBillFormResponse form,
            ChariBillUnpaidItemsPayload payload,
            String phoneNumber) {
        return billPaymentClient.getUnpaidItems(creditorId, receivableId, form, payload, phoneNumber);
    }

    /** Step 4 by scanned bill QR code, without filling the identification form. */
    public ChariBillUnpaidItemsResponse getBillUnpaidItemsByQrCode(
            String creditorId,
            String receivableId,
            String qrCodeContent,
            String phoneNumber) {
        return billPaymentClient.getUnpaidItems(creditorId, receivableId,
                ChariBillUnpaidItemsPayload.forQrCode(qrCodeContent), phoneNumber);
    }

    /** Step 5: pays the selected articles for a single creditor. */
    public ChariBillPaymentResponse confirmBillPayment(
            String phoneNumber, ChariBillPaymentPayload payload) {
        return billPaymentClient.confirmPayment(phoneNumber, payload);
    }

    /** Step 6: downloads the receipt of a settled bill payment operation. */
    public ChariBillReceiptResponse getBillReceipt(long operationId, String phoneNumber) {
        return billPaymentClient.getBillReceipt(operationId, phoneNumber);
    }

    // ==================== Card Management Operations ====================

    public ChariCardProgramsResponse getCardPrograms() {
        return cardManagementClient.getPrograms(10, 1);
    }

    public ChariCardProgramsResponse getCardPrograms(Integer pageSize, Integer pageNumber) {
        return cardManagementClient.getPrograms(pageSize, pageNumber);
    }

    public ChariCardApplicationCreatedResponse addCardApplication(
            String phoneNumber, Long cardProgramId) {
        return cardManagementClient.addApplication(phoneNumber, cardProgramId);
    }

    public ChariCardApplicationsResponse getCardApplications(ChariCardApplicationsQuery query) {
        return cardManagementClient.getApplications(query);
    }

    public ChariCardApplicationsResponse getCardApplicationsByCustomer(
            Integer pageSize, Integer pageNumber) {
        return cardManagementClient.getApplicationsByCustomer(pageSize, pageNumber);
    }

    public ChariCardApplicationResponse validateCardApplication(
            Long applicationId, String phoneNumber) {
        return cardManagementClient.updateApplication(applicationId, phoneNumber, true);
    }

    public ChariCardApplicationResponse rejectCardApplication(
            Long applicationId, String phoneNumber) {
        return cardManagementClient.updateApplication(applicationId, phoneNumber, false);
    }

    public ChariManagedCardsResponse getManagedCards(ChariManagedCardsQuery query) {
        return cardManagementClient.getCards(query);
    }

    public ChariManagedCardResponse getManagedCard(Long cardId, String phoneNumber) {
        return cardManagementClient.getCard(cardId, phoneNumber);
    }

    public ChariBooleanResponse manageCard(
            Long cardId, String phoneNumber, ChariCardAction action) {
        return cardManagementClient.runAction(cardId, phoneNumber, action);
    }

    public ChariBooleanResponse activateCard(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.ACTIVATE);
    }

    public ChariBooleanResponse blockCard(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.BLOCK);
    }

    public ChariBooleanResponse suspendCard(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.SUSPEND);
    }

    public ChariBooleanResponse reactivateCard(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.REACTIVATE);
    }

    public ChariBooleanResponse cancelCard(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.CANCEL);
    }

    public ChariBooleanResponse unblockCardPin(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.UNBLOCK_PIN);
    }

    public ChariBooleanResponse resetCardPin(Long cardId, String phoneNumber) {
        return manageCard(cardId, phoneNumber, ChariCardAction.RESET_PIN);
    }

    public ChariBooleanResponse updateCardUsageControl(
            Long cardId, String phoneNumber, ChariCardUsageControlPayload payload) {
        return cardManagementClient.updateUsageControl(cardId, phoneNumber, payload);
    }

    public ChariCardTransactionsResponse getCardTransactions(
            Long cardId, ChariCardTransactionsQuery query) {
        return cardManagementClient.getTransactions(cardId, query);
    }

    // ==================== Refund Operations ====================

    /**
     * Preview a refund operation.
     * Returns fee information and validates the refund before execution.
     *
     * @param payload refund payload with phone, amount, operationId, orderId,
     *                transactionTrackId
     * @return refund preview response
     */
    public ChariRefundResponse previewRefund(ChariRefundPayload payload) {
        return refundClient.previewRefund(payload);
    }

    /**
     * Execute a refund operation.
     * Refunds the specified amount for the given operation.
     *
     * @param payload refund payload with phone, amount, operationId, orderId,
     *                transactionTrackId
     * @return refund execution response
     */
    public ChariRefundResponse executeRefund(ChariRefundPayload payload) {
        return refundClient.executeRefund(payload);
    }

    // ==================== Configuration Getters ====================

    /**
     * Get the configured principal agent ID.
     */
    public String getConfiguredPrincipalAgentId() {
        return getPrincipalAgentId();
    }

    /**
     * Get the configured principal agent ID.
     */
    public String getPrincipalAgentId() {
        return NumericIdentifierUtil.normalize(properties.getPrincipalAgentId());
    }

    /**
     * Get the configured principal agent RIB.
     */
    public String getPrincipalAgentRib() {
        return NumericIdentifierUtil.normalize(properties.getPrincipalAgentRib());
    }

    // ==================== HTTP Helper Methods ====================

    private <T> T executeGet(String path, Class<T> responseType, String stage) {
        return httpClient.get(path, responseType, stage);
    }

}
