package com.github.hamzaelalaouiismaili.chari.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariBillArticleType;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariBillFormFieldType;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillArticle;
import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillPaymentPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariBillUnpaidItemsPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillCreditorsResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillFormResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillPaymentResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceiptResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceivablesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillUnpaidItemsResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariBillPaymentClientTest {

        @Test
        void listsCreditorsAndReceivables() {
                TestContext context = context();
                context.server.expect(once(), requestTo("https://sandbox.charimoney.com/api/bills/creanciers"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess("""
                                                {"data":[{"categoryName":"Eau et électricité",
                                                 "categoryDescription":"Eau et électricité","categoryCode":"102","order":2,
                                                 "listeCreanciersListe":[{"codeCreancier":"1002","nomCreancier":"RADEEMA",
                                                 "descriptionCreancier":"Régie autonome de Marrakech","logoPath":null,
                                                 "listeCreances":[{"codeCreance":"01","nomCreance":"factures radeema"}],
                                                 "params":[{"nomChamp":"categorieCode","valChamp":"102"}],
                                                 "siteWeb":"https://www.radeema.ma"}]}]}
                                                """, MediaType.APPLICATION_JSON));
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/creances?creancierId=1002"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"collection":[
                                                                 {"codeCreance":"01","nomCreance":"Factures Eau et Electricité"},
                                                                 {"codeCreance":"02","nomCreance":"Frais de raccordement"}],
                                                                 "count":2}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillCreditorsResponse creditors = context.client.getBillCreditors();
                ChariBillReceivablesResponse receivables = context.client.getBillReceivables("1002");

                assertThat(creditors.getCategories()).hasSize(1);
                assertThat(creditors.getCategories().getFirst().getCategoryCode()).isEqualTo("102");
                ChariBillCreditorsResponse.Creditor creditor = creditors.findCreditor("1002");
                assertThat(creditor.getNomCreancier()).isEqualTo("RADEEMA");
                assertThat(creditor.getReceivables().getFirst().getCodeCreance()).isEqualTo("01");
                assertThat(creditor.getParams().getFirst().getValeurChamp()).isEqualTo("102");
                assertThat(receivables.getCount()).isEqualTo(2);
                assertThat(receivables.getReceivables()).hasSize(2);
                assertThat(receivables.findReceivable("02").getNomCreance())
                                .isEqualTo("Frais de raccordement");
                context.server.verify();
        }

        @Test
        void mapsDynamicIdentificationFormWithoutHardcodedFields() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/form?creancierId=1001&creanceId=01"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"collection":[
                                                                  {"contrainte":"1","formatChamp":"2","libelle":"Numéro de téléphone",
                                                                   "listVals":[],"nomChamp":"ND","tailleMax":10,"tailleMin":10,
                                                                   "typeChamp":"text"},
                                                                  {"contrainte":"1","formatChamp":"1","libelle":"Montant",
                                                                   "listVals":["10","20","30","50","100","200","300"],
                                                                   "nomChamp":"montant","tailleMax":10,"tailleMin":0,
                                                                   "typeChamp":"select"},
                                                                  {"contrainte":"0","formatChamp":"1","libelle":"Check your bill",
                                                                   "listVals":[],"nomChamp":"","tailleMax":0,"tailleMin":0,
                                                                   "typeChamp":"libelle"}],
                                                                 "count":3}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillFormResponse response = context.client.getBillIdentificationForm("1001", "01");

                assertThat(response.getFields()).hasSize(3);
                var input = response.getFields().get(0);
                var select = response.getFields().get(1);
                var label = response.getFields().get(2);
                assertThat(input.getTypedFieldType()).isEqualTo(ChariBillFormFieldType.TEXT);
                assertThat(input.isRequired()).isTrue();
                assertThat(input.shouldSubmit()).isTrue();
                assertThat(select.isSelect()).isTrue();
                assertThat(select.getAllowedValues()).contains("10", "300");
                assertThat(label.getTypedFieldType()).isEqualTo(ChariBillFormFieldType.LABEL);
                assertThat(label.shouldSubmit()).isFalse();
                assertThat(response.findField("montant")).isSameAs(select);
                context.server.verify();
        }

        @Test
        void retrievesUnpaidItemsAndHidesTechnicalGlobalParameters() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/impayes?creancierId=1001&creanceId=01&phoneNumber=+212669440735"))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(content().json("""
                                                {"Alias":"Hamz","AddToFavorites":true,
                                                 "CreancierVals":[{"nomChamp":"ND","valChamp":"0669440735","valeurChamp":"0669440735"},
                                                 {"nomChamp":"montant","valChamp":"10","valeurChamp":"10"}]}
                                                """))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"refTxFatourati":"100003754540","nbreCreances":2,
                                                                 "montantTotalTTCField":"150.50","typeFrais":null,"valeurFrais":null,
                                                                 "seuilMinimal":null,
                                                                 "globalParams":[
                                                                   {"libelle":"Numéro de ligne à recharger","nomChamp":"ND","valeurChamp":"0669440735"},
                                                                   {"libelle":"","nomChamp":"contrPaiement","valeurChamp":"3"}],
                                                                 "impayesParams":[
                                                                   {"idArticle":"1","description":"RECHARGE NORMALE",
                                                                    "dateFacture":null,"prixTTC":"120.16","typeArticle":0},
                                                                   {"idArticle":"2","description":"Pass SMS *1",
                                                                    "dateFacture":null,"prixTTC":"30.34","typeArticle":0}]}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillUnpaidItemsPayload payload = ChariBillUnpaidItemsPayload.builder()
                                .creditorValues(List.of(
                                                ChariBillFieldValue.of("ND", "0669440735"),
                                                ChariBillFieldValue.of("montant", "10")))
                                .alias("Hamz")
                                .addToFavorites(true)
                                .build();

                ChariBillUnpaidItemsResponse response = context.client.getBillUnpaidItems(
                                "1001", "01", payload, "0669440735");

                assertThat(response.getRefTxFatourati()).isEqualTo("100003754540");
                assertThat(response.getMontantTotalTTC()).isEqualByComparingTo("150.50");
                assertThat(response.getArticles()).hasSize(2);
                assertThat(response.findArticle("2").getDescription()).isEqualTo("Pass SMS *1");
                assertThat(response.getArticles().getFirst().getTypedArticleType())
                                .isEqualTo(ChariBillArticleType.RECEIVABLE);
                assertThat(response.getDisplayableGlobalParams()).extracting("nomChamp")
                                .containsExactly("ND");
                context.server.verify();
        }

        @Test
        void retrievesUnpaidItemsByQrCodeWithoutFormValues() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/impayes?creancierId=9999&creanceId=01&phoneNumber=+212670770743"))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(content().json("""
                                                {"qrcodecontent":"ZI3qynFphbr00HgLL164D"}
                                                """, JsonCompareMode.STRICT))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"refTxFatourati":"100003282286","nbreCreances":1,
                                                                 "montantTotalTTCField":"168.00","globalParams":[],
                                                                 "impayesParams":[{"idArticle":"0001198391052009",
                                                                  "description":"0001198391052009","dateFacture":"01/04/2009",
                                                                  "prixTTC":"168.00","typeArticle":0}]}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillUnpaidItemsResponse response = context.client.getBillUnpaidItemsByQrCode(
                                "9999", "01", "ZI3qynFphbr00HgLL164D", "0670770743");

                assertThat(response.getArticles()).hasSize(1);
                assertThat(response.getMontantTotalTTC()).isEqualByComparingTo("168.00");
                context.server.verify();
        }

        @Test
        void confirmsOnlySelectedArticlesAndMapsOperationReceipt() {
                TestContext context = context();
                ChariBillUnpaidItemsResponse unpaidItems = new ChariBillUnpaidItemsResponse(
                                new ChariBillUnpaidItemsResponse.UnpaidItems(
                                                "100003754540", 1, new BigDecimal("20"), null, null, null, null,
                                                List.of(
                                                                new ChariBillFieldValue("Numéro de ligne à recharger", "ND",
                                                                                "0669440735"),
                                                                new ChariBillFieldValue("", "contrPaiement", "3")),
                                                List.of(ChariBillArticle.builder()
                                                                .idArticle("0:0")
                                                                .description("Recharge classique")
                                                                .prixTTC(new BigDecimal("20"))
                                                                .typeArticle(0)
                                                                .build())));
                ChariBillPaymentPayload payload = ChariBillPaymentPayload.fromUnpaidItems(
                                "1003", "01", unpaidItems, unpaidItems.getArticles());

                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/confirm?phoneNumber=+212669440735"))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(content()
                                                .json("""
                                                                {"creancierId":"1003","creanceId":"01","refTxFatourati":"100003754540",
                                                                 "globalParams":[
                                                                  {"libelle":"Numéro de ligne à recharger","nomChamp":"ND","valeurChamp":"0669440735"},
                                                                  {"libelle":"","nomChamp":"contrPaiement","valeurChamp":"3"}],
                                                                 "ListeArticleSelectionnes":[{"idArticle":"0:0",
                                                                 "description":"Recharge classique","prixTTC":"20","typeArticle":0}]}
                                                                """))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"operationType":25,"operationId":14326,"amount":20,
                                                                 "feesAmount":1,"totalAmount":20,"reason":null,
                                                                 "checkedAt":"2026-07-29T19:35:31.4244498Z",
                                                                 "creditor":"Orange Recharges et Catalogue Pass",
                                                                 "debt":"Orange recharge Sim","categoryCode":"101",
                                                                 "category":"Téléphone et Internet","authorizationCode":"d55994",
                                                                 "fatouratiErrorCode":"000"}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillPaymentResponse response = context.client.confirmBillPayment("0669440735", payload);

                assertThat(response.isSuccessful()).isTrue();
                assertThat(response.isReceiptAvailable()).isTrue();
                assertThat(response.isAwaitingWebhookResolution()).isFalse();
                assertThat(response.getOperationId()).isEqualTo(14326L);
                assertThat(response.getAuthorizationCode()).isEqualTo("d55994");
                assertThat(response.getData().getFeesAmount()).isEqualByComparingTo("1");
                assertThat(response.getData().getCreditor())
                                .isEqualTo("Orange Recharges et Catalogue Pass");
                context.server.verify();
        }

        @Test
        void downloadsBillReceiptByOperationId() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/bill-receipt/14326?phoneNumber=+212669440735"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                """
                                                                {"data":{"refReglement":"XH4195","montantTotalTTC":"20.00"}}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillReceiptResponse receipt = context.client.getBillReceipt(14326, "0669440735");

                assertThat(receipt.getString("refReglement")).isEqualTo("XH4195");
                assertThat(receipt.getFields()).containsKey("montantTotalTTC");
                context.server.verify();
        }

        @Test
        void exposesAlreadyProcessedAndAsynchronousBusinessOutcomes() {
                ChariBillPaymentResponse alreadyProcessed = new ChariBillPaymentResponse();
                alreadyProcessed.setData(new ChariBillPaymentResponse.Confirmation());
                alreadyProcessed.getData().setFatouratiErrorCode("301");
                ChariBillPaymentResponse asynchronous = new ChariBillPaymentResponse();
                asynchronous.setData(new ChariBillPaymentResponse.Confirmation());
                asynchronous.getData().setFatouratiErrorCode("909");

                assertThat(alreadyProcessed.isAlreadyProcessed()).isTrue();
                assertThat(alreadyProcessed.isReceiptAvailable()).isTrue();
                assertThat(asynchronous.isAwaitingWebhookResolution()).isTrue();
                assertThat(asynchronous.isSuccessful()).isFalse();
        }

        @Test
        void rejectsCrossCreditorOrMalformedConfirmationInputsLocally() {
                ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());
                ChariBillPaymentPayload payload = ChariBillPaymentPayload.builder()
                                .creancierId("1008")
                                .creanceId("1")
                                .refTxFatourati("100003141347")
                                .globalParams(List.of())
                                .selectedArticles(List.of())
                                .build();

                assertThatThrownBy(() -> client.confirmBillPayment("0670770743", payload))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Fatourati receivable ID must contain 2 digits");
        }

        @Test
        void rejectsUnpaidItemsLookupWithoutValuesOrQrCode() {
                ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());

                assertThatThrownBy(() -> client.getBillUnpaidItems("1001", "01",
                                ChariBillUnpaidItemsPayload.builder().build()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("QR code");
                assertThatThrownBy(() -> client.getBillUnpaidItems("1001", "01",
                                ChariBillUnpaidItemsPayload.builder()
                                                .creditorValues(List.of(ChariBillFieldValue.of("ND", "0669440735")))
                                                .addToFavorites(true)
                                                .build()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("alias");
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
