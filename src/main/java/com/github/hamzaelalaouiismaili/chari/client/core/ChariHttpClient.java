package com.github.hamzaelalaouiismaili.chari.client.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import com.github.hamzaelalaouiismaili.chari.domain.exception.ChariBaasException;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * Shared HTTP transport for Chari BaaS clients.
 */
@Slf4j
public class ChariHttpClient {

    private final RestTemplate restTemplate;
    private final ChariBaasProperties properties;
    private final ObjectMapper auditMapper;
    private final boolean auditEnabled;
    private final boolean maskSensitiveData;

    public ChariHttpClient(RestTemplate restTemplate, ChariBaasProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.auditEnabled = properties.getAudit().isEnabled();
        this.maskSensitiveData = properties.getAudit().isMaskSensitive();
        this.auditMapper = new ObjectMapper();
        this.auditMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public <T> T get(String path, Class<T> responseType, String stage) {
        return exchange(path, null, responseType, stage, HttpMethod.GET, false);
    }

    public <T> T post(String path, Object payload, Class<T> responseType, String stage) {
        return exchange(path, payload, responseType, stage, HttpMethod.POST, false);
    }

    public <T> T postMultipart(String path, MultiValueMap<String, Object> payload,
            Class<T> responseType, String stage) {
        return exchange(path, payload, responseType, stage, HttpMethod.POST, true);
    }

    public <T> T patch(String path, Object payload, Class<T> responseType, String stage) {
        return exchange(path, payload, responseType, stage, HttpMethod.PATCH, false);
    }

    public <T> T put(String path, Object payload, Class<T> responseType, String stage) {
        return exchange(path, payload, responseType, stage, HttpMethod.PUT, false);
    }

    public <T> T delete(String path, Class<T> responseType, String stage) {
        return exchange(path, null, responseType, stage, HttpMethod.DELETE, false);
    }

    private <T> T exchange(String path, Object payload, Class<T> responseType, String stage,
            HttpMethod method, boolean multipart) {
        String url = buildFullUrl(path);
        String requestId = generateRequestId();
        long startTime = System.currentTimeMillis();
        Object auditedPayload = multipart ? "(multipart/form-data)" : payload;

        logCurlCommand(requestId, method.name(), url, auditedPayload, stage, multipart);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    new HttpEntity<>(payload, multipart
                            ? buildMultipartRequestHeaders(requestId)
                            : buildRequestHeaders(requestId)),
                    responseType);
            long duration = System.currentTimeMillis() - startTime;
            logResponse(requestId, stage, response.getStatusCode().value(),
                    response.getHeaders(), response.getBody(), duration, null);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(requestId, stage, e.getStatusCode().value(),
                    e.getResponseHeaders(), e.getResponseBodyAsString(), duration, e);
            throw toChariBaasException(stage, e);
        } catch (HttpServerErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(requestId, stage, e.getStatusCode().value(),
                    e.getResponseHeaders(), e.getResponseBodyAsString(), duration, e);
            throw toChariBaasException(stage, e);
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - startTime;
            logConnectionError(requestId, stage, url, duration, e);
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw ChariBaasException.timeout(stage);
            }
            throw ChariBaasException.connectionError(stage, e);
        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            logParsingError(requestId, stage, url, duration, e);
            throw ChariBaasException.apiError(stage,
                    "[" + stage + "] Failed to parse the Chari response: " + e.getMessage(), 500);
        }
    }

    private String buildFullUrl(String path) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    private HttpHeaders buildRequestHeaders(String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Chari-Api-Key", properties.getApiKey());
        headers.set("C-Request-Id", requestId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private HttpHeaders buildMultipartRequestHeaders(String requestId) {
        HttpHeaders headers = buildRequestHeaders(requestId);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private void logCurlCommand(String requestId, String method, String url, Object payload,
            String stage, boolean multipart) {
        if (!auditEnabled) {
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
            String payloadJson = serializePayload(payload);
            curl.append("║   -d '").append(payloadJson).append("'\n");
        }

        curl.append("╚══════════════════════════════════════════════════════════════════════════════");
        log.info("[CHARI-AUDIT] {}", curl);
    }

    private void logResponse(String requestId, String stage, int statusCode,
            HttpHeaders headers, Object body, long durationMs, Exception error) {
        if (!auditEnabled) {
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
            String bodyJson = serializePayload(body);
            for (String line : bodyJson.split("\n")) {
                response.append("║ ").append(line).append("\n");
            }
        } else {
            response.append("║ (empty body)\n");
        }
        response.append("╚══════════════════════════════════════════════════════════════════════════════");

        if (error != null) {
            log.error("[CHARI-AUDIT] {}", response);
        } else {
            log.info("[CHARI-AUDIT] {}", response);
        }
    }

    private void logConnectionError(String requestId, String stage, String url, long durationMs, Exception e) {
        if (!auditEnabled) {
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

    private void logParsingError(String requestId, String stage, String url, long durationMs, Exception e) {
        if (!auditEnabled) {
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

    private String serializePayload(Object payload) {
        if (payload == null) {
            return "null";
        }
        if (payload instanceof String) {
            return (String) payload;
        }

        try {
            String json = auditMapper.writeValueAsString(payload);
            if (maskSensitiveData) {
                json = maskSensitiveFields(json);
            }
            return json;
        } catch (JsonProcessingException e) {
            return payload.toString();
        }
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

    private ChariBaasException toChariBaasException(String stage, HttpStatusCodeException e) {
        ChariErrorResponse errorResponse = parseErrorResponse(e.getResponseBodyAsString());
        if (errorResponse != null) {
            return ChariBaasException.fromErrorResponse(
                    stage,
                    e.getStatusCode().value(),
                    errorResponse.getErrorCode(),
                    errorResponse.getErrorDescription(),
                    e.getMessage());
        }
        return ChariBaasException.fromErrorResponse(
                stage, e.getStatusCode().value(), null, null, extractErrorMessage(e));
    }

    private ChariErrorResponse parseErrorResponse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            ChariErrorResponse errorResponse = auditMapper.readValue(body, ChariErrorResponse.class);
            if (errorResponse.getErrorCode() != null || errorResponse.getErrorDescription() != null) {
                return errorResponse;
            }
        } catch (JsonProcessingException e) {
            log.debug("Unable to parse Chari error response body: {}", e.getMessage());
        }
        return null;
    }

    private String extractErrorMessage(HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        ChariErrorResponse errorResponse = parseErrorResponse(body);
        if (errorResponse != null && errorResponse.getErrorDescription() != null) {
            return errorResponse.getErrorDescription();
        }
        if (body.contains("\"message\":")) {
            int start = body.indexOf("\"message\":") + 11;
            int end = body.indexOf("\"", start);
            if (end > start) {
                return body.substring(start, end);
            }
        }
        return e.getMessage();
    }
}
