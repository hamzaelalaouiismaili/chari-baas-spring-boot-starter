package com.github.hamzaelalaouiismaili.chari.client.core;

/**
 * Per-request holder for the end user's browser information, sent to Chari as the
 * {@code User-Agent}, {@code C-Browser-ColorDepth}, {@code C-Browser-ScreenHeight}
 * and {@code C-Browser-ScreenWidth} headers (used for 3DS device fingerprinting).
 *
 * <p>Set it from the incoming HTTP request before calling the SDK and always clear
 * it afterwards — the holder is a {@link ThreadLocal}, and servlet threads are
 * pooled, so a leaked value would bleed into another user's request:
 *
 * <pre>{@code
 * ChariBrowserContext.set(userAgent, colorDepth, screenHeight, screenWidth);
 * try {
 *     chariClient.executeMerchantTokenizedCardPayment(...);
 * } finally {
 *     ChariBrowserContext.clear();
 * }
 * }</pre>
 *
 * <p>When no context is set, the defaults from {@code chari.baas.browser.*} are used.
 * Values do not propagate across {@code @Async} or {@code CompletableFuture} thread
 * boundaries; set the context on the thread that performs the Chari call.
 */
public final class ChariBrowserContext {

    /**
     * Browser information for a single end-user request. When a context is present,
     * its values are used wholesale; otherwise the configured defaults apply.
     */
    public record BrowserInfo(String userAgent, int colorDepth, int screenHeight, int screenWidth) {
    }

    private static final ThreadLocal<BrowserInfo> HOLDER = new ThreadLocal<>();

    private ChariBrowserContext() {
    }

    public static void set(String userAgent, int colorDepth, int screenHeight, int screenWidth) {
        HOLDER.set(new BrowserInfo(userAgent, colorDepth, screenHeight, screenWidth));
    }

    public static BrowserInfo get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
