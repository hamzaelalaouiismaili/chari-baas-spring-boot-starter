package com.github.hamzaelalaouiismaili.chari.client;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariBrowserContext;
import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.config.ChariBaasProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class ChariBrowserHeadersTest {

  @Test
  void sendsDefaultBrowserHeadersOnEveryCall() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariHttpClient httpClient = new ChariHttpClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/ping"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"))
        .andExpect(header("C-Browser-ColorDepth", "24"))
        .andExpect(header("C-Browser-ScreenHeight", "1080"))
        .andExpect(header("C-Browser-ScreenWidth", "1920"))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    httpClient.get("/api/ping", String.class, "PING");

    server.verify();
  }

  @Test
  void browserContextOverridesDefaultHeaders() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariHttpClient httpClient = new ChariHttpClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/ping"))
        .andExpect(header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)"))
        .andExpect(header("C-Browser-ColorDepth", "32"))
        .andExpect(header("C-Browser-ScreenHeight", "844"))
        .andExpect(header("C-Browser-ScreenWidth", "390"))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    ChariBrowserContext.set("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)", 32, 844, 390);
    httpClient.get("/api/ping", String.class, "PING");

    server.verify();
  }

  @Test
  void clearRestoresDefaultHeaders() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    ChariHttpClient httpClient = new ChariHttpClient(restTemplate, properties());

    server.expect(once(), requestTo("https://sandbox.charimoney.com/api/ping"))
        .andExpect(header("C-Browser-ColorDepth", "24"))
        .andExpect(header("C-Browser-ScreenHeight", "1080"))
        .andExpect(header("C-Browser-ScreenWidth", "1920"))
        .andRespond(withSuccess("{\"data\":true}", MediaType.APPLICATION_JSON));

    ChariBrowserContext.set("Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)", 32, 844, 390);
    ChariBrowserContext.clear();
    httpClient.get("/api/ping", String.class, "PING");

    server.verify();
  }

  @AfterEach
  void clearBrowserContext() {
    ChariBrowserContext.clear();
  }

  private static ChariBaasProperties properties() {
    ChariBaasProperties properties = new ChariBaasProperties();
    properties.setBaseUrl("https://sandbox.charimoney.com");
    properties.setApiKey("test-key");
    properties.getAudit().setEnabled(false);
    return properties;
  }
}
