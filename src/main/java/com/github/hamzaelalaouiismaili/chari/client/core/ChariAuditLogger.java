package com.github.hamzaelalaouiismaili.chari.client.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.time.Instant;

/**
 * Renders and emits the {@code [CHARI-AUDIT]} log entries for outbound Chari
 * API calls in one of two formats, selected via
 * {@code chari.baas.audit.format}:
 *
 * <ul>
 *   <li>{@code BANNER} — the historical multi-line ASCII box, pleasant on a
 *       developer console;</li>
 *   <li>{@code KV} — one single-line logfmt-style entry per event (no
 *       newlines), designed for log aggregators such as Loki: fixed fields
 *       first ({@code event}, {@code request_id}, {@code stage}, …) with the
 *       free-form JSON body last.</li>
 * </ul>
 */
@Slf4j
class ChariAuditLogger {

    private final boolean enabled;
    private final boolean maskSensitive;
    private final ChariBaasProperties.Audit.Format format;
    private final ObjectMapper bannerMapper;
    private final ObjectMapper compactMapper;

    ChariAuditLogger(ChariBaasProperties.Audit audit) {
        this.enabled = audit.isEnabled();
        this.maskSensitive = audit.isMaskSensitive();
        this.format = audit.getFormat();
        this.bannerMapper = new ObjectMapper();
        this.bannerMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.compactMapper = new ObjectMapper();
    }

    void logRequest(String requestId, String method, String url, Object payload,
            String stage, boolean multipart) {
        if (!enabled) {
            return;
        }
        if (format == ChariBaasProperties.Audit.Format.KV) {
            StringBuilder kv = kvHeader("request", requestId, stage);
            kv.append(" method=").append(method);
            kv.append(" url=").append(url);
            kv.append(" multipart=").append(multipart);
            if (payload != null) {
                kv.append(" payload=").append(serialize(payload, compactMapper));
            }
            log.info("[CHARI-AUDIT] {}", singleLine(kv));
            return;
        }

        StringBuilder curl = new StringBuilder();
        curl.append("\n╔══════════════════════════════════════════════════════════════════════════════\n");
        curl.append("║ CHARI API REQUEST\n");
        curl.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        curl.append("║ Request ID  : ").append(requestId).append("\n");
        curl.append("║ Timestamp   : ").append(Instant.now()).append("\n");
        curl.append("║ Stage       : ").append(stage).append("\n");
        curl.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        curl.append("║ CURL COMMAND:\n");
        curl.append("╟──────────────────────────────────────────────────────────────────────────────\n");
        curl.append("║ curl -X ").append(method).append(" '").append(url).append("' \\\n");
        curl.append("║   -H 'Content-Type: ")
                .append(multipart ? "multipart/form-data" : "application/json")
                .append("' \\\n");
        curl.append("║   -H 'Chari-Api-Key: <API_KEY>' \\\n");
        curl.append("║   -H 'C-Request-Id: ").append(requestId).append("' \\\n");
        if (payload != null) {
            curl.append("║   -d '").append(serialize(payload, bannerMapper)).append("'\n");
        }
        curl.append("╚══════════════════════════════════════════════════════════════════════════════");
        log.info("[CHARI-AUDIT] {}", curl);
    }

    void logResponse(String requestId, String stage, int statusCode,
            HttpHeaders headers, Object body, long durationMs, Exception error) {
        if (!enabled) {
            return;
        }
        if (format == ChariBaasProperties.Audit.Format.KV) {
            StringBuilder kv = kvHeader("response", requestId, stage);
            kv.append(" status=").append(statusCode);
            kv.append(" outcome=").append(error != null ? "ERROR" : "SUCCESS");
            kv.append(" duration_ms=").append(durationMs);
            if (headers != null && headers.getContentType() != null) {
                kv.append(" content_type=").append(headers.getContentType());
            }
            if (body != null) {
                kv.append(" body=").append(serialize(body, compactMapper));
            }
            emit(singleLine(kv), error != null);
            return;
        }

        StringBuilder response = new StringBuilder();
        response.append("\n╔══════════════════════════════════════════════════════════════════════════════\n");
        response.append("║ CHARI API RESPONSE\n");
        response.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        response.append("║ Request ID  : ").append(requestId).append("\n");
        response.append("║ Timestamp   : ").append(Instant.now()).append("\n");
        response.append("║ Stage       : ").append(stage).append("\n");
        response.append("║ Duration    : ").append(durationMs).append(" ms\n");
        response.append("║ Status      : ").append(statusCode).append(error != null ? " (ERROR)" : " (SUCCESS)")
                .append("\n");
        response.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        if (headers != null) {
            response.append("║ HEADERS:\n");
            response.append("╟──────────────────────────────────────────────────────────────────────────────\n");
            if (headers.getContentType() != null) {
                response.append("║   Content-Type: ").append(headers.getContentType()).append("\n");
            }
            if (headers.getContentLength() >= 0) {
                response.append("║   Content-Length: ").append(headers.getContentLength()).append("\n");
            }
            response.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        }
        response.append("║ BODY:\n");
        response.append("╟──────────────────────────────────────────────────────────────────────────────\n");
        if (body != null) {
            String bodyJson = serialize(body, bannerMapper);
            for (String line : bodyJson.split("\n")) {
                response.append("║ ").append(line).append("\n");
            }
        } else {
            response.append("║ (empty body)\n");
        }
        response.append("╚══════════════════════════════════════════════════════════════════════════════");
        emit(response, error != null);
    }

    void logConnectionError(String requestId, String stage, String url, long durationMs, Exception e) {
        if (!enabled) {
            return;
        }
        if (format == ChariBaasProperties.Audit.Format.KV) {
            StringBuilder kv = kvHeader("connection_error", requestId, stage);
            kv.append(" url=").append(url);
            kv.append(" duration_ms=").append(durationMs);
            appendError(kv, e);
            log.error("[CHARI-AUDIT] {}", singleLine(kv));
            return;
        }

        StringBuilder error = new StringBuilder();
        error.append("\n╔══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ CHARI API CONNECTION ERROR\n");
        error.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ Request ID  : ").append(requestId).append("\n");
        error.append("║ Timestamp   : ").append(Instant.now()).append("\n");
        error.append("║ Stage       : ").append(stage).append("\n");
        error.append("║ URL         : ").append(url).append("\n");
        error.append("║ Duration    : ").append(durationMs).append(" ms\n");
        error.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ ERROR:\n");
        error.append("╟──────────────────────────────────────────────────────────────────────────────\n");
        error.append("║ Type    : ").append(e.getClass().getSimpleName()).append("\n");
        error.append("║ Message : ").append(e.getMessage()).append("\n");
        if (e.getCause() != null) {
            error.append("║ Cause   : ").append(e.getCause().getMessage()).append("\n");
        }
        error.append("╚══════════════════════════════════════════════════════════════════════════════");
        log.error("[CHARI-AUDIT] {}", error);
    }

    void logParsingError(String requestId, String stage, String url, long durationMs, Exception e) {
        if (!enabled) {
            return;
        }
        if (format == ChariBaasProperties.Audit.Format.KV) {
            StringBuilder kv = kvHeader("parsing_error", requestId, stage);
            kv.append(" url=").append(url);
            kv.append(" duration_ms=").append(durationMs);
            appendError(kv, e);
            log.error("[CHARI-AUDIT] {}", singleLine(kv));
            return;
        }

        StringBuilder error = new StringBuilder();
        error.append("\n╔══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ CHARI API PARSING ERROR\n");
        error.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ Request ID  : ").append(requestId).append("\n");
        error.append("║ Timestamp   : ").append(Instant.now()).append("\n");
        error.append("║ Stage       : ").append(stage).append("\n");
        error.append("║ URL         : ").append(url).append("\n");
        error.append("║ Duration    : ").append(durationMs).append(" ms\n");
        error.append("╠══════════════════════════════════════════════════════════════════════════════\n");
        error.append("║ ERROR: Response received but failed to parse\n");
        error.append("╟──────────────────────────────────────────────────────────────────────────────\n");
        error.append("║ Type    : ").append(e.getClass().getSimpleName()).append("\n");
        error.append("║ Message : ").append(e.getMessage()).append("\n");
        if (e.getCause() != null) {
            error.append("║ Cause   : ").append(e.getCause().getClass().getSimpleName())
                    .append(" - ").append(e.getCause().getMessage()).append("\n");
        }
        error.append("║\n");
        error.append("║ TROUBLESHOOTING:\n");
        error.append("║ - The API returned a response but it doesn't match the expected format\n");
        error.append("║ - Contact Chari support with the Request ID above\n");
        error.append("║ - Check if the API response structure has changed\n");
        error.append("╚══════════════════════════════════════════════════════════════════════════════");
        log.error("[CHARI-AUDIT] {}", error);
    }

    private StringBuilder kvHeader(String event, String requestId, String stage) {
        return new StringBuilder()
                .append("event=").append(event)
                .append(" request_id=").append(requestId)
                .append(" stage=").append(stage);
    }

    private void appendError(StringBuilder kv, Exception e) {
        kv.append(" error_type=").append(e.getClass().getSimpleName());
        kv.append(" error_message=\"").append(e.getMessage()).append('"');
        if (e.getCause() != null) {
            kv.append(" error_cause=\"").append(e.getCause().getMessage()).append('"');
        }
    }

    private void emit(CharSequence message, boolean isError) {
        if (isError) {
            log.error("[CHARI-AUDIT] {}", message);
        } else {
            log.info("[CHARI-AUDIT] {}", message);
        }
    }

    /** KV entries must be exactly one log line — collapse any embedded newlines. */
    private String singleLine(StringBuilder kv) {
        return kv.toString().replace("\r", " ").replace("\n", " ");
    }

    private String serialize(Object payload, ObjectMapper mapper) {
        if (payload == null) {
            return "null";
        }
        String json;
        if (payload instanceof String s) {
            json = s;
        } else {
            try {
                json = mapper.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                json = payload.toString();
            }
        }
        return maskSensitive ? maskSensitiveFields(json) : json;
    }

    private String maskSensitiveFields(String json) {
        json = json.replaceAll("\"pan\"\\s*:\\s*\"(\\d{4})\\d+(\\d{4})\"", "\"pan\":\"$1********$2\"");
        json = json.replaceAll("\"Pan\"\\s*:\\s*\"(\\d{4})\\d+(\\d{4})\"", "\"Pan\":\"$1********$2\"");
        json = json.replaceAll("\"cvv\"\\s*:\\s*\"\\d+\"", "\"cvv\":\"***\"");
        json = json.replaceAll("\"Cvv\"\\s*:\\s*\"\\d+\"", "\"Cvv\":\"***\"");
        json = json.replaceAll("\"pin\"\\s*:\\s*\"\\d+\"", "\"pin\":\"****\"");
        json = json.replaceAll("\"expiryDate\"\\s*:\\s*\"(\\d{2})/(\\d{2})\"", "\"expiryDate\":\"$1/**\"");
        json = json.replaceAll("\"ExpiryDate\"\\s*:\\s*\"(\\d{2})/(\\d{2})\"", "\"ExpiryDate\":\"$1/**\"");
        return json;
    }
}
