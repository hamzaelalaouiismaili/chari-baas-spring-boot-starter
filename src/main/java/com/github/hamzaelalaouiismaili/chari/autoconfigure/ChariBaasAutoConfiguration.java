package com.github.hamzaelalaouiismaili.chari.autoconfigure;

import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Auto-configuration for the Chari BaaS HTTP client.
 */
@AutoConfiguration
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties(ChariBaasProperties.class)
public class ChariBaasAutoConfiguration {

    @Bean(name = "chariBaasRestTemplate")
    @ConditionalOnMissingBean(name = "chariBaasRestTemplate")
    public RestTemplate chariBaasRestTemplate(ChariBaasProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(properties.getTimeoutMs());
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(authInterceptor(properties));
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public ChariBaasClient chariBaasClient(
            @Qualifier("chariBaasRestTemplate") RestTemplate restTemplate,
            ChariBaasProperties properties) {
        return new ChariBaasClient(restTemplate, properties);
    }

    private ClientHttpRequestInterceptor authInterceptor(ChariBaasProperties properties) {
        return (request, body, execution) -> {
            if (request.getHeaders().getFirst("Chari-Api-Key") == null) {
                request.getHeaders().set("Chari-Api-Key", properties.getApiKey());
            }
            if (request.getHeaders().getFirst("C-Request-Id") == null) {
                request.getHeaders().set("C-Request-Id", UUID.randomUUID().toString());
            }
            if (request.getHeaders().getContentType() == null) {
                request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            }
            if (request.getHeaders().getAccept().isEmpty()) {
                request.getHeaders().setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            }
            return execution.execute(request, body);
        };
    }
}
