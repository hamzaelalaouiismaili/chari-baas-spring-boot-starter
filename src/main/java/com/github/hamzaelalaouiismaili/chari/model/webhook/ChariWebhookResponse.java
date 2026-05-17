package com.github.hamzaelalaouiismaili.chari.model.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard response returned to Chari after processing a webhook.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChariWebhookResponse {

    private String status;

    private String message;

    private Instant processedAt;

    public static ChariWebhookResponse accepted() {
        return ChariWebhookResponse.builder()
                .status("accepted")
                .message("Event processed successfully")
                .processedAt(Instant.now())
                .build();
    }

    public static ChariWebhookResponse rejected(String reason) {
        return ChariWebhookResponse.builder()
                .status("rejected")
                .message(reason)
                .processedAt(Instant.now())
                .build();
    }
}
