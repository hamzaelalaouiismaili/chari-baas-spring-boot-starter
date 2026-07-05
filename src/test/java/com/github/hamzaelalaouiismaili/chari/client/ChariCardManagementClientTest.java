package com.github.hamzaelalaouiismaili.chari.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardAction;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardApplicationStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardDeliveryStatus;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariCardType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariIssuedCardStatus;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardApplicationsQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardTransactionsQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardUsageControlPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariManagedCardsQuery;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardApplicationResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardApplicationsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardProgramsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCardTransactionsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariManagedCardResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariManagedCardsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariCardManagementClientTest {

    @Test
    void getsProgramsWithTypedCardType() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/programs?pageSize=10&pageNumber=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{"cardProgramId":123123,
                        "programName":"Chari Standard Visa","binRange":"44661830",
                        "cardTypeId":1,"cardTypeName":"PHYSICAL","cardSchemeId":1,
                        "cardSchemeName":"VISA","currencyCode":"MAD","validityYears":4,
                        "renewalDaysBefore":30,"pinTriesLimit":3,"isActive":true,
                        "allowAtm":true,"allowOnline":false,"allowPos":true,
                        "allowInternational":false,"contactlessEnabled":true,
                        "createdAt":"2025-04-06T15:14:59.948","partnerId":2,
                        "totalCards":91,"activeCards":62,"price":50}],"count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariCardProgramsResponse response = context.client.getCardPrograms();

        var program = response.getData().getCollection().getFirst();
        assertThat(program.getCardProgramId()).isEqualTo(123123L);
        assertThat(program.getTypedCardType()).isEqualTo(ChariCardType.PHYSICAL);
        assertThat(program.getPrice()).isEqualByComparingTo("50");
        context.server.verify();
    }

    @Test
    void managesApplicationLifecycleAndFilters() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/applications?phoneNumber=+212665638046&cardProgramId=1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(""))
                .andRespond(withSuccess("{\"data\":{}}", MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/applications?pageSize=10&pageNumber=1&status=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(applicationsJson(2), MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/applications/customer?pageSize=10&pageNumber=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(applicationsJson(2), MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/applications/333/validate?phoneNumber=+212665638046"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(applicationJson(2), MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/applications/333/reject?phoneNumber=+212665638046"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(applicationJson(3), MediaType.APPLICATION_JSON));

        context.client.addCardApplication("0665638046", 1L);
        ChariCardApplicationsResponse applications = context.client.getCardApplications(
                ChariCardApplicationsQuery.builder()
                        .pageSize(10)
                        .pageNumber(1)
                        .status(ChariCardApplicationStatus.VALIDATED)
                        .build());
        context.client.getCardApplicationsByCustomer(10, 1);
        ChariCardApplicationResponse validated =
                context.client.validateCardApplication(333L, "0665638046");
        ChariCardApplicationResponse rejected =
                context.client.rejectCardApplication(333L, "0665638046");

        assertThat(applications.getData().getCount()).isEqualTo(1);
        assertThat(validated.getData().getTypedApplicationStatus())
                .isEqualTo(ChariCardApplicationStatus.VALIDATED);
        assertThat(rejected.getData().getTypedApplicationStatus())
                .isEqualTo(ChariCardApplicationStatus.REJECTED);
        context.server.verify();
    }

    @Test
    void filtersCardsAndMapsStatusesAndLimits() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards?pageSize=10&pageNumber=1&phoneNumber=+212665638046&cardProgramId=1&deliveryStatusId=2&cardStatusId=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":{\"collection\":[" + cardJson() + "],\"count\":1}}",
                        MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/123?phoneNumber=+212665638046"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":" + cardJson() + "}", MediaType.APPLICATION_JSON));

        ChariManagedCardsResponse cards = context.client.getManagedCards(ChariManagedCardsQuery.builder()
                .pageSize(10)
                .pageNumber(1)
                .phoneNumber("0665638046")
                .cardProgramId(1L)
                .deliveryStatus(ChariCardDeliveryStatus.SENT_TO_PERSONALIZATION)
                .cardStatus(ChariIssuedCardStatus.ISSUED)
                .build());
        ChariManagedCardResponse card = context.client.getManagedCard(123L, "0665638046");

        assertThat(cards.getData().getCount()).isEqualTo(1);
        assertThat(card.getData().getTypedCardType()).isEqualTo(ChariCardType.PHYSICAL);
        assertThat(card.getData().getTypedCardStatus()).isEqualTo(ChariIssuedCardStatus.ISSUED);
        assertThat(card.getData().getTypedDeliveryStatus())
                .isEqualTo(ChariCardDeliveryStatus.SENT_TO_PERSONALIZATION);
        assertThat(card.getData().getLimits().getFirst().getAmount()).isEqualByComparingTo("20000.0");
        context.server.verify();
    }

    @Test
    void runsTypedActionAndUpdatesAllUsageControls() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/123/activate?phoneNumber=+212665638046"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/123/services?phoneNumber=+212665638046"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json("""
                        {"allowAtm":true,"allowOnline":false,
                        "allowPos":true,"contactlessEnabled":false}
                        """))
                .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

        assertThat(context.client.activateCard(123L, "0665638046").getData()).isTrue();
        assertThat(context.client.updateCardUsageControl(
                123L,
                "0665638046",
                ChariCardUsageControlPayload.builder()
                        .allowAtm(true)
                        .allowOnline(false)
                        .allowPos(true)
                        .contactlessEnabled(false)
                        .build()).getData()).isTrue();
        assertThat(ChariCardAction.values()).extracting(ChariCardAction::getPath)
                .containsExactly("activate", "block", "suspend", "reactivate", "cancel",
                        "unblock-pin", "reset-pin");
        context.server.verify();
    }

    @Test
    void getsCardTransactionsWithFilters() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/cards/123/transactions?pageSize=10&pageNumber=1&phoneNumber=+212665638046&from=2026-05-01&to=2026-05-31"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{"cardTransactionId":123,"transactionId":123,
                        "cardId":11,"maskedPan":"446618XXXXXX0000","description":"ACHAT PAR CARTE",
                        "merchantName":"MERCHANT","merchantCity":null,"merchantCountry":"MA",
                        "merchantCategoryCode":"7372","authCode":"000000","terminalId":"00000000",
                        "isContactless":false,"transactionMethod":2,"amount":3,"currencyCode":"MAD",
                        "transactionStatus":1,"transactionType":20,
                        "transactionDate":"2026-05-22T12:37:32.372823",
                        "createdAt":"2026-05-22T12:37:32.376579","partnerId":2,
                        "operationType":13,"operationId":123123}],"count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariCardTransactionsResponse response = context.client.getCardTransactions(
                123L,
                ChariCardTransactionsQuery.builder()
                        .pageSize(10)
                        .pageNumber(1)
                        .phoneNumber("0665638046")
                        .from("2026-05-01")
                        .to("2026-05-31")
                        .build());

        assertThat(response.getData().getCollection().getFirst().getAmount())
                .isEqualByComparingTo("3");
        assertThat(response.getData().getCollection().getFirst().getOperationId())
                .isEqualTo(123123L);
        context.server.verify();
    }

    @Test
    void rejectsIncompleteUsageControlBeforeApiCall() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

        assertThatThrownBy(() -> client.updateCardUsageControl(
                123L,
                "0665638046",
                ChariCardUsageControlPayload.builder().allowAtm(true).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All card usage-control fields are required");
    }

    private String applicationsJson(int status) {
        return "{\"data\":{\"collection\":[" + applicationDataJson(status) + "],\"count\":1}}";
    }

    private String applicationJson(int status) {
        return "{\"data\":" + applicationDataJson(status) + "}";
    }

    private String applicationDataJson(int status) {
        return """
                {"customerId":2012,"applicationId":333,"customerFullName":"MOHAMMED CHAIRI",
                "cardProgramId":1,"cardProgramName":"Chari Standard Visa","applicationStatus":%d,
                "createdAt":"2026-01-15T23:10:59.80313","validatedAt":null,
                "validatedByUsername":null,"rejectionReason":null,"isManualEntry":false,
                "phoneNumber":"+212665638046","partnerId":2,"defaultLimits":null}
                """.formatted(status);
    }

    private String cardJson() {
        return """
                {"customerId":123,"cardId":123,"cardProgramId":1,
                "cardProgramName":"Chari Standard Visa","binRange":"44661830",
                "cardType":"PHYSICAL","cardScheme":"VISA","customerFullName":"TEST TEST",
                "cardToken":null,"maskedPan":"446618XXXXXX0000","cardStatus":1,
                "deliveryStatus":2,"issueDate":"2026-05-19T11:32:16.364611",
                "activationDate":null,"expiryDate":"2030-05-19T11:32:16.364611",
                "newExpiryDate":null,"embossedName":"TEST TEST","isVirtual":false,
                "pinSet":false,"failedPinAttempts":0,"lockedUntil":null,
                "createdAt":"2026-05-19T11:32:16.364614","partnerId":1,
                "limits":[{"cardLimitId":139,"cardId":123,"limitType":2,
                "amount":20000.0,"period":1,"isActive":true,
                "createdAt":"2026-05-19T11:32:16.581986","partnerId":2}],
                "allowAtm":true,"allowOnline":false,"allowInternational":false,
                "contactlessEnabled":true,"allowPos":true}
                """;
    }

    private TestContext context() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        return new TestContext(new ChariBaasClient(restTemplate, properties()), server);
    }

    private ChariBaasProperties properties() {
        ChariBaasProperties properties = new ChariBaasProperties();
        properties.setBaseUrl("https://sandbox.charimoney.com");
        properties.setApiKey("test-key");
        properties.getAudit().setEnabled(false);
        return properties;
    }

    private record TestContext(ChariBaasClient client, MockRestServiceServer server) {
    }
}
