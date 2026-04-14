package com.erdcloud.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class RequestCorrelationMdc {

    private RequestCorrelationMdc() {
    }

    public static Scope open(String requestId, String userId, String userLoginId) {
        Map<String, String> previousValues = capture();
        putIfHasText(MdcConstants.REQUEST_ID, requestId);
        putIfHasText(MdcConstants.USER_ID, userId);
        putIfHasText(MdcConstants.USER_LOGIN_ID, userLoginId);
        return () -> restore(previousValues);
    }

    public static String ensureRequestId(String requestId) {
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private static Map<String, String> capture() {
        Map<String, String> previousValues = new LinkedHashMap<>();
        previousValues.put(MdcConstants.REQUEST_ID, MDC.get(MdcConstants.REQUEST_ID));
        previousValues.put(MdcConstants.USER_ID, MDC.get(MdcConstants.USER_ID));
        previousValues.put(MdcConstants.USER_LOGIN_ID, MDC.get(MdcConstants.USER_LOGIN_ID));
        return previousValues;
    }

    private static void restore(Map<String, String> previousValues) {
        restoreKey(MdcConstants.REQUEST_ID, previousValues.get(MdcConstants.REQUEST_ID));
        restoreKey(MdcConstants.USER_ID, previousValues.get(MdcConstants.USER_ID));
        restoreKey(MdcConstants.USER_LOGIN_ID, previousValues.get(MdcConstants.USER_LOGIN_ID));
    }

    private static void restoreKey(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
            return;
        }
        MDC.remove(key);
    }

    private static void putIfHasText(String key, String value) {
        if (StringUtils.hasText(value)) {
            MDC.put(key, value);
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {

        @Override
        void close();
    }
}
