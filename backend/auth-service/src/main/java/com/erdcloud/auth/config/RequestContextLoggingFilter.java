package com.erdcloud.auth.config;

import com.erdcloud.common.HeaderConstants;
import com.erdcloud.common.RequestCorrelationMdc;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestContextLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = RequestCorrelationMdc.ensureRequestId(request.getHeader(HeaderConstants.REQUEST_ID));
        String userId = request.getHeader(HeaderConstants.USER_ID);
        String userLoginId = request.getHeader(HeaderConstants.USER_LOGIN_ID);
        response.setHeader(HeaderConstants.REQUEST_ID, requestId);

        try (RequestCorrelationMdc.Scope ignored = RequestCorrelationMdc.open(requestId, userId, userLoginId)) {
            filterChain.doFilter(request, response);
        }
    }
}
