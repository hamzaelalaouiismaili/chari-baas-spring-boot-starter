package com.github.hamzaelalaouiismaili.chari.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookController;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookDispatcher;
import com.github.hamzaelalaouiismaili.chari.webhook.ChariWebhookHandler;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for receiving Chari BaaS webhooks.
 */
@AutoConfiguration(after = ChariBaasAutoConfiguration.class)
@ConditionalOnClass(ChariBaasClient.class)
@ConditionalOnProperty(name = "chari.baas.webhook.enabled", havingValue = "true", matchIfMissing = true)
public class ChariBaasWebhookAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper chariBaasObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChariWebhookDispatcher chariWebhookDispatcher(
            ChariBaasProperties properties,
            ObjectMapper objectMapper,
            List<ChariWebhookHandler> handlers) {
        return new ChariWebhookDispatcher(properties, objectMapper, handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChariWebhookController chariWebhookController(ChariWebhookDispatcher dispatcher) {
        return new ChariWebhookController(dispatcher);
    }
}
