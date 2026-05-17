package com.github.hamzaelalaouiismaili.chari.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Chari BaaS Spring Boot starter.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "chari.baas")
public class ChariBaasProperties {

    /**
     * Base URL for the Chari BaaS API, for example {@code https://api.chari.ma}.
     */
    @NotBlank(message = "chari.baas.base-url is required")
    private String baseUrl;

    /**
     * API key used for every Chari BaaS HTTP request.
     */
    @NotBlank(message = "chari.baas.api-key is required")
    private String apiKey;

    /**
     * Optional HMAC-SHA256 secret for verifying incoming Chari webhooks.
     */
    private String webhookSecret;

    /**
     * HTTP connection and read timeout in milliseconds.
     */
    @Positive
    private int timeoutMs = 10_000;

    /**
     * Optional principal agent code used for Agent Principal transfers.
     */
    private String principalAgentId;

    /**
     * Optional principal agent RIB used by Agent Principal bank-transfer calls.
     */
    private String principalAgentRib;

    /**
     * Card funding settings.
     */
    @Valid
    private CardFunding cardFunding = new CardFunding();

    /**
     * Webhook endpoint registration settings.
     */
    @Valid
    private Webhook webhook = new Webhook();

    /**
     * Audit logging settings for outbound Chari API calls.
     */
    @Valid
    private Audit audit = new Audit();

    @Getter
    @Setter
    public static class Webhook {

        /**
         * Whether to auto-register the webhook controller.
         */
        private boolean enabled = true;

        /**
         * HTTP path that receives Chari webhook POST requests.
         */
        private String path = "/webhooks/chari";
    }

    @Getter
    @Setter
    public static class Audit {

        /**
         * Whether to log structured request and response audit entries.
         */
        private boolean enabled = true;

        /**
         * Whether audit logs should mask card PAN, CVV, PIN, and expiry fields.
         */
        private boolean maskSensitive = true;
    }

    @Getter
    @Setter
    public static class CardFunding {

        /**
         * Optional default URL where Chari redirects the user after successful 3DS
         * authentication.
         */
        private String acceptUrl;

        /**
         * Optional default URL where Chari redirects the user after failed 3DS
         * authentication.
         */
        private String declineUrl;
    }
}
