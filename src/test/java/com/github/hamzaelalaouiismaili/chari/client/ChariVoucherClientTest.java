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
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherProductResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariVoucherPurchaseResponse;
import java.math.BigDecimal;
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
                  "amount":0,
                  "skuId":18,
                  "providerSkuId":null,
                  "price":512.76,
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
                        {"data":{
                          "operationType":23,"voucherName":"USD 50","amount":564.04,
                          "cashBack":36.66,"totalAmount":512.76,"reason":null,
                          "recipientPhoneNumber":"+212662345678",
                          "checkedAt":"2026-07-30T15:05:59.8632851Z","urlActivateCard":null,
                          "destinationPhoneNumber":"+212662345678","beneficiaryName":"Abdennour",
                          "code":"0qGNUOQWAWHVT1","description":"Gaming credit"
                        }}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherPurchasePayload payload = purchasePayload();
        ChariVoucherPreviewResponse preview = context.client.previewVoucherPurchase(payload);
        ChariVoucherPurchaseResponse purchase = context.client.confirmVoucherPurchase(payload);

        assertThat(preview.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(preview.getData().getFeesAmount()).isEqualByComparingTo("0.15");
        assertThat(purchase.getData().getTypedOperationType()).isEqualTo(ChariOperationType.VOUCHER);
        assertThat(purchase.getData().getCode()).isEqualTo("0qGNUOQWAWHVT1");
        assertThat(purchase.getData().getCashBack()).isEqualByComparingTo("36.66");
        context.server.verify();
    }

    @Test
    void rejectsInvalidCatalogAndPurchaseInputsLocally() {
        ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

        assertThatThrownBy(() -> client.getVoucherArticles("0661231234", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher brand ID must be positive");

        ChariVoucherPurchasePayload payload = purchasePayload();
        payload.setSkuId(0);
        assertThatThrownBy(() -> client.previewVoucherPurchase(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Voucher SKU ID must be positive");
    }

    @Test
    void parsesSkuIdAndPricingOnArticles() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/articles?phoneNumber=+212661231234&brandId=25&page=1&take=10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":15,"providerSkuId":"NPAP","productName":"1800 Flowers",
                          "imageUrl":null,"price":150,"description":"","providerId":3,"brandId":-1,
                          "pricing":{"maxValueAmount":5000,"baseValueAmount":150,
                                     "isVariableValue":true,"multiplicateur":0}
                        }],"count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response =
                context.client.getVoucherArticles("0661231234", 25);
        var article = response.getData().getCollection().getFirst();

        assertThat(article.getSkuId()).isEqualTo(15);
        assertThat(article.getPricing().getMaxValueAmount()).isEqualByComparingTo("5000");
        assertThat(article.getPricing().getIsVariableValue()).isTrue();
        assertThat(article.getPricing().getMultiplicateur()).isEqualTo(0);
    }

    @Test
    void appendsKeywordAfterPagingInCatalogUrl() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/articles?phoneNumber=+212661231234&brandId=25&page=1&take=10&keyword=razer"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"data\":{\"collection\":[],\"count\":0}}",
                        MediaType.APPLICATION_JSON));

        context.client.getVoucherArticles(ChariVoucherCatalogQuery.builder()
                .phoneNumber("0661231234")
                .brandId(25)
                .page(1)
                .take(10)
                .keyword("razer")
                .build());
        context.server.verify();
    }

    @Test
    void fetchesProductListWithPaging() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/product?page=1&take=2"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":0,"providerSkuId":"NPAP","productName":"1800 Flowers",
                          "imageUrl":"https://img","price":150,"description":"","providerId":3,
                          "brandId":-1,"pricing":{"maxValueAmount":5000,"baseValueAmount":150,
                          "isVariableValue":true,"multiplicateur":0}}],"count":168}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response = context.client.getVoucherProducts(1, 2);

        assertThat(response.getData().getCount()).isEqualTo(168);
        assertThat(response.getData().getCollection().getFirst().getProviderSkuId()).isEqualTo("NPAP");
        context.server.verify();
    }

    @Test
    void fetchesProductDetailByConfigId() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers/products/NPAP123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{
                          "capProductId":29,"capDefaultUpc":"07675021141","name":"1800 Flowers",
                          "blackhawkId":"https://api/product/C6S7","priceUsd":15,
                          "priceMadCurrent":135.025,"productConfigId":"NPAP123",
                          "productDescription":"Flowers and gifts",
                          "productImages":[{"id":"Q717","imageSize":"SMALL",
                            "frontImage":"https://img/icon.png"}],
                          "activationCharacteristics":{"maxValueAmount":500,
                            "baseValueAmount":15,"isVariableValue":true},
                          "redemptionCharacteristics":{"redemptionOptions":["IN_STORE","ON_LINE"]},
                          "termsAndConditions":[{"id":"816B","termsAndConditions":"Terms text",
                            "termsAndConditionsType":"WEB"}]
                        }}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherProductResponse response = context.client.getVoucherProductDetail("NPAP123");
        var data = response.getData();

        assertThat(data.getCapProductId()).isEqualTo(29);
        assertThat(data.getPriceMadCurrent()).isEqualByComparingTo("135.025");
        assertThat(data.getProductImages().getFirst().getImageSize()).isEqualTo("SMALL");
        assertThat(data.getActivationCharacteristics().getIsVariableValue()).isTrue();
        assertThat(data.getRedemptionCharacteristics().getRedemptionOptions()).containsExactly("IN_STORE", "ON_LINE");
        assertThat(data.getTermsAndConditions().getFirst().getTermsAndConditionsType()).isEqualTo("WEB");
        context.server.verify();
    }

    @Test
    void fetchesLocalVouchersWithoutBrandId() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/vouchers?phoneNumber=+212661231234&keyword=pubg"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"data":{"collection":[{
                          "skuId":15,"providerSkuId":"15","productName":"PUBG UC","imageUrl":null,
                          "price":100,"description":"","providerId":1,"brandId":44,"pricing":null}],
                          "count":1}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherArticlesResponse response = context.client.getLocalVouchers(
                ChariVoucherCatalogQuery.builder()
                        .phoneNumber("0661231234")
                        .keyword("pubg")
                        .build());

        assertThat(response.getData().getCollection().getFirst().getSkuId()).isEqualTo(15);
        context.server.verify();
    }

    @Test
    void rejectsLocalVouchersWithoutPhone() {
        assertThatThrownBy(() -> new ChariBaasClient(new RestTemplate(), properties())
                .getLocalVouchers(ChariVoucherCatalogQuery.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A valid Moroccan mobile phone number is required");
    }

    @Test
    void previewsAndPurchasesServiceVoucher() {
        TestContext context = context();
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/service/voucher/preview"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"data":{"type":23,"operation":{
                          "customerPhoneNumber":"+212661231234","amount":2.16,"reason":"",
                          "beneficiaryId":null,"recipientPhoneNumber":"+212662345678"
                        },"feesAmount":0.15,"totalAmount":2.16,
                        "checkedAt":"2026-03-31T14:52:07","openLoop":false}}
                        """, MediaType.APPLICATION_JSON));
        context.server.expect(once(), requestTo(
                        "https://sandbox.charimoney.com/api/operations/service/voucher"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"data":{"operationType":23,"voucherName":"USD 50","amount":564.04,
                          "cashBack":36.66,"totalAmount":512.76,"reason":null,
                          "recipientPhoneNumber":"+212662345678","checkedAt":"2026-07-30T15:05:59Z",
                          "urlActivateCard":null,"destinationPhoneNumber":"+212662345678",
                          "beneficiaryName":"Abdennour","code":"0qGNUOQWAWHVT1","description":"x"}}
                        """, MediaType.APPLICATION_JSON));

        ChariVoucherPurchasePayload payload = purchasePayload();
        ChariVoucherPreviewResponse preview = context.client.previewServiceVoucherPurchase(payload);
        ChariVoucherPurchaseResponse purchase = context.client.purchaseServiceVoucher(payload);

        assertThat(preview.getData().getTotalAmount()).isEqualByComparingTo("2.16");
        assertThat(purchase.getData().getCode()).isEqualTo("0qGNUOQWAWHVT1");
        context.server.verify();
    }

    private ChariVoucherPurchasePayload purchasePayload() {
        return ChariVoucherPurchasePayload.builder()
                .customerPhoneNumber("0661231234")
                .destinationPhoneNumber("0662345678")
                .beneficiaryName(" Abdennour ")
                .amount(new BigDecimal("0"))
                .skuId(18)
                .price(new BigDecimal("512.76"))
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
