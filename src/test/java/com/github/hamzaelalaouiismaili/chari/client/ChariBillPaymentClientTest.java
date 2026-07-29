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
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillReceivablesResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillUnpaidItemsResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
                                                {"codeRetour":"000","msg":"ACCEPTE","nbreCreancier":1,
                                                 "listeCreanciers":[{"codeCreancier":"1002","nomCreancier":"RADEEMA",
                                                 "descriptionCreancier":"Régie autonome de Marrakech","logoPath":null,
                                                 "siteWeb":"https://www.radeema.ma","params":null}]}
                                                """, MediaType.APPLICATION_JSON));
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/creances?creancierId=1002"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                """
                                                                {"codeRetour":"000","msg":"ACCEPTE","nbreCreance":2,
                                                                 "listeCreance":[{"codeCreance":"01","nomCreance":"Factures Eau et Electricité"},
                                                                 {"codeCreance":"02","nomCreance":"Frais de raccordement"}]}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillCreditorsResponse creditors = context.client.getBillCreditors();
                ChariBillReceivablesResponse receivables = context.client.getBillReceivables("1002");

                assertThat(creditors.isSuccessful()).isTrue();
                assertThat(creditors.getListeCreanciers().getFirst().getNomCreancier()).isEqualTo("RADEEMA");
                assertThat(receivables.getNbreCreance()).isEqualTo(2);
                context.server.verify();
        }

        @Test
        void mapsDynamicIdentificationFormWithoutHardcodedFields() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/form?creancierId=1008&creanceId=01"))
                                .andExpect(method(HttpMethod.GET))
                                .andRespond(withSuccess(
                                                """
                                                                {"codeRetour":"000","msg":"ACCEPTE","nbreParams":2,
                                                                 "creancierParams":[
                                                                   {"libelle":"Contract number","nomChamp":"numeroProduit","typeChamp":"text",
                                                                    "formatChamp":"2","tailleMin":8,"tailleMax":12,"contrainte":"1"},
                                                                   {"libelle":"Check your bill","nomChamp":"","typeChamp":"libelle",
                                                                    "formatChamp":"1","tailleMin":0,"tailleMax":0,"contrainte":"0"}
                                                                 ],"refTxFatourati":"1"}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillFormResponse response = context.client.getBillIdentificationForm("1008", "01");

                var input = response.getCreancierParams().get(0);
                var label = response.getCreancierParams().get(1);
                assertThat(input.getTypedFieldType()).isEqualTo(ChariBillFormFieldType.TEXT);
                assertThat(input.isRequired()).isTrue();
                assertThat(input.shouldSubmit()).isTrue();
                assertThat(label.getTypedFieldType()).isEqualTo(ChariBillFormFieldType.LABEL);
                assertThat(label.shouldSubmit()).isFalse();
                context.server.verify();
        }

        @Test
        void retrievesUnpaidItemsAndHidesTechnicalGlobalParameters() {
                TestContext context = context();
                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/impayes?creancierId=1008&creanceId=01"))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(content().json("""
                                                {"CreancierVals":[{"libelle":null,"nomChamp":"numeroProduit",
                                                "valeurChamp":"16422270229"}]}
                                                """))
                                .andRespond(withSuccess(
                                                """
                                                                {"codeRetour":"000","msg":"ACCEPTE","refTxFatourati":"100003141347",
                                                                 "codeDevise":"504","nbreCreances":2,"montantTotalTTC":"150.50",
                                                                 "typeFrais":"commission","valeurFrais":"100","seuilMinimal":"3.00",
                                                                 "globalParams":[
                                                                   {"libelle":"Customer name","nomChamp":"nomClient","valeurChamp":"ALERGE"},
                                                                   {"libelle":"","nomChamp":"contrPaiement","valeurChamp":"1"}],
                                                                 "impayesParams":[
                                                                   {"idArticle":"1005533319","description":"Water bill July 2026",
                                                                    "dateFacture":"26/07/2026","prixTTC":"120.16","typeArticle":0},
                                                                   {"idArticle":"1005533320","description":"Water bill August 2026",
                                                                    "dateFacture":"26/08/2026","prixTTC":"30.34","typeArticle":0}]}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillUnpaidItemsResponse response = context.client.getBillUnpaidItems(
                                "1008", "01", unpaidPayload());

                assertThat(response.getMontantTotalTTC()).isEqualByComparingTo("150.50");
                assertThat(response.getImpayesParams()).hasSize(2);
                assertThat(response.getImpayesParams().getFirst().getTypedArticleType())
                                .isEqualTo(ChariBillArticleType.RECEIVABLE);
                assertThat(response.getDisplayableGlobalParams()).extracting("nomChamp")
                                .containsExactly("nomClient");
                context.server.verify();
        }

        @Test
        void confirmsOnlySelectedArticlesAndMapsReceipt() {
                TestContext context = context();
                ChariBillArticle selected = ChariBillArticle.builder()
                                .idArticle("1005533319")
                                .description("Water bill July 2026")
                                .dateFacture("26/07/2026")
                                .prixTTC(new BigDecimal("120.16"))
                                .typeArticle(0)
                                .build();
                ChariBillPaymentPayload payload = ChariBillPaymentPayload.builder()
                                .creancierId("1008")
                                .creanceId("01")
                                .refTxFatourati("100003141347")
                                .creditorValues(List.of(ChariBillFieldValue.builder()
                                                .libelle("Contract number")
                                                .nomChamp("numeroProduit")
                                                .valeurChamp("16422270229")
                                                .build()))
                                .selectedArticles(List.of(selected))
                                .build();

                context.server.expect(once(), requestTo(
                                "https://sandbox.charimoney.com/api/bills/confirm?phoneNumber=+212670770743"))
                                .andExpect(method(HttpMethod.POST))
                                .andExpect(content()
                                                .json("""
                                                                {"creancierId":"1008","creanceId":"01","refTxFatourati":"100003141347",
                                                                 "CreancierVals":[{"libelle":"Contract number","nomChamp":"numeroProduit",
                                                                 "valeurChamp":"16422270229"}],
                                                                 "ListeArticleSelectionnes":[{"idArticle":"1005533319",
                                                                 "description":"Water bill July 2026","dateFacture":"26/07/2026",
                                                                 "prixTTC":120.16,"typeArticle":0,"extraArticleParams":null}]}
                                                                """))
                                .andRespond(withSuccess(
                                                """
                                                                {"codeRetour":"000","msg":"CONFIRME","refTxFatourati":"100003141347",
                                                                 "refReglement":"REG-2026-123","codeDevise":"504","montantTotalTTC":"120.16",
                                                                 "params":[{"libelle":"CRC","nomChamp":"numCRC","valeurChamp":"123456"}]}
                                                                """,
                                                MediaType.APPLICATION_JSON));

                ChariBillPaymentResponse response = context.client.confirmBillPayment("0670770743", payload);

                assertThat(response.isReceiptAvailable()).isTrue();
                assertThat(response.isAwaitingWebhookResolution()).isFalse();
                assertThat(response.getRefReglement()).isEqualTo("REG-2026-123");
                context.server.verify();
        }

        @Test
        void exposesAlreadyProcessedAndAsynchronousBusinessOutcomes() {
                ChariBillPaymentResponse alreadyProcessed = new ChariBillPaymentResponse();
                alreadyProcessed.setCodeRetour("301");
                ChariBillPaymentResponse asynchronous = new ChariBillPaymentResponse();
                asynchronous.setCodeRetour("909");

                assertThat(alreadyProcessed.isAlreadyProcessed()).isTrue();
                assertThat(alreadyProcessed.isReceiptAvailable()).isTrue();
                assertThat(asynchronous.isAwaitingWebhookResolution()).isTrue();
                assertThat(asynchronous.isTechnicalError()).isTrue();
        }

        @Test
        void rejectsCrossCreditorOrMalformedConfirmationInputsLocally() {
                ChariBaasClient client = new ChariBaasClient(new RestTemplate(), properties());
                ChariBillPaymentPayload payload = ChariBillPaymentPayload.builder()
                                .creancierId("1008")
                                .creanceId("1")
                                .refTxFatourati("100003141347")
                                .creditorValues(List.of())
                                .selectedArticles(List.of())
                                .build();

                assertThatThrownBy(() -> client.confirmBillPayment("0670770743", payload))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Fatourati receivable ID must contain 2 digits");
        }

        private ChariBillUnpaidItemsPayload unpaidPayload() {
                return ChariBillUnpaidItemsPayload.builder()
                                .creditorValues(List.of(ChariBillFieldValue.builder()
                                                .nomChamp("numeroProduit")
                                                .valeurChamp("16422270229")
                                                .build()))
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
