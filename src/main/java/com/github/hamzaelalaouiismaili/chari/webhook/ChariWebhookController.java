package com.github.hamzaelalaouiismaili.chari.webhook;

import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariWebhookSignatureException;
import com.github.hamzaelalaouiismaili.chari.model.webhook.ChariWebhookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auto-configured webhook endpoint for Chari BaaS callbacks.
 */
@RestController
public class ChariWebhookController {

    private final ChariWebhookDispatcher dispatcher;

    public ChariWebhookController(ChariWebhookDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("${chari.baas.webhook.path:/webhooks/chari}")
    public ResponseEntity<ChariWebhookResponse> handleWebhook(
            @RequestHeader(name = "x-chari-signature", required = false) String signature,
            @RequestHeader(name = "x-chari-timestamp", required = false) String timestamp,
            @RequestBody String rawBody) {
        ChariWebhookResponse response = dispatcher.dispatch(signature, timestamp, rawBody);
        if ("rejected".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(ChariWebhookSignatureException.class)
    public ResponseEntity<ChariWebhookResponse> handleSignatureFailure(ChariWebhookSignatureException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ChariWebhookResponse.rejected(e.getMessage()));
    }
}
