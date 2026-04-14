package com.erdcloud.team.config;

import com.erdcloud.common.HeaderConstants;
import com.erdcloud.common.RequestCorrelationMdc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = RequestCorrelationMdc.ensureRequestId(headerValue(request, HeaderConstants.REQUEST_ID));
        String userId = headerValue(request, HeaderConstants.USER_ID);
        String userLoginId = headerValue(request, HeaderConstants.USER_LOGIN_ID);

        response.setHeader(HeaderConstants.REQUEST_ID, requestId);
        try (RequestCorrelationMdc.Scope ignored = RequestCorrelationMdc.open(requestId, userId, userLoginId)) {
            filterChain.doFilter(request, response);
        }
    }

    private static String headerValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null || value.isBlank() ? null : value;
    }
}
