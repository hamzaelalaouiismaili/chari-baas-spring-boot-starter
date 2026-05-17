package com.github.hamzaelalaouiismaili.chari.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookController;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariBaasAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ChariBaasAutoConfiguration.class,
                    ChariBaasWebhookAutoConfiguration.class))
            .withPropertyValues(
                    "chari.baas.base-url=https://sandbox.charimoney.com",
                    "chari.baas.api-key=test-key");

    @Test
    void registersClientAndWebhookBeansByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ChariBaasProperties.class);
            assertThat(context).hasSingleBean(ChariBaasClient.class);
            assertThat(context).hasSingleBean(ChariWebhookDispatcher.class);
            assertThat(context).hasSingleBean(ChariWebhookController.class);
            assertThat(context).hasBean("chariBaasRestTemplate");
            assertThat(context.getBean("chariBaasRestTemplate", RestTemplate.class)).isNotNull();
        });
    }

    @Test
    void disablesWebhookAutoConfiguration() {
        contextRunner
                .withPropertyValues("chari.baas.webhook.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChariBaasClient.class);
                    assertThat(context).doesNotHaveBean(ChariWebhookDispatcher.class);
                    assertThat(context).doesNotHaveBean(ChariWebhookController.class);
                });
    }

    @Test
    void restTemplateAddsOfficialAuthenticationHeaders() {
        contextRunner.run(context -> {
            RestTemplate restTemplate = context.getBean("chariBaasRestTemplate", RestTemplate.class);
            MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

            server.expect(once(), requestTo("https://sandbox.charimoney.com/ping"))
                    .andExpect(header("Chari-Api-Key", "test-key"))
                    .andExpect(header("C-Request-Id", matchesPattern(uuidPattern())))
                    .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

            restTemplate.getForEntity("https://sandbox.charimoney.com/ping", String.class);

            server.verify();
        });
    }

    private static String uuidPattern() {
        return "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
    }
}
