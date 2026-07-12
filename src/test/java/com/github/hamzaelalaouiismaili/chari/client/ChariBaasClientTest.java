package com.github.hamzaelalaouiismaili.chari.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariErrorCode;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCustomerStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariAccountLevel;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariClosureReason;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariDocumentType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariRequestOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariSens;
import com.github.hamzaelalaouiismaili.chari.domain.enums.WalletType;
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariBaasException;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBankTransferPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBeneficiaryPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardCashinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashinByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCashoutByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariChargebackPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCreatePinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCustomerConfirmPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariExecuteRequestOperationByReferencePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariGenerateQrCodePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariLoginWithPinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantCardPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantKycUploadPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantPaymentByPhonePayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariMerchantTokenizedCardPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariOperationsByCustomerQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariQrCodePaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRegisterCustomerPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRefundPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRetailAgentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariSavedCardCashinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTransferPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariUpdatePinPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBalanceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBankTransferPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBankTransferResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBeneficiariesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBeneficiaryResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardFundingExecutionResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardFundingPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashinByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCashoutByReferenceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariChargebackPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCustomerInfoResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCustomerStatusResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariDefaultWalletResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariGenerateQrCodeResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariLoginWithPinResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantCardPaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantPaymentByPhonePreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariMerchantPaymentByPhoneResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariOperationResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariOperationsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariPrincipalAgentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariQrCodePaymentPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariQrCodePaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRefundResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariListSavedCardsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentCreatedResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariRetailAgentsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariSavedCardCashinResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariSavedCardResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariShareIdAuthResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTransferPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTransferResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ChariBaasClientTest {

  @Test
  void getCustomerStatusNormalizesPhoneAndSendsAuthenticationHeaders() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/status?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("{\"data\":{\"status\":3,\"message\":\"Active\"}}",
            MediaType.APPLICATION_JSON));

    ChariCustomerStatusResponse response = client.getCustomerStatus("0612345678");

    assertThat(response.getData().getStatus()).isEqualTo(3);
    assertThat(response.getData().getMessage()).isEqualTo("Active");
    assertThat(response.getData().getCustomerStatus()).isEqualTo(ChariCustomerStatus.ACTIVE);
    assertThat(response.getData().canTransact()).isTrue();
    assertThat(response.getData().needsRegistration()).isFalse();
    assertThat(response.getData().needsOtpConfirmation()).isFalse();
    assertThat(response.getData().isLocked()).isFalse();
    server.verify();
  }

  @Test
  void checkDefaultWalletNormalizesPhoneAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/default?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("{\"data\":{\"isDefaultWallet\":true}}",
            MediaType.APPLICATION_JSON));

    ChariDefaultWalletResponse response = client.checkDefaultWallet("0612345678");

    assertThat(response.getData().getIsDefaultWallet()).isTrue();
    server.verify();
  }

  @Test
  void isDefaultWalletReturnsBooleanValue() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/default?phoneNumber=+212612345678"))
        .andRespond(withSuccess("{\"data\":{\"isDefaultWallet\":false}}",
            MediaType.APPLICATION_JSON));

    assertThat(client.isDefaultWallet("0612345678")).isFalse();
    server.verify();
  }

  @Test
  void getCustomerBalanceNormalizesPhoneAndMapsBalance() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/balance?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("{\"data\":{\"balance\":174.0}}", MediaType.APPLICATION_JSON));

    ChariBalanceResponse response = client.getCustomerBalance("0612345678");

    assertThat(response.getData().getBalance()).isEqualByComparingTo("174.0");
    server.verify();
  }

  @Test
  void getBeneficiariesSendsOfficialPhoneQueryAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customer/beneficiaries?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "collection": [
                  {
                    "id": 129,
                    "customerId": 56,
                    "name": "abdennour",
                    "phoneNumber": null,
                    "createdAt": "2025-06-12T22:03:18.226Z",
                    "isVisible": true,
                    "rib": "1234568754045184800463450",
                    "email": null
                  }
                ],
                "count": 14
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariBeneficiariesResponse response = client.getBeneficiaries("0612345678");

    assertThat(response.getData().getCount()).isEqualTo(14);
    assertThat(response.getData().getCollection()).hasSize(1);
    assertThat(response.getData().getCollection().get(0).getId()).isEqualTo(129L);
    assertThat(response.getData().getCollection().get(0).getCustomerId()).isEqualTo(56L);
    assertThat(response.getData().getCollection().get(0).getName()).isEqualTo("abdennour");
    assertThat(response.getData().getCollection().get(0).getPhoneNumber()).isNull();
    assertThat(response.getData().getCollection().get(0).getCreatedAt())
        .isEqualTo("2025-06-12T22:03:18.226Z");
    assertThat(response.getData().getCollection().get(0).getIsVisible()).isTrue();
    assertThat(response.getData().getCollection().get(0).getRib()).isEqualTo("1234568754045184800463450");
    assertThat(response.getData().getCollection().get(0).getEmail()).isNull();
    server.verify();
  }

  @Test
  void addBeneficiarySendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariBeneficiaryPayload payload = ChariBeneficiaryPayload.builder()
        .name("Test")
        .phoneNumber("0611111111")
        .email("chari@chari.com")
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customer/beneficiaries?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"name\":\"Test\"")))
        .andExpect(content().string(containsString("\"PhoneNumber\":\"+212611111111\"")))
        .andExpect(content().string(containsString("\"Email\":\"chari@chari.com\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "id": 153,
                "customerId": 56,
                "name": "Test",
                "phoneNumber": "+212611111111",
                "createdAt": "2025-07-03T14:04:39.823Z",
                "isVisible": true,
                "rib": null,
                "email": "chari@chari.com"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariBeneficiaryResponse response = client.addBeneficiary("0612345678", payload);

    assertThat(response.getData().getId()).isEqualTo(153L);
    assertThat(response.getData().getCustomerId()).isEqualTo(56L);
    assertThat(response.getData().getName()).isEqualTo("Test");
    assertThat(response.getData().getPhoneNumber()).isEqualTo("+212611111111");
    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-07-03T14:04:39.823Z");
    assertThat(response.getData().getIsVisible()).isTrue();
    assertThat(response.getData().getRib()).isNull();
    assertThat(response.getData().getEmail()).isEqualTo("chari@chari.com");
    server.verify();
  }

  @Test
  void deleteBeneficiarySendsOfficialPathAndPhoneQuery() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/customer/beneficiaries/123?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.DELETE))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    ChariBooleanResponse response = client.deleteBeneficiary(123L, "0612345678");

    assertThat(response.getData()).isTrue();
    server.verify();
  }

  @Test
  void getCustomerInfoMapsOfficialProfileShape() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/customers/info?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "id": 72,
                "fullName": "Mohammed Chairi",
                "firstName": "Mohammed",
                "lastName": "Chairi",
                "phoneNumber": "+212612345678",
                "createdAt": "2025-04-02T11:17:37.05725",
                "enrolledAt": "2025-04-02T12:21:20.263176",
                "balance": 78,
                "accountType": 2,
                "rib": "82764000001000000000xxxx",
                "accountLevel": 1,
                "customerStatus": 2,
                "partnerId": 1,
                "createdBy": null,
                "levelInReview": null,
                "partner": {
                  "id": 1,
                  "name": "Partner",
                  "email": "partner@chari.com",
                  "active": true,
                  "createdAt": "2024-06-30T19:28:28.467",
                  "description": "abcd"
                }
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCustomerInfoResponse response = client.getCustomerInfo("0612345678");

    assertThat(response.getData().getId()).isEqualTo(72);
    assertThat(response.getData().getFullName()).isEqualTo("Mohammed Chairi");
    assertThat(response.getData().getBalance()).isEqualByComparingTo("78");
    assertThat(response.getData().getAccountType()).isEqualTo(2);
    assertThat(response.getData().getRib()).isEqualTo("82764000001000000000xxxx");
    assertThat(response.getData().getAccountLevel()).isEqualTo(1);
    assertThat(response.getData().getCustomerStatus()).isEqualTo(2);
    assertThat(response.getData().getTypedCustomerStatus()).isEqualTo(ChariCustomerStatus.CONFIRMED);
    assertThat(response.getData().getCurrentAccountLevel()).isEqualTo(ChariAccountLevel.BASIC);
    assertThat(response.getData().getPartnerId()).isEqualTo(1L);
    assertThat(response.getData().getPartner().getEmail()).isEqualTo("partner@chari.com");
    assertThat(response.getData().getPartner().getActive()).isTrue();
    server.verify();
  }

  @Test
  void unregisterCustomerNormalizesPhoneAndSendsClosureReason() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/unregister"))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"reason\":3")))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.unregisterCustomer("0612345678", ChariClosureReason.CLIENT_CONTRACT_CLOSURE)
        .getData())
        .isTrue();
    server.verify();
  }

  @Test
  void authenticateShareIdUsesUppercasePhoneNumberQueryAndMapsToken() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/kyc/shareid/auth?PhoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "baseUrl": "https://v2.shareid.net",
                "applicant_id": "a-o-251103-142417-pro-0003-c46cfaf1a",
                "token": "eyJhbGciO..."
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariShareIdAuthResponse response = client.authenticateShareId("0612345678");

    assertThat(response.getData().getBaseUrl()).isEqualTo("https://v2.shareid.net");
    assertThat(response.getData().getApplicantId()).isEqualTo("a-o-251103-142417-pro-0003-c46cfaf1a");
    assertThat(response.getData().getToken()).isEqualTo("eyJhbGciO...");
    server.verify();
  }

  @Test
  void confirmKycUsesUppercaseQueryParametersAndNoBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo(
            "https://sandbox.charimoney.com/api/customers/upgrade/request?PhoneNumber=+212612345678&AccountLevel=2"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().string(""))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.confirmKyc("0612345678", ChariAccountLevel.KYC_LEVEL_2).getData()).isTrue();
    server.verify();
  }

  @Test
  void confirmKycErrorsExposeUpgradeUnderReviewCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo(
            "https://sandbox.charimoney.com/api/customers/upgrade/request?PhoneNumber=+212612345678&AccountLevel=2"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 32000,
                  "errorDescription": "An upgrade request is already under review for this account."
                }
                """));

    assertThatThrownBy(() -> client.confirmKyc("0612345678", 2))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW);
          assertThat(exception.getErrorCode()).isEqualTo(32000);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void uploadMerchantKycDocumentsUsesIndexedMultipartFields() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantKycUploadPayload.KycDocument identityCard = ChariMerchantKycUploadPayload.KycDocument
        .builder()
        .docType(ChariDocumentType.IdentityCard)
        .docFront(resource("cin-front.jpg", "front"))
        .docBack(resource("cin-back.jpg", "back"))
        .build();
    ChariMerchantKycUploadPayload.KycDocument commercialRegister = ChariMerchantKycUploadPayload.KycDocument
        .builder()
        .docType(ChariDocumentType.CommercialRegister)
        .docFront(resource("rc.pdf", "%PDF"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/merchant/kyc/request?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
        .andExpect(content().string(containsString("name=\"KycDocuments[0].DocType\"")))
        .andExpect(content().string(containsString("name=\"KycDocuments[0].DocFront\"")))
        .andExpect(content().string(containsString("filename=\"cin-front.jpg\"")))
        .andExpect(content().string(containsString("name=\"KycDocuments[0].DocBack\"")))
        .andExpect(content().string(containsString("filename=\"cin-back.jpg\"")))
        .andExpect(content().string(containsString("name=\"KycDocuments[1].DocType\"")))
        .andExpect(content().string(containsString("name=\"KycDocuments[1].DocFront\"")))
        .andExpect(content().string(containsString("filename=\"rc.pdf\"")))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    ChariBooleanResponse response = client.uploadMerchantKycDocuments("0612345678",
        List.of(identityCard, commercialRegister));

    assertThat(response.getData()).isTrue();
    server.verify();
  }

  @Test
  void uploadMerchantKycDocumentsErrorsExposeUpgradeUnderReviewCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/merchant/kyc/request?phoneNumber=+212612345678"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 32000,
                  "errorDescription": "An upgrade request is already under review for this account."
                }
                """));

    assertThatThrownBy(() -> client.uploadMerchantKycDocuments(ChariMerchantKycUploadPayload.builder()
        .phoneNumber("0612345678")
        .kycDocuments(List.of(ChariMerchantKycUploadPayload.KycDocument.builder()
            .docType(ChariDocumentType.CommercialRegister)
            .docFront(resource("rc.pdf", "%PDF"))
            .build()))
        .build()))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.UPGRADE_REQUEST_UNDER_REVIEW);
          assertThat(exception.getErrorCode()).isEqualTo(32000);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void uploadMerchantKycDocumentsRequiresBackImageForTwoSidedDocumentTypes() {
    RestTemplate restTemplate = new RestTemplate();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantKycUploadPayload.KycDocument identityCard = ChariMerchantKycUploadPayload.KycDocument
        .builder()
        .docType(ChariDocumentType.IdentityCard)
        .docFront(resource("cin-front.jpg", "front"))
        .build();

    assertThatThrownBy(() -> client.uploadMerchantKycDocuments("0612345678", List.of(identityCard)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("KycDocuments[0].DocBack is required for IdentityCard");
  }

  @Test
  void registerCustomerNormalizesPhoneAndSendsOfficialBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariRegisterCustomerPayload payload = ChariRegisterCustomerPayload.builder()
        .phoneNumber("0612345678")
        .firstName("Mohammed")
        .lastName("Chairi")
        .cin("K000000")
        .walletType(WalletType.P)
        .closeLoopOnly(true)
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/register"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"firstName\":\"Mohammed\"")))
        .andExpect(content().string(containsString("\"lastName\":\"Chairi\"")))
        .andExpect(content().string(containsString("\"cin\":\"K000000\"")))
        .andExpect(content().string(containsString("\"walletType\":\"P\"")))
        .andExpect(content().string(containsString("\"closeLoopOnly\":true")))
        .andRespond(withStatus(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"data\":true}"));

    assertThat(client.registerCustomer(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void previewCardFundingSendsOfficialAmountOnlyPayloadAndMapsOperation() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo(
            "https://sandbox.charimoney.com/api/operations/cashin/card/preview?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andExpect(content().string(not(containsString("\"type\""))))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 1,
                "operation": {
                  "phoneNumber": "+212612345678",
                  "amount": 100,
                  "method": 2,
                  "acceptedBy": 0,
                  "description": ""
                },
                "feesAmount": 0,
                "checkedAt": "2025-04-12T12:55:39.213Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCardFundingPreviewResponse response = client.previewCardFunding("0612345678",
        new BigDecimal("100"));

    assertThat(response.getData().getType()).isEqualTo(1);
    assertThat(response.getData().getOperation().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("100");
    assertThat(response.getData().getOperation().getMethod()).isEqualTo(2);
    assertThat(response.getData().getOperation().getAcceptedBy()).isEqualTo(0);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:55:39.213Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void previewCardFundingByAgentSendsOfficialAmountOnlyPayloadAndMapsOperation() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card/agent/preview?code=21011"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 1,
                "operation": {
                  "code": "21011",
                  "phoneNumber": "+212612345678",
                  "amount": 100,
                  "method": 2
                },
                "feesAmount": 0,
                "checkedAt": "2025-04-12T12:55:39.213Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCardFundingPreviewResponse response = client.previewCardFundingByAgent(" 21011 ",
        new BigDecimal("100"));

    assertThat(response.getData().getType()).isEqualTo(1);
    assertThat(response.getData().getOperation().getCode()).isEqualTo("21011");
    assertThat(response.getData().getOperation().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("100");
    assertThat(response.getData().getOperation().getMethod()).isEqualTo(2);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:55:39.213Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void previewTransferSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariTransferPayload payload = ChariTransferPayload.builder()
        .customerPhoneNumber("0612345678")
        .recipientPhoneNumber("0712345678")
        .amount(new BigDecimal("10"))
        .reason("test preview")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/transfer/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test preview\"")))
        .andExpect(content()
            .string(containsString("\"recipientPhoneNumber\":\"+212712345678\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 3,
                "operation": {
                  "customerPhoneNumber": "+212612345678",
                  "amount": 10,
                  "reason": "test preview",
                  "recipientPhoneNumber": "+212712345678"
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:31:59.313Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariTransferPreviewResponse response = client.previewTransfer(payload);

    assertThat(response.getData().getType()).isEqualTo(3);
    assertThat(response.getData().getOperation().getCustomerPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getOperation().getReason()).isEqualTo("test preview");
    assertThat(response.getData().getOperation().getRecipientPhoneNumber()).isEqualTo("+212712345678");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:31:59.313Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void executeTransferSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariTransferPayload payload = ChariTransferPayload.builder()
        .customerPhoneNumber("0612345678")
        .recipientPhoneNumber("0712345678")
        .amount(new BigDecimal("10"))
        .reason("test operation")
        .beneficiaryId(42)
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/transfer"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test operation\"")))
        .andExpect(content()
            .string(containsString("\"recipientPhoneNumber\":\"+212712345678\"")))
        .andExpect(content().string(containsString("\"beneficiaryId\":42")))
        .andRespond(withSuccess("""
            {
              "data": {
                "operationType": 3,
                "operationId": 2181,
                "amount": 10,
                "feesAmount": 0,
                "totalAmount": 10,
                "reason": "test operation",
                "recipientPhoneNumber": "+212712345678",
                "checkedAt": "2025-04-12T12:36:21.102Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariTransferResponse response = client.executeTransfer(payload);

    assertThat(response.getData().getOperationType()).isEqualTo(3);
    assertThat(response.getData().getTypedOperationType().getCode()).isEqualTo(3);
    assertThat(response.getData().getOperationId()).isEqualTo(2181L);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getReason()).isEqualTo("test operation");
    assertThat(response.getData().getRecipientPhoneNumber()).isEqualTo("+212712345678");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:36:21.102Z");
    server.verify();
  }

  @Test
  void previewBankTransferSendsOfficialBodyIdentifierAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariBankTransferPayload payload = ChariBankTransferPayload.builder()
        .amount(new BigDecimal("10"))
        .reason("test preview")
        .rib("827640000010000000001234")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/bank-transfer/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test preview\"")))
        .andExpect(content().string(containsString("\"rib\":\"827640000010000000001234\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 3,
                "operation": {
                  "customerPhoneNumber": "+212612345678",
                  "amount": 10,
                  "reason": "test preview",
                  "rib": "827640000010000000001234"
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-02T12:31:59.313Z",
                "openLoop": true
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariBankTransferPreviewResponse response = client.previewBankTransfer(payload, "0612345678");

    assertThat(response.getData().getType()).isEqualTo(3);
    assertThat(response.getData().getOperation().getCustomerPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getOperation().getReason()).isEqualTo("test preview");
    assertThat(response.getData().getOperation().getRib()).isEqualTo("827640000010000000001234");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-02T12:31:59.313Z");
    assertThat(response.getData().getOpenLoop()).isTrue();
    server.verify();
  }

  @Test
  void executeBankTransferSendsOfficialBodyIdentifierAndMapsMixedCaseResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariBankTransferPayload payload = ChariBankTransferPayload.builder()
        .amount(new BigDecimal("10"))
        .reason("test operation")
        .rib("827640000010000000001234")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/bank-transfer"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test operation\"")))
        .andExpect(content().string(containsString("\"rib\":\"827640000010000000001234\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 9,
                "Amount": 10,
                "TotalAmount": 10,
                "feesAmount": 0,
                "reason": "test operation",
                "RecipientRib": "827640000010000000001234",
                "checkedAt": "2025-04-02T12:31:59.313Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariBankTransferResponse response = client.executeBankTransfer(payload, "0612345678");

    assertThat(response.getData().getType()).isEqualTo(9);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getReason()).isEqualTo("test operation");
    assertThat(response.getData().getRecipientRib()).isEqualTo("827640000010000000001234");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-02T12:31:59.313Z");
    server.verify();
  }

  @Test
  void previewBankTransferFromApNormalizesConfiguredAgentCodeAndRib() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasProperties properties = properties();
    properties.setPrincipalAgentId(" 1710301 ");
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties);

    ChariBankTransferPayload payload = ChariBankTransferPayload.builder()
        .amount(new BigDecimal("10"))
        .reason("test preview")
        .rib(" 827640000710000000030360 ")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/bank-transfer/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"AgentCode\":\"1710301\"")))
        .andExpect(content().string(containsString("\"rib\":\"827640000710000000030360\"")))
        .andExpect(content().string(not(containsString("1710301 "))))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 3,
                "operation": {
                  "agentCode": "1710301",
                  "amount": 10,
                  "reason": "test preview",
                  "rib": "827640000710000000030360"
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-02T12:31:59.313Z",
                "openLoop": true
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariBankTransferPreviewResponse response = client.previewBankTransferFromAP(payload);

    assertThat(response.getData().getOperation().getAgentCode()).isEqualTo("1710301");
    assertThat(response.getData().getOperation().getRib()).isEqualTo("827640000710000000030360");
    server.verify();
  }

  @Test
  void previewMerchantPaymentByPhoneSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantPaymentByPhonePayload payload = ChariMerchantPaymentByPhonePayload.builder()
        .customerPhoneNumber("0612345678")
        .recipientPhoneNumber("0712345678")
        .amount(new BigDecimal("10"))
        .reason("test merchant push")
        .beneficiaryId(99)
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/payment/push/manual/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test merchant push\"")))
        .andExpect(content()
            .string(containsString("\"recipientPhoneNumber\":\"+212712345678\"")))
        .andExpect(content().string(containsString("\"beneficiaryId\":99")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 5,
                "operation": {
                  "customerPhoneNumber": "+212612345678",
                  "amount": 10,
                  "reason": "test merchant push",
                  "recipientPhoneNumber": "+212712345678",
                  "beneficiaryId": 99
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:31:59.313Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariMerchantPaymentByPhonePreviewResponse response = client.previewMerchantPaymentByPhone(payload);

    assertThat(response.getData().getType()).isEqualTo(5);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
    assertThat(response.getData().getOperation().getCustomerPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getOperation().getReason()).isEqualTo("test merchant push");
    assertThat(response.getData().getOperation().getRecipientPhoneNumber()).isEqualTo("+212712345678");
    assertThat(response.getData().getOperation().getBeneficiaryId()).isEqualTo(99);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:31:59.313Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void executeMerchantPaymentByPhoneSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantPaymentByPhonePayload payload = ChariMerchantPaymentByPhonePayload.builder()
        .customerPhoneNumber("0612345678")
        .recipientPhoneNumber("0712345678")
        .amount(new BigDecimal("10"))
        .reason("test merchant push")
        .beneficiaryId(99)
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/merchant/payment/push/manual"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"reason\":\"test merchant push\"")))
        .andExpect(content()
            .string(containsString("\"recipientPhoneNumber\":\"+212712345678\"")))
        .andExpect(content().string(containsString("\"beneficiaryId\":99")))
        .andRespond(withSuccess("""
            {
              "data": {
                "operationType": 5,
                "amount": 10,
                "feesAmount": 0,
                "totalAmount": 10,
                "reason": "test operation",
                "recipientPhoneNumber": "+212712345678",
                "checkedAt": "2025-04-12T12:36:21.102Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariMerchantPaymentByPhoneResponse response = client.executeMerchantPaymentByPhone(payload);

    assertThat(response.getData().getOperationType()).isEqualTo(5);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getReason()).isEqualTo("test operation");
    assertThat(response.getData().getRecipientPhoneNumber()).isEqualTo("+212712345678");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:36:21.102Z");
    server.verify();
  }

  @Test
  void previewQrCodePaymentSendsOfficialPayloadWithoutAmountAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariQrCodePaymentPayload payload = ChariQrCodePaymentPayload.builder()
        .customerPhoneNumber("0612345678")
        .qrCodeContent("00020101021126xxxxxx")
        .amount(new BigDecimal("10"))
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/payment/push/qrcode/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content()
            .string(containsString("\"qrCodeContent\":\"00020101021126xxxxxx\"")))
        .andExpect(content().string(not(containsString("\"amount\""))))
        .andExpect(content().string(not(containsString("\"CustomerPhoneNumber\""))))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 5,
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:31:59.313Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariQrCodePaymentPreviewResponse response = client.previewQrCodePayment(payload);

    assertThat(response.getData().getType()).isEqualTo(5);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:31:59.313Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void executeQrCodePaymentSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariQrCodePaymentPayload payload = ChariQrCodePaymentPayload.builder()
        .customerPhoneNumber("0612345678")
        .qrCodeContent("00020101021126xxxxxx")
        .amount(new BigDecimal("10"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/merchant/payment/push/qrcode"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content()
            .string(containsString("\"customerPhoneNumber\":\"+212612345678\"")))
        .andExpect(content()
            .string(containsString("\"qrCodeContent\":\"00020101021126xxxxxx\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(not(containsString("\"CustomerPhoneNumber\""))))
        .andRespond(withSuccess("""
            {
              "data": {
                "operationType": 5,
                "amount": 10,
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:36:21.102Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariQrCodePaymentResponse response = client.executeQrCodePayment(payload);

    assertThat(response.getData().getOperationType()).isEqualTo(5);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:36:21.102Z");
    server.verify();
  }

  @Test
  void previewMerchantCardPaymentSendsOfficialQueryAndAmountPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/payment/push/card/preview?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"amount\":250")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 5,
                "operation": {
                  "phoneNumber": "+212612345678",
                  "amount": 250,
                  "method": 2
                },
                "feesAmount": 0,
                "checkedAt": "2025-04-12T12:55:39.213Z",
                "openLoop": false
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariMerchantCardPaymentPreviewResponse response = client.previewMerchantCardPayment("0612345678",
        new BigDecimal("250"));

    assertThat(response.getData().getType()).isEqualTo(5);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.MOBILE_PAYMENT);
    assertThat(response.getData().getOperation().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("250");
    assertThat(response.getData().getOperation().getMethod()).isEqualTo(2);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:55:39.213Z");
    assertThat(response.getData().getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void executeMerchantCardPaymentSendsOfficialPayloadAndMapsRedirectResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantCardPaymentPayload payload = ChariMerchantCardPaymentPayload.builder()
        .firstName("John")
        .lastName("Doe")
        .cvv("123")
        .amount(new BigDecimal("250"))
        .pan("4111111111111111")
        .expiryDate("2608")
        .keepAlive(true)
        .threeDSecure(true)
        .autoCapture(true)
        .notificationUrl("https://merchant.example.com/webhook")
        .acceptUrl("https://merchant.example.com/success")
        .externalReference("ORDER-1001")
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/payment/card?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"firstName\":\"John\"")))
        .andExpect(content().string(containsString("\"lastName\":\"Doe\"")))
        .andExpect(content().string(containsString("\"cvv\":\"123\"")))
        .andExpect(content().string(containsString("\"amount\":250")))
        .andExpect(content().string(containsString("\"pan\":\"4111111111111111\"")))
        .andExpect(content().string(containsString("\"expiryDate\":\"2608\"")))
        .andExpect(content().string(containsString("\"keepAlive\":true")))
        .andExpect(content().string(containsString("\"3dSecure\":true")))
        .andExpect(content().string(containsString("\"autoCapture\":true")))
        .andExpect(content().string(containsString(
            "\"notificationUrl\":\"https://merchant.example.com/webhook\"")))
        .andExpect(content().string(containsString(
            "\"acceptUrl\":\"https://merchant.example.com/success\"")))
        .andExpect(content().string(containsString("\"externalReference\":\"ORDER-1001\"")))
        .andRespond(withSuccess(
            """
                {
                  "data": {
                    "redirect": true,
                    "responseCode": 0,
                    "amount": 250,
                    "transactionTrackId": "600789381213",
                    "orderId": "CH473bbe51d546",
                    "transactionReferenceId": "5852",
                    "redirectionURL": "https://staging-api.charipay.ma/chari-frontend/home_card3",
                    "acceptURL": "https://www.chari.com",
                    "declineURL": "https://www.chari.fail.com",
                    "gateway": "CHARIPAY",
                    "operationId": null,
                    "operationDate": null,
                    "feesAmount": null,
                    "externalReference": null
                  }
                }
                """,
            MediaType.APPLICATION_JSON));

    ChariMerchantCardPaymentResponse response = client.executeMerchantCardPayment("0612345678", payload);

    assertThat(response.getData().getRedirect()).isTrue();
    assertThat(response.getData().getResponseCode()).isEqualTo(0);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("250");
    assertThat(response.getData().getTransactionTrackId()).isEqualTo("600789381213");
    assertThat(response.getData().getOrderId()).isEqualTo("CH473bbe51d546");
    assertThat(response.getData().getTransactionReferenceId()).isEqualTo("5852");
    assertThat(response.getData().getRedirectionURL())
        .isEqualTo("https://staging-api.charipay.ma/chari-frontend/home_card3");
    assertThat(response.getData().getAcceptURL()).isEqualTo("https://www.chari.com");
    assertThat(response.getData().getDeclineURL()).isEqualTo("https://www.chari.fail.com");
    assertThat(response.getData().getGateway()).isEqualTo("CHARIPAY");
    assertThat(response.getData().getOperationId()).isNull();
    assertThat(response.getData().getOperationDate()).isNull();
    assertThat(response.getData().getFeesAmount()).isNull();
    assertThat(response.getData().getExternalReference()).isNull();
    server.verify();
  }

  @Test
  void executeMerchantTokenizedCardPaymentSendsOfficialPayloadAndMapsRedirectResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariMerchantTokenizedCardPaymentPayload payload = ChariMerchantTokenizedCardPaymentPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("188"))
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/payment/tokenized/card/277?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"cvv\":\"123\"")))
        .andExpect(content().string(containsString("\"amount\":188")))
        .andRespond(withSuccess(
            """
                {
                  "data": {
                    "redirect": true,
                    "responseCode": 0,
                    "amount": 188,
                    "transactionTrackId": "600789381214",
                    "orderId": "CH8f2a1b9e4d7c",
                    "transactionReferenceId": "5853",
                    "redirectionURL": "https://staging-api.charipay.ma/chari-frontend/home_card3",
                    "acceptURL": "https://www.chari.com",
                    "declineURL": "https://www.chari.fail.com",
                    "gateway": "CHARIPAY",
                    "operationId": null,
                    "operationDate": null,
                    "feesAmount": null,
                    "externalReference": null
                  }
                }
                """,
            MediaType.APPLICATION_JSON));

    ChariMerchantCardPaymentResponse response = client.executeMerchantTokenizedCardPayment(277,
        "0612345678",
        payload);

    assertThat(response.getData().getRedirect()).isTrue();
    assertThat(response.getData().getResponseCode()).isEqualTo(0);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("188");
    assertThat(response.getData().getTransactionTrackId()).isEqualTo("600789381214");
    assertThat(response.getData().getOrderId()).isEqualTo("CH8f2a1b9e4d7c");
    assertThat(response.getData().getTransactionReferenceId()).isEqualTo("5853");
    assertThat(response.getData().getRedirectionURL())
        .isEqualTo("https://staging-api.charipay.ma/chari-frontend/home_card3");
    assertThat(response.getData().getAcceptURL()).isEqualTo("https://www.chari.com");
    assertThat(response.getData().getDeclineURL()).isEqualTo("https://www.chari.fail.com");
    assertThat(response.getData().getGateway()).isEqualTo("CHARIPAY");
    assertThat(response.getData().getOperationId()).isNull();
    assertThat(response.getData().getOperationDate()).isNull();
    assertThat(response.getData().getFeesAmount()).isNull();
    assertThat(response.getData().getExternalReference()).isNull();
    server.verify();
  }

  @Test
  void previewChargebackSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariChargebackPayload payload = ChariChargebackPayload.builder()
        .sourcePhoneNumber("0612345678")
        .destinationPhoneNumber("0623456789")
        .amount(new BigDecimal("10"))
        .description("test preview")
        .originalOperationId(12345L)
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/chargeback"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"sourcePhoneNumber\":\"+212612345678\"")))
        .andExpect(content()
            .string(containsString("\"destinationPhoneNumber\":\"+212623456789\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andExpect(content().string(containsString("\"description\":\"test preview\"")))
        .andExpect(content().string(containsString("\"originalOperationId\":12345")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 9,
                "Amount": 10,
                "TotalAmount": 10,
                "feesAmount": 0,
                "reason": "test operation",
                "RecipientRib": "827640000010000000001234",
                "checkedAt": "2025-04-02T12:31:59.313Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariChargebackPreviewResponse response = client.previewChargeback(payload);

    assertThat(response.getData().getType()).isEqualTo(9);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.BANK_TRANSFER);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getReason()).isEqualTo("test operation");
    assertThat(response.getData().getRecipientRib()).isEqualTo("827640000010000000001234");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-02T12:31:59.313Z");
    server.verify();
  }

  @Test
  void generateStaticQrCodeSendsOfficialPhoneQueryAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/qrcode/static?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "qrContent": "000201010211269800325bb66a92d",
                "qrCodeReference": null
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariGenerateQrCodeResponse response = client.generateStaticQrCode("0612345678");

    assertThat(response.getData().getQrContent()).isEqualTo("000201010211269800325bb66a92d");
    assertThat(response.getData().getQrCodeContent()).isEqualTo("000201010211269800325bb66a92d");
    assertThat(response.getData().getQrCodeReference()).isNull();
    server.verify();
  }

  @Test
  void generateStaticQrCodeCanSendMaskedNumberQuery() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/qrcode/static?phoneNumber=+212612345678&maskedNumber=true"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "qrContent": "000201010211269800325bb66a92d",
                "qrCodeReference": null
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariGenerateQrCodeResponse response = client.generateStaticQrCode("0612345678", true);

    assertThat(response.getData().getQrContent()).isEqualTo("000201010211269800325bb66a92d");
    assertThat(response.getData().getQrCodeReference()).isNull();
    server.verify();
  }

  @Test
  void generateDynamicQrCodeSendsOfficialPhoneQueryAndAmountPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariGenerateQrCodePayload payload = ChariGenerateQrCodePayload.builder()
        .amount(new BigDecimal("100"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/merchant/qrcode?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andRespond(withSuccess("""
            {
              "data": {
                "qrContent": "000201010212269500325bb66a92d",
                "qrCodeReference": "QR1-251105141040-9029"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariGenerateQrCodeResponse response = client.generateQrCode("0612345678", payload);

    assertThat(response.getData().getQrContent()).isEqualTo("000201010212269500325bb66a92d");
    assertThat(response.getData().getQrCodeContent()).isEqualTo("000201010212269500325bb66a92d");
    assertThat(response.getData().getQrCodeReference()).isEqualTo("QR1-251105141040-9029");
    server.verify();
  }

  @Test
  void generateDynamicQrCodeCanSendMaskedNumberQuery() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariGenerateQrCodePayload payload = ChariGenerateQrCodePayload.builder()
        .amount(new BigDecimal("100"))
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/merchant/qrcode?phoneNumber=+212612345678&maskedNumber=true"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andRespond(withSuccess("""
            {
              "data": {
                "qrContent": "000201010212269500325bb66a92d",
                "qrCodeReference": "QR1-251105141040-9029"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariGenerateQrCodeResponse response = client.generateQrCode("0612345678", true, payload);

    assertThat(response.getData().getQrContent()).isEqualTo("000201010212269500325bb66a92d");
    assertThat(response.getData().getQrCodeReference()).isEqualTo("QR1-251105141040-9029");
    server.verify();
  }

  @Test
  void requestCashinByReferenceSendsOfficialPayloadAndMapsOperationEnums() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCashinByReferencePayload payload = ChariCashinByReferencePayload.builder()
        .phoneNumber("0612345678")
        .amount(new BigDecimal("10"))
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/cashin/request"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"PhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":10")))
        .andRespond(withSuccess("""
            {
              "data": {
                "createdAt": "2025-05-15T23:55:55.082Z",
                "closedAt": null,
                "reference": "1122334455",
                "phoneNumber": "+212612345678",
                "accountId": 35,
                "operationType": 1,
                "operationStatus": 1,
                "partnerId": 1,
                "amount": 10,
                "description": "test request"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCashinByReferenceResponse response = client.requestCashinByReference(payload);

    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-05-15T23:55:55.082Z");
    assertThat(response.getData().getClosedAt()).isNull();
    assertThat(response.getData().getReference()).isEqualTo("1122334455");
    assertThat(response.getData().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getAccountId()).isEqualTo(35L);
    assertThat(response.getData().getOperationType()).isEqualTo(1);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariRequestOperationType.CASHIN);
    assertThat(response.getData().getOperationStatus()).isEqualTo(1);
    assertThat(response.getData().getTypedOperationStatus()).isEqualTo(ChariRequestOperationStatus.OPEN);
    assertThat(response.getData().getPartnerId()).isEqualTo(1);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getDescription()).isEqualTo("test request");
    server.verify();
  }

  @Test
  void requestCashoutByReferenceSendsOfficialPayloadAndMapsOperationEnums() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCashoutByReferencePayload payload = ChariCashoutByReferencePayload.builder()
        .phoneNumber("0612345678")
        .amount(new BigDecimal("100"))
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/cashout/request"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"PhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andRespond(withSuccess("""
            {
              "data": {
                "createdAt": "2025-05-15T23:56:55.082Z",
                "closedAt": null,
                "reference": "1122334456",
                "phoneNumber": "+212612345678",
                "accountId": 35,
                "operationType": 2,
                "operationStatus": 1,
                "partnerId": 1,
                "amount": 100,
                "description": "test request"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCashoutByReferenceResponse response = client.requestCashoutByReference(payload);

    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-05-15T23:56:55.082Z");
    assertThat(response.getData().getClosedAt()).isNull();
    assertThat(response.getData().getReference()).isEqualTo("1122334456");
    assertThat(response.getData().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getAccountId()).isEqualTo(35L);
    assertThat(response.getData().getOperationType()).isEqualTo(2);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariRequestOperationType.CASHOUT);
    assertThat(response.getData().getOperationStatus()).isEqualTo(1);
    assertThat(response.getData().getTypedOperationStatus()).isEqualTo(ChariRequestOperationStatus.OPEN);
    assertThat(response.getData().getPartnerId()).isEqualTo(1);
    assertThat(response.getData().getAmount()).isEqualByComparingTo("100");
    assertThat(response.getData().getDescription()).isEqualTo("test request");
    server.verify();
  }

  @Test
  void getRetailAgentsSendsOfficialFiltersAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/agents/retail?code=123&pageSize=10&pageNumber=1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess(
            """
                {"data":{"collection":[{"Code":"21098","Name":"Agent 1","createdAt":"2025-05-15T23:55:55.082Z","Active":"true","Account":{"id":1}}],"count":7}}
                """,
            MediaType.APPLICATION_JSON));

    ChariRetailAgentsResponse response = client.getRetailAgents("123", 10, 1);

    assertThat(response.getData().getCount()).isEqualTo(7);
    assertThat(response.getData().getCollection().get(0).getCode()).isEqualTo("21098");
    assertThat(response.getData().getCollection().get(0).getName()).isEqualTo("Agent 1");
    assertThat(response.getData().getCollection().get(0).getActive()).isEqualTo("true");
    assertThat(response.getData().getCollection().get(0).getAccount().get("id")).isEqualTo(1);
    server.verify();
  }

  @Test
  void getRetailAgentByCodeSendsOfficialPathAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/agents/retail/123"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess(
            """
                {"data":{"Code":"21098","createdAt":"2025-05-15T23:55:55.082Z","phoneNumber":"+212612345678","Account":{"id":1}}}
                """,
            MediaType.APPLICATION_JSON));

    ChariRetailAgentResponse response = client.getRetailAgentByCode("123");

    assertThat(response.getData().getCode()).isEqualTo("21098");
    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-05-15T23:55:55.082Z");
    assertThat(response.getData().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getAccount().get("id")).isEqualTo(1);
    server.verify();
  }

  @Test
  void getPrincipalAgentInfoSendsOfficialIdPathAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/agents/principal/11098"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "Code": "11098",
                "createdAt": "2025-05-15T23:55:55.082Z",
                "Account": {
                  "accountId": 35,
                  "rib": "827640000010000000001234",
                  "balance": 1000,
                  "currentAccountLevel": 2
                }
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariPrincipalAgentResponse response = client.getPrincipalAgentInfo(" 11098 ");

    assertThat(response.getData().getCode()).isEqualTo("11098");
    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-05-15T23:55:55.082Z");
    assertThat(response.getData().getAccount().getAccountId()).isEqualTo(35L);
    assertThat(response.getData().getAccount().getRib()).isEqualTo("827640000010000000001234");
    assertThat(response.getData().getAccount().getBalance()).isEqualByComparingTo("1000");
    assertThat(response.getData().getAccount().getCurrentAccountLevel()).isEqualTo(2);
    server.verify();
  }

  @Test
  void getPrincipalAgentInfoUsesConfiguredPrincipalAgentId() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasProperties properties = properties();
    properties.setPrincipalAgentId(" 11098 ");
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties);

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/agents/principal/11098"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("""
            {
              "data": {
                "Code": "11098"
              }
            }
            """, MediaType.APPLICATION_JSON));

    client.getPrincipalAgentInfo(null);

    server.verify();
  }

  @Test
  void addRetailAgentSendsOfficialPayloadAndMapsCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariRetailAgentPayload payload = ChariRetailAgentPayload.builder()
        .phoneNumber("0612345678")
        .name("Agent Détaillant Tanger")
        .firstName("Agent")
        .lastName("Detaillant")
        .cin("AB123456")
        .email("agent@chari.com")
        .address("10, Nejma Tanger")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/agents/retail"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"name\":\"Agent Détaillant Tanger\"")))
        .andExpect(content().string(containsString("\"cin\":\"AB123456\"")))
        .andRespond(withSuccess("{\"data\":{\"Code\":\"12033\"}}", MediaType.APPLICATION_JSON));

    ChariRetailAgentCreatedResponse response = client.addRetailAgent(payload);

    assertThat(response.getData().getCode()).isEqualTo("12033");
    server.verify();
  }

  @Test
  void getAndExecuteCashinByReferenceUseOfficialEndpointsAndEnumMappings() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/request?reference=0000000001"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(
            """
                {"data":{"reference":"1122334455","entity":null,"createdAt":"2025-06-03T09:37:26.881973","executedAt":null,"phoneNumber":"+212612345678","amount":1000.0,"description":null,"partner":"ChariMoney","status":1,"type":1}}
                """,
            MediaType.APPLICATION_JSON));
    ChariExecuteRequestOperationByReferencePayload payload = ChariExecuteRequestOperationByReferencePayload
        .builder().code("123").reference("1122334455").build();
    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/cashin/agent"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(containsString("\"code\":\"123\"")))
        .andExpect(content().string(containsString("\"reference\":\"1122334455\"")))
        .andRespond(withSuccess(
            """
                {"data":{"createdAt":"2025-05-15T23:55:55.0821309Z","closedAt":null,"reference":"1122334455","phoneNumber":"+212612345678","accountId":35,"operationType":1,"operationStatus":1,"partnerId":1,"amount":10,"description":"test request"}}
                """,
            MediaType.APPLICATION_JSON));

    ChariCashinByReferenceResponse getResponse = client.getCashinByReference("0000000001");
    assertThat(getResponse.getData().getTypedType()).isEqualTo(ChariRequestOperationType.CASHIN);
    assertThat(getResponse.getData().getTypedStatus()).isEqualTo(ChariRequestOperationStatus.OPEN);
    ChariCashinByReferenceResponse executeResponse = client.executeCashinByReference(payload);
    assertThat(executeResponse.getData().getTypedOperationType())
        .isEqualTo(ChariRequestOperationType.CASHIN);
    assertThat(executeResponse.getData().getTypedOperationStatus())
        .isEqualTo(ChariRequestOperationStatus.OPEN);
    server.verify();
  }

  @Test
  void getAndExecuteCashoutByReferenceUseOfficialEndpointsAndEnumMappings() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashout/request?reference=0000000001"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(
            """
                {"data":{"createdAt":"2025-05-15T23:56:55.08213","closedAt":null,"reference":"1122334455","phoneNumber":"+212612345678","accountId":35,"operationType":2,"operationStatus":1,"partnerId":1,"amount":10,"description":""}}
                """,
            MediaType.APPLICATION_JSON));
    ChariExecuteRequestOperationByReferencePayload payload = ChariExecuteRequestOperationByReferencePayload
        .builder().code("123").reference("1122334455").build();
    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/cashout/agent"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(containsString("\"code\":\"123\"")))
        .andExpect(content().string(containsString("\"reference\":\"1122334455\"")))
        .andRespond(withSuccess(
            """
                {"data":{"createdAt":"2025-05-15T23:55:55.0821309Z","closedAt":null,"reference":"1122334455","phoneNumber":"+212612345678","accountId":35,"operationType":2,"operationStatus":1,"partnerId":1,"amount":10,"description":"test request"}}
                """,
            MediaType.APPLICATION_JSON));

    ChariCashoutByReferenceResponse getResponse = client.getCashoutByReference("0000000001");
    assertThat(getResponse.getData().getTypedOperationType()).isEqualTo(ChariRequestOperationType.CASHOUT);
    assertThat(getResponse.getData().getTypedOperationStatus()).isEqualTo(ChariRequestOperationStatus.OPEN);
    ChariCashoutByReferenceResponse executeResponse = client.executeCashoutByReference(payload);
    assertThat(executeResponse.getData().getTypedOperationType())
        .isEqualTo(ChariRequestOperationType.CASHOUT);
    assertThat(executeResponse.getData().getTypedOperationStatus())
        .isEqualTo(ChariRequestOperationStatus.OPEN);
    server.verify();
  }

  @Test
  void getOperationsByCustomerSendsOfficialFiltersAndMapsEnumFields() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariOperationsByCustomerQuery query = ChariOperationsByCustomerQuery.builder()
        .phoneNumber("0612345678")
        .operationTypes(List.of(ChariOperationType.CASHIN, ChariOperationType.CASHOUT))
        .transactionStatus(ChariOperationStatus.COMPLETED)
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations?phoneNumber=+212612345678&operationType=1&operationType=2&transactionStatus=2"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "collection": [
                  {
                    "operationId": 1525,
                    "transactionId": 2710,
                    "amount": 30,
                    "reason": "test",
                    "transactionDate": "2025-06-25T16:42:41.982Z",
                    "operationType": 1,
                    "accountNumber": "+212612345678",
                    "method": null,
                    "beneficiaryName": "",
                    "transactionStatus": 2,
                    "sens": 1,
                    "openLoop": false
                  }
                ],
                "count": 7
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariOperationsResponse response = client.getOperationsByCustomer(query);

    assertThat(response.getData().getCount()).isEqualTo(7);
    assertThat(response.getData().getCollection()).hasSize(1);
    ChariOperationsResponse.OperationItem item = response.getData().getCollection().get(0);
    assertThat(item.getOperationId()).isEqualTo(1525L);
    assertThat(item.getTransactionId()).isEqualTo(2710L);
    assertThat(item.getAmount()).isEqualByComparingTo("30");
    assertThat(item.getReason()).isEqualTo("test");
    assertThat(item.getTransactionDate()).isEqualTo("2025-06-25T16:42:41.982Z");
    assertThat(item.getOperationType()).isEqualTo(1);
    assertThat(item.getTypedOperationType()).isEqualTo(ChariOperationType.CASHIN);
    assertThat(item.getAccountNumber()).isEqualTo("+212612345678");
    assertThat(item.getMethod()).isNull();
    assertThat(item.getBeneficiaryName()).isEmpty();
    assertThat(item.getTransactionStatus()).isEqualTo(2);
    assertThat(item.getTypedTransactionStatus()).isEqualTo(ChariOperationStatus.COMPLETED);
    assertThat(item.getSens()).isEqualTo(1);
    assertThat(item.getTypedSens()).isEqualTo(ChariSens.CREDIT);
    assertThat(item.getOpenLoop()).isFalse();
    server.verify();
  }

  @Test
  void getAllOperationsByPartnerSendsOfficialEndpointAndFilters() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariOperationsByCustomerQuery query = ChariOperationsByCustomerQuery.builder()
        .phoneNumber("0612345678")
        .operationTypes(List.of(ChariOperationType.CASHIN, ChariOperationType.CASHOUT))
        .transactionStatus(ChariOperationStatus.COMPLETED)
        .build();

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/all?phoneNumber=+212612345678&operationType=1&operationType=2&transactionStatus=2"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "collection": [
                  {
                    "operationId": 1525,
                    "transactionId": 2710,
                    "amount": 30,
                    "reason": "test",
                    "transactionDate": "2025-06-25T16:42:41.982Z",
                    "operationType": 1,
                    "accountNumber": "+212612345678",
                    "method": null,
                    "beneficiaryName": "",
                    "transactionStatus": 2,
                    "sens": 1,
                    "openLoop": false
                  }
                ],
                "count": 7
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariOperationsResponse response = client.getAllOperationsByPartner(query);

    assertThat(response.getData().getCount()).isEqualTo(7);
    assertThat(response.getData().getCollection()).hasSize(1);
    assertThat(response.getData().getCollection().get(0).getTypedOperationType())
        .isEqualTo(ChariOperationType.CASHIN);
    assertThat(response.getData().getCollection().get(0).getTypedTransactionStatus())
        .isEqualTo(ChariOperationStatus.COMPLETED);
    assertThat(response.getData().getCollection().get(0).getTypedSens()).isEqualTo(ChariSens.CREDIT);
    server.verify();
  }

  @Test
  void getOperationByIdSendsOfficialPathAndPhoneQueryAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/operations/123?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "operationId": 1525,
                "transactionId": 2710,
                "transactionReference": "T0101-25062515-1110",
                "amount": 30,
                "reason": null,
                "operationType": 1,
                "transactionDate": "2025-06-25T16:42:41.982Z",
                "sens": 1,
                "transactionStatus": 2,
                "feesAmount": 0,
                "totalAmount": 30,
                "transactionFeesId": null,
                "sender": "+212612345678",
                "receiver": "+212612345678",
                "beneficiary": null,
                "voucherDetails": null
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariOperationResponse response = client.getOperationById(123L, "0612345678");

    assertThat(response.getData().getOperationId()).isEqualTo(1525L);
    assertThat(response.getData().getTransactionId()).isEqualTo(2710L);
    assertThat(response.getData().getTransactionReference()).isEqualTo("T0101-25062515-1110");
    assertThat(response.getData().getAmount()).isEqualByComparingTo("30");
    assertThat(response.getData().getReason()).isNull();
    assertThat(response.getData().getOperationType()).isEqualTo(1);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.CASHIN);
    assertThat(response.getData().getTransactionDate()).isEqualTo("2025-06-25T16:42:41.982Z");
    assertThat(response.getData().getSens()).isEqualTo(1);
    assertThat(response.getData().getTypedSens()).isEqualTo(ChariSens.CREDIT);
    assertThat(response.getData().getTransactionStatus()).isEqualTo(2);
    assertThat(response.getData().getTypedTransactionStatus()).isEqualTo(ChariOperationStatus.COMPLETED);
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("30");
    assertThat(response.getData().getTransactionFeesId()).isNull();
    assertThat(response.getData().getSender()).isEqualTo("+212612345678");
    assertThat(response.getData().getReceiver()).isEqualTo("+212612345678");
    assertThat(response.getData().getBeneficiary()).isNull();
    assertThat(response.getData().getVoucherDetails()).isNull();
    server.verify();
  }

  @Test
  void previewRefundSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariRefundPayload payload = ChariRefundPayload.builder()
        .phoneNumber("0612345678")
        .operationId(1234L)
        .refundAmount(new BigDecimal("200"))
        .orderId("b981430df1")
        .transactionTrackId("230648378573")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/refund/preview"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"PhoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"operationId\":1234")))
        .andExpect(content().string(containsString("\"refundAmount\":200")))
        .andExpect(content().string(containsString("\"orderId\":\"b981430df1\"")))
        .andExpect(content().string(containsString("\"transactionTrackId\":\"230648378573\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 7,
                "operation": {
                  "PhoneNumber": "+212612345678",
                  "operationId": 1234,
                  "refundAmount": 200,
                  "orderId": "b981430df1",
                  "transactionTrackId": "230648378573"
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:31:59.31347Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariRefundResponse response = client.previewRefund(payload);

    assertThat(response.getData().getType()).isEqualTo(7);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.PAYMENT_REFUND);
    assertThat(response.getData().getOperation().getPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getOperationId()).isEqualTo(1234L);
    assertThat(response.getData().getOperation().getRefundAmount()).isEqualByComparingTo("200");
    assertThat(response.getData().getOperation().getOrderId()).isEqualTo("b981430df1");
    assertThat(response.getData().getOperation().getTransactionTrackId()).isEqualTo("230648378573");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:31:59.31347Z");
    server.verify();
  }

  @Test
  void executeRefundSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariRefundPayload payload = ChariRefundPayload.builder()
        .phoneNumber("0612345678")
        .operationId(1234L)
        .refundAmount(new BigDecimal("200"))
        .orderId("b981430df1")
        .transactionTrackId("230648378573")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/operations/refund"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(not(containsString("\"PhoneNumber\""))))
        .andExpect(content().string(containsString("\"operationId\":1234")))
        .andExpect(content().string(containsString("\"refundAmount\":200")))
        .andExpect(content().string(containsString("\"orderId\":\"b981430df1\"")))
        .andExpect(content().string(containsString("\"transactionTrackId\":\"230648378573\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "type": 7,
                "operation": {
                  "customerPhoneNumber": "+212612345678",
                  "amount": 200,
                  "reason": "test preview",
                  "beneficiaryId": null,
                  "recipientPhoneNumber": "+212712345678"
                },
                "feesAmount": 0,
                "totalAmount": 10,
                "checkedAt": "2025-04-12T12:31:59.31347Z"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariRefundResponse response = client.executeRefund(payload);

    assertThat(response.getData().getType()).isEqualTo(7);
    assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.PAYMENT_REFUND);
    assertThat(response.getData().getOperation().getCustomerPhoneNumber()).isEqualTo("+212612345678");
    assertThat(response.getData().getOperation().getAmount()).isEqualByComparingTo("200");
    assertThat(response.getData().getOperation().getReason()).isEqualTo("test preview");
    assertThat(response.getData().getOperation().getBeneficiaryId()).isNull();
    assertThat(response.getData().getOperation().getRecipientPhoneNumber()).isEqualTo("+212712345678");
    assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
    assertThat(response.getData().getTotalAmount()).isEqualByComparingTo("10");
    assertThat(response.getData().getCheckedAt()).isEqualTo("2025-04-12T12:31:59.31347Z");
    server.verify();
  }

  @Test
  void executeCardFundingUsesPayloadRedirectUrlsAndMapsOfficialResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

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

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().string(containsString("\"pan\":\"4918914107195005\"")))
        .andExpect(content().string(containsString("\"cvv\":\"123\"")))
        .andExpect(content().string(containsString("\"expiryDate\":\"2505\"")))
        .andExpect(content().string(containsString("\"keepAlive\":true")))
        .andExpect(content().string(containsString("\"cardName\":\"my_cfg_card\"")))
        .andExpect(content()
            .string(containsString("\"acceptURL\":\"https://example.com/accept\"")))
        .andExpect(content().string(
            containsString("\"declineURL\":\"https://example.com/decline\"")))
        .andExpect(content().string(not(containsString("\"acceptUrl\""))))
        .andExpect(content().string(not(containsString("\"declineUrl\""))))
        .andExpect(content().string(not(containsString("tiki-pay.mobileappexpert.dev"))))
        .andRespond(withSuccess("""
            {
              "data": {
                "redirect": true,
                "amount": 100,
                "transactionTrackId": "80832126-848",
                "orderId": "edc5608819",
                "transactionReferenceId": "2003",
                "redirectionURL": "https://staging-api.charipay.ma/...",
                "acceptURL": "https://example.com/accept",
                "declineURL": "https://example.com/decline"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCardFundingExecutionResponse response = client.executeCardFunding("0612345678", payload);

    assertThat(response.getData().getRedirect()).isTrue();
    assertThat(response.getData().getAmount()).isEqualByComparingTo("100");
    assertThat(response.getData().getTransactionTrackId()).isEqualTo("80832126-848");
    assertThat(response.getData().getOrderId()).isEqualTo("edc5608819");
    assertThat(response.getData().getTransactionReferenceId()).isEqualTo("2003");
    assertThat(response.getData().getRedirectionURL()).isEqualTo("https://staging-api.charipay.ma/...");
    assertThat(response.getData().getAcceptURL()).isEqualTo("https://example.com/accept");
    assertThat(response.getData().getDeclineURL()).isEqualTo("https://example.com/decline");
    server.verify();
  }

  @Test
  void executeCardFundingUsesConfiguredRedirectUrlsWhenPayloadOmitsThem() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasProperties properties = properties();
    properties.getCardFunding().setAcceptUrl("https://configured.example/accept");
    properties.getCardFunding().setDeclineUrl("https://configured.example/decline");
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties);

    ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("4918914107195005")
        .expiryDate("08/26")
        .cvv("123")
        .amount(new BigDecimal("100"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(
            containsString("\"acceptURL\":\"https://configured.example/accept\"")))
        .andExpect(content().string(containsString(
            "\"declineURL\":\"https://configured.example/decline\"")))
        .andRespond(withSuccess("{\"data\":{\"redirect\":false,\"amount\":100}}",
            MediaType.APPLICATION_JSON));

    assertThat(client.executeCardFunding("0612345678", payload).getData().getRedirect()).isFalse();
    server.verify();
  }

  @Test
  void executeCardFundingOmitsRedirectUrlsWhenPayloadAndConfigAreNull() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("4918914107195005")
        .expiryDate("08/26")
        .cvv("123")
        .amount(new BigDecimal("100"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(not(containsString("acceptURL"))))
        .andExpect(content().string(not(containsString("declineURL"))))
        .andExpect(content().string(not(containsString("acceptUrl"))))
        .andExpect(content().string(not(containsString("declineUrl"))))
        .andRespond(withSuccess("{\"data\":{\"redirect\":false,\"amount\":100}}",
            MediaType.APPLICATION_JSON));

    assertThat(client.executeCardFunding("0612345678", payload).getData().getRedirect()).isFalse();
    server.verify();
  }

  @Test
  void executeCardFundingByAgentSendsOfficialPayloadAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCardCashinPayload payload = ChariCardCashinPayload.builder()
        .firstName("Mohammed")
        .lastName("Chairi")
        .pan("1234567890123456")
        .expiryDate("2505")
        .cvv("123")
        .amount(new BigDecimal("100"))
        .keepAlive(true)
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card/agent?code=11023"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"firstName\":\"Mohammed\"")))
        .andExpect(content().string(containsString("\"lastName\":\"Chairi\"")))
        .andExpect(content().string(containsString("\"cvv\":\"123\"")))
        .andExpect(content().string(containsString("\"amount\":100")))
        .andExpect(content().string(containsString("\"pan\":\"1234567890123456\"")))
        .andExpect(content().string(containsString("\"expiryDate\":\"2505\"")))
        .andExpect(content().string(containsString("\"keepAlive\":true")))
        .andExpect(content().string(not(containsString("acceptURL"))))
        .andExpect(content().string(not(containsString("declineURL"))))
        .andRespond(withSuccess("""
            {
              "data": {
                "redirect": true,
                "amount": 100,
                "transactionTrackId": "80832126-848",
                "orderId": "edc5608819",
                "transactionReferenceId": "2003",
                "redirectionURL": "https://staging-api.charipay.ma/...",
                "acceptURL": null,
                "declineURL": null
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariCardFundingExecutionResponse response = client.executeCardFundingByAgent(" 11023 ", payload);

    assertThat(response.getData().getRedirect()).isTrue();
    assertThat(response.getData().getAmount()).isEqualByComparingTo("100");
    assertThat(response.getData().getTransactionTrackId()).isEqualTo("80832126-848");
    assertThat(response.getData().getOrderId()).isEqualTo("edc5608819");
    assertThat(response.getData().getTransactionReferenceId()).isEqualTo("2003");
    assertThat(response.getData().getRedirectionURL()).isEqualTo("https://staging-api.charipay.ma/...");
    assertThat(response.getData().getAcceptURL()).isNull();
    assertThat(response.getData().getDeclineURL()).isNull();
    server.verify();
  }

  @Test
  void listSavedCardsSendsOfficialPaginationAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/customers/tokenized/cards?phoneNumber=+212612345678&pageSize=5&pageNumber=1"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "collection": [
                  {
                    "customerBankCardId": 33,
                    "maskedPan": "****1017",
                    "cardExpiryDate": "2512",
                    "issuer": null,
                    "createdAt": "2025-04-21T17:36:02.797Z",
                    "cardholderFirstname": "FirstName",
                    "cardholderLastname": "LastName",
                    "scheme": null
                  },
                  {
                    "customerBankCardId": 50,
                    "maskedPan": "****5005",
                    "cardExpiryDate": "2605",
                    "issuer": "CDM",
                    "createdAt": "2025-06-19T12:51:40.850Z",
                    "cardholderFirstname": "ABDENNOUR",
                    "cardholderLastname": "HASSOUNE",
                    "scheme": null,
                    "cardName": "VIP"
                  }
                ],
                "count": 10
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariListSavedCardsResponse response = client.listSavedCards("0612345678", 5, 1);

    assertThat(response.getData().getCount()).isEqualTo(10);
    assertThat(response.getData().getCollection()).hasSize(2);
    assertThat(response.getData().getCollection().get(0).getCustomerBankCardId()).isEqualTo(33);
    assertThat(response.getData().getCollection().get(0).getMaskedPan()).isEqualTo("****1017");
    assertThat(response.getData().getCollection().get(0).getCardExpiryDate()).isEqualTo("2512");
    assertThat(response.getData().getCollection().get(0).getIssuer()).isNull();
    assertThat(response.getData().getCollection().get(0).getCreatedAt())
        .isEqualTo("2025-04-21T17:36:02.797Z");
    assertThat(response.getData().getCollection().get(0).getCardholderFirstname()).isEqualTo("FirstName");
    assertThat(response.getData().getCollection().get(0).getCardholderLastname()).isEqualTo("LastName");
    assertThat(response.getData().getCollection().get(0).getScheme()).isNull();
    assertThat(response.getData().getCollection().get(1).getCustomerBankCardId()).isEqualTo(50);
    assertThat(response.getData().getCollection().get(1).getIssuer()).isEqualTo("CDM");
    assertThat(response.getData().getCollection().get(1).getCardName()).isEqualTo("VIP");
    server.verify();
  }

  @Test
  void getSavedCardSendsOfficialPathAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo(
        "https://sandbox.charimoney.com/api/customers/tokenized/cards/48?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andRespond(withSuccess("""
            {
              "data": {
                "customerBankCardId": 48,
                "maskedPan": "****5005",
                "cardExpiryDate": "2905",
                "issuer": "ABB",
                "createdAt": "2025-06-15T14:12:04.561Z",
                "cardholderFirstname": "ABDENNOUR",
                "cardholderLastname": "HASSOUNE",
                "scheme": null,
                "cardName": "ABB"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariSavedCardResponse response = client.getSavedCard(48, "0612345678");

    assertThat(response.getData().getCustomerBankCardId()).isEqualTo(48);
    assertThat(response.getData().getMaskedPan()).isEqualTo("****5005");
    assertThat(response.getData().getCardExpiryDate()).isEqualTo("2905");
    assertThat(response.getData().getIssuer()).isEqualTo("ABB");
    assertThat(response.getData().getCreatedAt()).isEqualTo("2025-06-15T14:12:04.561Z");
    assertThat(response.getData().getCardholderFirstname()).isEqualTo("ABDENNOUR");
    assertThat(response.getData().getCardholderLastname()).isEqualTo("HASSOUNE");
    assertThat(response.getData().getScheme()).isNull();
    assertThat(response.getData().getCardName()).isEqualTo("ABB");
    server.verify();
  }

  @Test
  void cashinWithSavedCardUsesPayloadRedirectUrlsAndMapsOfficialResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariSavedCardCashinPayload payload = ChariSavedCardCashinPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("200"))
        .acceptURL("https://example.com/accept")
        .declineURL("https://example.com/decline")
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card/123?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"cvv\":\"123\"")))
        .andExpect(content().string(containsString("\"amount\":200")))
        .andExpect(content()
            .string(containsString("\"acceptURL\":\"https://example.com/accept\"")))
        .andExpect(content().string(
            containsString("\"declineURL\":\"https://example.com/decline\"")))
        .andExpect(content().string(not(containsString("acceptUrl"))))
        .andExpect(content().string(not(containsString("declineUrl"))))
        .andExpect(content().string(not(containsString("tiki-pay.mobileappexpert.dev"))))
        .andRespond(withSuccess("""
            {
              "data": {
                "redirect": true,
                "amount": 200,
                "transactionTrackId": "80832126-848",
                "orderId": "edc5608819",
                "transactionReferenceId": "2003",
                "redirectionURL": "https://staging-api.charipay.ma/...",
                "acceptURL": "https://example.com/accept",
                "declineURL": "https://example.com/decline"
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariSavedCardCashinResponse response = client.cashinWithSavedCard(123, "0612345678", payload);

    assertThat(response.getData().getRedirect()).isTrue();
    assertThat(response.getData().getAmount()).isEqualByComparingTo("200");
    assertThat(response.getData().getTransactionTrackId()).isEqualTo("80832126-848");
    assertThat(response.getData().getOrderId()).isEqualTo("edc5608819");
    assertThat(response.getData().getTransactionReferenceId()).isEqualTo("2003");
    assertThat(response.getData().getRedirectionURL()).isEqualTo("https://staging-api.charipay.ma/...");
    assertThat(response.getData().getAcceptURL()).isEqualTo("https://example.com/accept");
    assertThat(response.getData().getDeclineURL()).isEqualTo("https://example.com/decline");

    server.verify();
  }

  @Test
  void cashinWithSavedCardUsesConfiguredRedirectUrlsWhenPayloadOmitsThem() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasProperties properties = properties();
    properties.getCardFunding().setAcceptUrl("https://configured.example/accept");
    properties.getCardFunding().setDeclineUrl("https://configured.example/decline");
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties);

    ChariSavedCardCashinPayload payload = ChariSavedCardCashinPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("200"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card/123?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(
            containsString("\"acceptURL\":\"https://configured.example/accept\"")))
        .andExpect(content().string(containsString(
            "\"declineURL\":\"https://configured.example/decline\"")))
        .andRespond(withSuccess("{\"data\":{\"redirect\":false,\"amount\":200}}",
            MediaType.APPLICATION_JSON));

    assertThat(client.cashinWithSavedCard(123, "0612345678", payload).getData().getRedirect()).isFalse();
    server.verify();
  }

  @Test
  void cashinWithSavedCardOmitsRedirectUrlsWhenPayloadAndConfigAreNull() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariSavedCardCashinPayload payload = ChariSavedCardCashinPayload.builder()
        .cvv("123")
        .amount(new BigDecimal("200"))
        .build();

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/operations/cashin/card/123?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(not(containsString("acceptURL"))))
        .andExpect(content().string(not(containsString("declineURL"))))
        .andExpect(content().string(not(containsString("acceptUrl"))))
        .andExpect(content().string(not(containsString("declineUrl"))))
        .andRespond(withSuccess("{\"data\":{\"redirect\":false,\"amount\":200}}",
            MediaType.APPLICATION_JSON));

    assertThat(client.cashinWithSavedCard(123, "0612345678", payload).getData().getRedirect()).isFalse();
    server.verify();
  }

  @Test
  void apiErrorsExposeChariErrorCodeAndDescription() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/status?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 20005,
                  "errorDescription": "The specified user could not be found."
                }
                """));

    assertThatThrownBy(() -> client.getCustomerStatus("0612345678"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getHttpStatusCode()).isEqualTo(400);
          assertThat(exception.getErrorCode()).isEqualTo(20005);
          assertThat(exception.getErrorDescription())
              .isEqualTo("The specified user could not be found.");
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.USER_NOT_FOUND);
          assertThat(exception.hasErrorCode(ChariErrorCode.USER_NOT_FOUND)).isTrue();
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void registerCustomerErrorsExposeKnownRegistrationCodes() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariRegisterCustomerPayload payload = ChariRegisterCustomerPayload.builder()
        .phoneNumber("0612345678")
        .firstName("Mohammed")
        .lastName("Chairi")
        .cin("K000000")
        .walletType("P")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/register"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 20008,
                  "errorDescription": "Registration is temporarily locked due to security or policy restrictions."
                }
                """));

    assertThatThrownBy(() -> client.registerCustomer(payload))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.REGISTRATION_TEMPORARILY_LOCKED);
          assertThat(exception
              .hasErrorCode(ChariErrorCode.REGISTRATION_TEMPORARILY_LOCKED))
              .isTrue();
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void confirmCustomerNormalizesPhoneAndSendsOfficialBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCustomerConfirmPayload payload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365-768")
        .walletType(WalletType.P)
        .autoActivate(true)
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/confirm"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"code\":\"365-768\"")))
        .andExpect(content().string(containsString("\"walletType\":\"P\"")))
        .andExpect(content().string(containsString("\"autoActivate\":true")))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.confirmCustomer(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void confirmCustomerFormatsCompactOtpCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCustomerConfirmPayload payload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365768")
        .walletType(WalletType.P)
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/confirm"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"code\":\"365-768\"")))
        .andExpect(content().string(not(containsString("\"code\":\"365768\""))))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.confirmCustomer(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void confirmCustomerCanOmitOptionalAutoActivate() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCustomerConfirmPayload payload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365-768")
        .walletType("P")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/confirm"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().string(not(containsString("autoActivate"))))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.confirmCustomer(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void confirmCustomerErrorsExposePendingRequestCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCustomerConfirmPayload payload = ChariCustomerConfirmPayload.builder()
        .phoneNumber("0612345678")
        .code("365-768")
        .walletType("P")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/confirm"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 20017,
                  "errorDescription": "There is no pending request associated with the provided Phone Number."
                }
                """));

    assertThatThrownBy(() -> client.confirmCustomer(payload))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.NO_PENDING_REQUEST_FOR_PHONE_NUMBER);
          assertThat(exception.getErrorCode()).isEqualTo(20017);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void resendCustomerOtpPostsWithoutBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/confirm/resend-otp?phoneNumber=+212612345678"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().string(""))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.resendCustomerOtp("0612345678").getData()).isTrue();
    server.verify();
  }

  @Test
  void loginWithPinNormalizesPhoneAndMapsResponse() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariLoginWithPinPayload payload = ChariLoginWithPinPayload.builder()
        .phoneNumber("0612345678")
        .pin("0000")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/login"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"pin\":\"0000\"")))
        .andRespond(withSuccess("""
            {
              "data": {
                "logged": true,
                "remainingAttempts": 5
              }
            }
            """, MediaType.APPLICATION_JSON));

    ChariLoginWithPinResponse response = client.loginWithPin(payload);

    assertThat(response.getData().getLogged()).isTrue();
    assertThat(response.getData().getRemainingAttempts()).isEqualTo(5);
    server.verify();
  }

  @Test
  void loginWithPinErrorsExposeIncorrectPinCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/login"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 26001,
                  "errorDescription": "The entered PIN is incorrect."
                }
                """));

    assertThatThrownBy(() -> client.loginWithPin("0612345678", "0000"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.INCORRECT_PIN);
          assertThat(exception.getErrorCode()).isEqualTo(26001);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void createPinNormalizesPhoneAndSendsOfficialBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariCreatePinPayload payload = ChariCreatePinPayload.builder()
        .phoneNumber("0612345678")
        .pin("0000")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"pin\":\"0000\"")))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.createPin(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void createPinErrorsExposeAlreadySetCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 26004,
                  "errorDescription": "A PIN has already been set for this wallet."
                }
                """));

    assertThatThrownBy(() -> client.createPin("0612345678", "0000"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.PIN_ALREADY_SET);
          assertThat(exception.getErrorCode()).isEqualTo(26004);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void createPinErrorsExposeInvalidPinFormatCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 26005,
                  "errorDescription": "The provided PIN does not meet the required format (must be a 4-digit number)."
                }
                """));

    assertThatThrownBy(() -> client.createPin("0612345678", "12"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.INVALID_PIN_FORMAT);
          assertThat(exception.getErrorCode()).isEqualTo(26005);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void updatePinNormalizesPhoneAndSendsOfficialPatchBody() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    ChariUpdatePinPayload payload = ChariUpdatePinPayload.builder()
        .phoneNumber("0612345678")
        .oldPin("0000")
        .newPin("1111")
        .build();

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin/change"))
        .andExpect(method(HttpMethod.PATCH))
        .andExpect(header("Chari-Api-Key", "test-key"))
        .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("\"phoneNumber\":\"+212612345678\"")))
        .andExpect(content().string(containsString("\"oldPin\":\"0000\"")))
        .andExpect(content().string(containsString("\"newPin\":\"1111\"")))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    assertThat(client.updatePin(payload).getData()).isTrue();
    server.verify();
  }

  @Test
  void updatePinErrorsExposeIncorrectPinCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin/change"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 26001,
                  "errorDescription": "The entered PIN is incorrect."
                }
                """));

    assertThatThrownBy(() -> client.updatePin("0612345678", "0000", "1111"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.INCORRECT_PIN);
          assertThat(exception.getErrorCode()).isEqualTo(26001);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void updatePinErrorsExposeInvalidPinFormatCode() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/customers/pin/change"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 26005,
                  "errorDescription": "The provided PIN does not meet the required format (must be a 4-digit number)."
                }
                """));

    assertThatThrownBy(() -> client.updatePin("0612345678", "0000", "12"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.INVALID_PIN_FORMAT);
          assertThat(exception.getErrorCode()).isEqualTo(26005);
          assertThat(exception.isBusinessError()).isTrue();
        });

    server.verify();
  }

  @Test
  void lockedErrorsAreEasyToIdentify() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

    server.expect(once(),
        requestTo("https://sandbox.charimoney.com/api/customers/status?phoneNumber=+212612345678"))
        .andRespond(withStatus(HttpStatus.LOCKED)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {
                  "errorCode": 423,
                  "errorDescription": "The access is locked for the given customer."
                }
                """));

    assertThatThrownBy(() -> client.getCustomerStatus("0612345678"))
        .isInstanceOfSatisfying(ChariBaasException.class, exception -> {
          assertThat(exception.getHttpStatusCode()).isEqualTo(423);
          assertThat(exception.getKnownErrorCode())
              .isEqualTo(ChariErrorCode.ACCOUNT_LOCKED);
          assertThat(exception.isAccountLocked()).isTrue();
        });

    server.verify();
  }

  private static ChariBaasProperties properties() {
    ChariBaasProperties properties = new ChariBaasProperties();
    properties.setBaseUrl("https://sandbox.charimoney.com");
    properties.setApiKey("test-key");
    properties.getAudit().setEnabled(false);
    return properties;
  }

  private static String uuidPattern() {
    return "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
  }

  private static ByteArrayResource resource(String filename, String content) {
    return new ByteArrayResource(content.getBytes()) {
      @Override
      public String getFilename() {
        return filename;
      }
    };
  }
}
