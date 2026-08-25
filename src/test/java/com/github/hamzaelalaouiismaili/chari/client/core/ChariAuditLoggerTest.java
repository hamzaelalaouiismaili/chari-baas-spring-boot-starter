package com.github.hamzaelalaouiismaili.chari.client.core;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ChariAuditLoggerTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(ChariAuditLogger.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    private ChariAuditLogger auditLogger(ChariBaasProperties.Audit.Format format) {
        ChariBaasProperties.Audit audit = new ChariBaasProperties.Audit();
        audit.setFormat(format);
        return new ChariAuditLogger(audit);
    }

    @Test
    void kvResponseIsSingleLineWithParsableFields() {
        auditLogger(ChariBaasProperties.Audit.Format.KV).logResponse(
                "req-1", "GET_CUSTOMER_INFO", 200, jsonHeaders(),
                Map.of("data", Map.of("id", 16781)), 102, null);

        assertThat(appender.list).hasSize(1);
        ILoggingEvent event = appender.list.get(0);
        String message = event.getFormattedMessage();
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(message).doesNotContain("\n");
        assertThat(message)
                .contains("[CHARI-AUDIT]")
                .contains("event=response")
                .contains("request_id=req-1")
                .contains("stage=GET_CUSTOMER_INFO")
                .contains("status=200")
                .contains("outcome=SUCCESS")
                .contains("duration_ms=102")
                .contains("body={\"data\":{\"id\":16781}}");
    }

    @Test
    void kvRequestIsSingleLineWithCompactPayload() {
        auditLogger(ChariBaasProperties.Audit.Format.KV).logRequest(
                "req-2", "POST", "https://api.chari.ma/x", Map.of("a", 1), "PAY", false);

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\n");
        assertThat(message)
                .contains("event=request")
                .contains("method=POST")
                .contains("url=https://api.chari.ma/x")
                .contains("stage=PAY")
                .contains("payload={\"a\":1}");
    }

    @Test
    void kvErrorResponseLogsAtErrorLevelWithOutcomeError() {
        auditLogger(ChariBaasProperties.Audit.Format.KV).logResponse(
                "req-3", "PAY", 500, null, "boom", 55, new RuntimeException("x"));

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("status=500")
                .contains("outcome=ERROR");
    }

    @Test
    void kvConnectionErrorIsSingleLineAndStripsNewlinesFromMessage() {
        auditLogger(ChariBaasProperties.Audit.Format.KV).logConnectionError(
                "req-4", "PAY", "https://api.chari.ma/x", 7,
                new RuntimeException("line1\nline2"));

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\n");
        assertThat(message)
                .contains("event=connection_error")
                .contains("error_type=RuntimeException")
                .contains("line1 line2");
    }

    @Test
    void kvMasksSensitiveFieldsWhenEnabled() {
        ChariBaasProperties.Audit audit = new ChariBaasProperties.Audit();
        audit.setFormat(ChariBaasProperties.Audit.Format.KV);
        audit.setMaskSensitive(true);
        new ChariAuditLogger(audit).logRequest(
                "req-5", "POST", "https://api.chari.ma/x",
                Map.of("pan", "1234567890123456"), "CARD", false);

        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("\"pan\":\"1234********3456\"")
                .doesNotContain("1234567890123456");
    }

    @Test
    void bannerFormatKeepsMultiLineBox() {
        auditLogger(ChariBaasProperties.Audit.Format.BANNER).logResponse(
                "req-6", "GET_CUSTOMER_INFO", 200, jsonHeaders(),
                Map.of("data", 1), 10, null);

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message)
                .contains("CHARI API RESPONSE")
                .contains("╔")
                .contains("\n");
    }

    @Test
    void disabledAuditLogsNothing() {
        ChariBaasProperties.Audit audit = new ChariBaasProperties.Audit();
        audit.setEnabled(false);
        ChariAuditLogger silent = new ChariAuditLogger(audit);
        silent.logRequest("r", "GET", "u", null, "S", false);
        silent.logResponse("r", "S", 200, null, null, 1, null);
        silent.logConnectionError("r", "S", "u", 1, new RuntimeException("x"));
        silent.logParsingError("r", "S", "u", 1, new RuntimeException("x"));

        assertThat(appender.list).isEmpty();
    }

    @Test
    void kvParsingErrorIsSingleLine() {
        auditLogger(ChariBaasProperties.Audit.Format.KV).logParsingError(
                "req-7", "PAY", "https://api.chari.ma/x", 9, new RuntimeException("bad json"));

        assertThat(appender.list).hasSize(1);
        String message = appender.list.get(0).getFormattedMessage();
        assertThat(message).doesNotContain("\n");
        assertThat(message).contains("event=parsing_error").contains("bad json");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
