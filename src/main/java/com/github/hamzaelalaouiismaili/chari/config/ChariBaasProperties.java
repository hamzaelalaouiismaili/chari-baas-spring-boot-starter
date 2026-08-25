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

    /**
     * Default browser information headers sent on every Chari API call when no
     * per-request {@code ChariBrowserContext} is set.
     */
    @Valid
    private Browser browser = new Browser();

    @Getter
    @Setter
    public static class Webhook {

        /**
         * Whether to auto-register the webhook controller.
         */
        private boolean enabled = true;

        /**
         * Whether to verify the HMAC-SHA256 signature of incoming webhooks.
         * When {@code false}, signature verification is bypassed entirely even
         * if {@code chari.baas.webhook-secret} is set. Useful when the upstream
         * provider does not sign its webhook callbacks.
         */
        private boolean verify = true;

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
    public static class Browser {

        /**
         * Value of the {@code User-Agent} header.
         */
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

        /**
         * Value of the {@code C-Browser-ColorDepth} header.
         */
        @Positive
        private int colorDepth = 24;

        /**
         * Value of the {@code C-Browser-ScreenHeight} header.
         */
        @Positive
        private int screenHeight = 1080;

        /**
         * Value of the {@code C-Browser-ScreenWidth} header.
         */
        @Positive
        private int screenWidth = 1920;
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
