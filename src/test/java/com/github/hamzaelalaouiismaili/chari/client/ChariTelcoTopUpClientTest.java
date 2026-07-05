package com.github.hamzaelalaouiismaili.chari.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoOperator;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariTelcoRechargeType;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTelcoRechargePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoCatalogResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariTelcoRechargeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariTelcoTopUpClientTest {

    @Test
    void returnsSupportedOperatorsWithoutAnApiCall() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

        assertThat(client.getSupportedTelcoOperators()).containsExactly(
                ChariTelcoOperator.MAROC_TELECOM,
                ChariTelcoOperator.ORANGE,
                ChariTelcoOperator.INWI);
        assertThat(ChariTelcoOperator.fromCode(1)).isEqualTo(ChariTelcoOperator.MAROC_TELECOM);
        assertThat(ChariTelcoOperator.ORANGE.getCode()).isEqualTo(2);
    }

    @Test
    void retrievesCatalogWithNormalizedPhoneAndNumericOperatorCode() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

        server.expect(once(), requestTo("https://sandbox.charimoney.com/api/services/telco/catalog/b2b"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Chari-Api-Key", "test-key"))
                .andExpect(content().json("""
                        {
                          "recipientPhoneNumber": "+212661231234",
                          "amount": 10,
                          "operator": 2
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "data": [
                            {"productCode":3,"description":"Pass Internet sur mobile","arDescription":"عرض الإنترنت","enabled":true},
                            {"productCode":5,"description":"Pass TikTok","arDescription":"عرض تطبيق تيكتوك","enabled":false}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        ChariTelcoCatalogResponse response = client.getTelcoCatalog("0661231234", 10,
                ChariTelcoOperator.ORANGE);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getEnabledProducts()).extracting("productCode").containsExactly(3);
        assertThat(response.findProduct(3)).isPresent();
        assertThat(response.findProduct(99)).isEmpty();
        server.verify();
    }

    @Test
    void executesProductRechargeAndMapsUppercaseAmount() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        ChariBaasClient client = new ChariBaasClient(restTemplate, properties());

        server.expect(once(), requestTo("https://sandbox.charimoney.com/api/services/telco/recharge/b2b"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "recipientPhoneNumber": "+212661231234",
                          "amount": 10,
                          "operator": 2,
                          "rechargeType": 1,
                          "productCode": 3,
                          "code": "12003"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "data": {
                            "operationType": 10,
                            "Amount": 10,
                            "feesAmount": 0,
                            "checkedAt": "2025-04-12T12:31:59.31347Z",
                            "openLoop": false
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        ChariTelcoRechargeResponse response = client.rechargeTelco(
                "0661231234",
                10,
                ChariTelcoOperator.ORANGE,
                ChariTelcoRechargeType.PRODUCT,
                3,
                "12003");

        assertThat(response.getData().getAmount()).isEqualByComparingTo("10");
        assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("0");
        assertThat(response.getData().getTypedOperationType()).isEqualTo(ChariOperationType.RECHARGE);
        assertThat(response.getData().getOpenLoop()).isFalse();
        server.verify();
    }

    @Test
    void rejectsInvalidTopUpBeforeMakingApiCall() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());
        ChariTelcoRechargePayload payload = ChariTelcoRechargePayload.builder()
                .recipientPhoneNumber("123")
                .amount(0)
                .operator(ChariTelcoOperator.INWI)
                .rechargeType(ChariTelcoRechargeType.PRODUCT)
                .productCode(3)
                .code("12003")
                .build();

        assertThatThrownBy(() -> client.rechargeTelco(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A valid Moroccan mobile phone number is required");
    }

    private ChariBaasProperties properties() {
        ChariBaasProperties properties = new ChariBaasProperties();
        properties.setBaseUrl("https://sandbox.charimoney.com");
        properties.setApiKey("test-key");
        properties.getAudit().setEnabled(false);
        return properties;
    }
}
