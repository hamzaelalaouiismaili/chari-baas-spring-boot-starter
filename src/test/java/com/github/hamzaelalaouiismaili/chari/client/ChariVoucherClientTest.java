package com.github.hamzaelalaouiismaili.chari.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariOperationType;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherCatalogQuery;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariVoucherPurchasePayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherArticlesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherBrandsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPreviewResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPurchaseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariVoucherClientTest {

    @Test
    void retrievesPaginatedArticlesAndMapsDecimalPrice() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/articles?phoneNumber=+212661231234&brandId=25&page=2&take=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "providerSkuId":"RAZER-10","productName":"Razer Gold",
                          "imageUrl":null,"price":10.50,"description":"Gaming credit",
                          "providerId":2,"brandId":25
                        }],"count":3}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response = context.client.getVoucherArticles(
                ChariVoucherCatalogQuery.builder()
                        .phoneNumber("0661231234")
                        .brandId(25)
                        .page(2)
                        .take(5)
                        .build());

        assertThat(response.getData().getCount()).isEqualTo(3);
        assertThat(response.getData().getCollection().getFirst().getProviderSkuId()).isEqualTo("RAZER-10");
        assertThat(response.getData().getCollection().getFirst().getPrice()).isEqualByComparingTo("10.50");
        context.server.verify();
    }

    @Test
    void retrievesBrandsWithDefaultPagination() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/brands?phoneNumber=+212661231234&brandId=25&page=1&take=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "id":14,"name":"Razer","description":"Usage instructions",
                          "image":"https://example.com/razer.png","expirationDelay":"none"
                        }],"count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherBrandsResponse response = context.client.getVoucherBrands("0661231234", 25);

        assertThat(response.getData().getCollection().getFirst().getName()).isEqualTo("Razer");
        assertThat(response.getData().getCount()).isEqualTo(1);
        context.server.verify();
    }

    @Test
    void retrievesBrandAndVouchersByBrand() {
        TestContext context = context();
        String brandJson = """
                {"data":{"id":14,"name":"Razer","description":"Usage instructions",
                "image":null,"expirationDelay":"none"}}
                """;
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/brands/14?phoneNumber=+212661231234"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(brandJson, MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/14/articles?phoneNumber=+212661231234"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(brandJson, MediaType.APPLICATION_JSON));

        ChariVoucherBrandResponse brand = context.client.getVoucherBrand(14, "0661231234");
        ChariVoucherBrandResponse vouchers = context.client.getVouchersByBrand(14, "0661231234");

        assertThat(brand.getData().getId()).isEqualTo(14);
        assertThat(vouchers.getData().getExpirationDelay()).isEqualTo("none");
        context.server.verify();
    }

    @Test
    void previewsAndConfirmsSameNormalizedPurchasePayload() {
        TestContext context = context();
        String expectedBody = """
                {
                  "customerPhoneNumber":"+212661231234",
                  "destinationPhoneNumber":"+212662345678",
                  "beneficiaryName":"Abdennour",
                  "providerSkuId":"1212AAABBBccc",
                  "providerId":2
                }
                """;
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/voucher/preview"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedBody))
                .andRespond(withSuccess("""
                        {"data":{"type":23,"operation":{
                          "customerPhoneNumber":"+212661231234","amount":2.16,"reason":"",
                          "beneficiaryId":null,"recipientPhoneNumber":"+212662345678"
                        },"feesAmount":0.15,"totalAmount":2.16,
                        "checkedAt":"2026-03-31T14:52:07","openLoop":false}}
                        """, MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/voucher/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedBody))
                .andRespond(withSuccess("""
                        {"data":{"type":23,"operation":{
                          "operationType":23,"voucherName":"Razer Gold","amount":2.16,
                          "cashBack":0.14,"totalAmount":1.96,"reason":null,
                          "recipientPhoneNumber":"+212662345678",
                          "checkedAt":"2026-03-31T14:53:50.6078466Z","urlActivateCard":null,
                          "destinationPhoneNumber":"+212662345678","beneficiaryName":"Abdennour",
                          "code":"01X01X01X","description":"Gaming credit"
                        },"feesAmount":0.15,"totalAmount":2.16,
                        "checkedAt":"2026-03-31T14:52:07","openLoop":false}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherPurchasePayload payload = purchasePayload();
        ChariVoucherPreviewResponse preview = context.client.previewVoucherPurchase(payload);
        ChariVoucherPurchaseResponse purchase = context.client.confirmVoucherPurchase(payload);

        assertThat(preview.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(preview.getData().getFeesAmount()).isEqualByComparingTo("0.15");
        assertThat(purchase.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(purchase.getData().getOperation().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(purchase.getData().getOperation().getCode()).isEqualTo("01X01X01X");
        assertThat(purchase.getData().getOperation().getCashBack()).isEqualByComparingTo("0.14");
        context.server.verify();
    }

    @Test
    void rejectsInvalidCatalogAndPurchaseInputsLocally() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

        assertThatThrownBy(() -> client.getVoucherArticles("0661231234", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher brand ID must be positive");

        ChariVoucherPurchasePayload payload = purchasePayload();
        payload.setProviderSkuId(" ");
        assertThatThrownBy(() -> client.previewVoucherPurchase(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher provider SKU ID is required");
    }

    private ChariVoucherPurchasePayload purchasePayload() {
        return ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber("0661231234")
                .destinationPhoneNumber("0662345678")
                .beneficiaryName(" Abdennour ")
                .providerSkuId(" 1212AAABBBccc ")
                .providerId(2)
                .build();
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
