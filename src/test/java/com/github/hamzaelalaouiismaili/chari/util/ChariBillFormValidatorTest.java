package com.github.hamzaelalaouiismaili.chari.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.hamzaelalaouiismaili.chari.model.bill.ChariBillFieldValue;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBillFormResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChariBillFormValidatorTest {

        private ChariBillFormResponse form() {
                ChariBillFormResponse.IdentificationField phone = new ChariBillFormResponse.IdentificationField(
                                "Numéro de téléphone", "ND", "text", List.of(), "2", 10, 10, "1");
                ChariBillFormResponse.IdentificationField amount = new ChariBillFormResponse.IdentificationField(
                                "Montant", "montant", "select",
                                List.of("10", "20", "30", "50", "100", "200", "300"), "1", 0, 10, "1");
                ChariBillFormResponse.IdentificationField hint = new ChariBillFormResponse.IdentificationField(
                                "Vérifiez votre facture", "", "libelle", List.of(), "1", 0, 0, "0");
                ChariBillFormResponse.IdentificationField optional = new ChariBillFormResponse.IdentificationField(
                                "Code confidentiel", "Fidelio", "text", List.of(), "1", 4, 4, "0");
                return new ChariBillFormResponse(
                                new ChariBillFormResponse.Form(List.of(phone, amount, hint, optional), 4));
        }

        @Test
        void acceptsValuesMatchingTheForm() {
                List<String> problems = ChariBillFormValidator.check(form(), List.of(
                                ChariBillFieldValue.of("ND", "0669440735"),
                                ChariBillFieldValue.of("montant", "10")));

                assertThat(problems).isEmpty();
        }

        @Test
        void reportsMissingRequiredFieldLengthAndSelectViolations() {
                List<String> problems = ChariBillFormValidator.check(form(), List.of(
                                ChariBillFieldValue.of("ND", "06694"),
                                ChariBillFieldValue.of("Fidelio", "12345")));

                assertThat(problems)
                                .anySatisfy(problem -> assertThat(problem).contains("montant").contains("required"))
                                .anySatisfy(problem -> assertThat(problem).contains("ND").contains("at least 10"))
                                .anySatisfy(problem -> assertThat(problem).contains("Fidelio").contains("at most 4"));
        }

        @Test
        void reportsValuesOutsideTheSelectListAndUnknownFields() {
                List<String> problems = ChariBillFormValidator.check(form(), List.of(
                                ChariBillFieldValue.of("ND", "0669440735"),
                                ChariBillFieldValue.of("montant", "15"),
                                ChariBillFieldValue.of("inconnu", "x")));

                assertThat(problems)
                                .anySatisfy(problem -> assertThat(problem).contains("Montant").contains("one of"))
                                .anySatisfy(problem -> assertThat(problem).contains("inconnu")
                                                .contains("not part of the identification form"));
        }

        @Test
        void validateThrowsWithEveryProblemListed() {
                assertThatThrownBy(() -> ChariBillFormValidator.validate(form(), List.of()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Numéro de téléphone")
                                .hasMessageContaining("Montant");
        }

        @Test
        void displayOnlyFieldsAreNeverRequired() {
                List<String> problems = ChariBillFormValidator.check(form(), List.of(
                                ChariBillFieldValue.of("ND", "0669440735"),
                                ChariBillFieldValue.of("montant", "20")));

                assertThat(problems).noneSatisfy(problem -> assertThat(problem).contains("Vérifiez"));
        }
}
