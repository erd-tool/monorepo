package com.erdcloud.gateway.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;

public final class GatewayRequestCorrelationMdc {

    private GatewayRequestCorrelationMdc() {
    }

    public static Scope open(String requestId, String userId, String userLoginId) {
        Map<String, String> previousValues = capture();
        putIfPresent("requestId", requestId);
        putIfPresent("userId", userId);
        putIfPresent("userLoginId", userLoginId);
        return () -> restore(previousValues);
    }

    public static String ensureRequestId(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private static Map<String, String> capture() {
        Map<String, String> previousValues = new LinkedHashMap<>();
        previousValues.put("requestId", MDC.get("requestId"));
        previousValues.put("userId", MDC.get("userId"));
        previousValues.put("userLoginId", MDC.get("userLoginId"));
        return previousValues;
    }

    private static void restore(Map<String, String> previousValues) {
        restoreKey("requestId", previousValues.get("requestId"));
        restoreKey("userId", previousValues.get("userId"));
        restoreKey("userLoginId", previousValues.get("userLoginId"));
    }

    private static void restoreKey(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
            return;
        }
        MDC.remove(key);
    }

    private static void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {

        @Override
        void close();
    }
}
