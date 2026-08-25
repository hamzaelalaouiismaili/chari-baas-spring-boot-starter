package com.github.hamzaelalaouiismaili.chari.client.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

/**
 * Shared HTTP transport for Chari BaaS clients.
 */
@Slf4j
public class ChariHttpClient {

    private final RestTemplate restTemplate;
    private final ChariBaasProperties properties;
    private final ObjectMapper errorMapper;
    private final ChariAuditLogger auditLogger;

    public ChariHttpClient(RestTemplate restTemplate, ChariBaasProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.errorMapper = new ObjectMapper();
        this.auditLogger = new ChariAuditLogger(properties.getAudit());
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

        auditLogger.logRequest(requestId, method.name(), url, auditedPayload, stage, multipart);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    new HttpEntity<>(payload, multipart
                            ? buildMultipartRequestHeaders(requestId)
                            : buildRequestHeaders(requestId)),
                    responseType);
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logResponse(requestId, stage, response.getStatusCode().value(),
                    response.getHeaders(), response.getBody(), duration, null);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logResponse(requestId, stage, e.getStatusCode().value(),
                    e.getResponseHeaders(), e.getResponseBodyAsString(), duration, e);
            throw toChariBaasException(stage, e);
        } catch (HttpServerErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logResponse(requestId, stage, e.getStatusCode().value(),
                    e.getResponseHeaders(), e.getResponseBodyAsString(), duration, e);
            throw toChariBaasException(stage, e);
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logConnectionError(requestId, stage, url, duration, e);
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw ChariBaasException.timeout(stage);
            }
            throw ChariBaasException.connectionError(stage, e);
        } catch (RestClientException e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.logParsingError(requestId, stage, url, duration, e);
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
        ChariBrowserContext.BrowserInfo context = ChariBrowserContext.get();
        if (context != null) {
            headers.set(HttpHeaders.USER_AGENT, context.userAgent());
            headers.set("C-Browser-ColorDepth", String.valueOf(context.colorDepth()));
            headers.set("C-Browser-ScreenHeight", String.valueOf(context.screenHeight()));
            headers.set("C-Browser-ScreenWidth", String.valueOf(context.screenWidth()));
        } else {
            ChariBaasProperties.Browser browser = properties.getBrowser();
            headers.set(HttpHeaders.USER_AGENT, browser.getUserAgent());
            headers.set("C-Browser-ColorDepth", String.valueOf(browser.getColorDepth()));
            headers.set("C-Browser-ScreenHeight", String.valueOf(browser.getScreenHeight()));
            headers.set("C-Browser-ScreenWidth", String.valueOf(browser.getScreenWidth()));
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private HttpHeaders buildMultipartRequestHeaders(String requestId) {
        HttpHeaders headers = buildRequestHeaders(requestId);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
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
            ChariErrorResponse errorResponse = errorMapper.readValue(body, ChariErrorResponse.class);
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
