package com.erdcloud.erd.config;

import com.erdcloud.common.HeaderConstants;
import feign.RequestInterceptor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignHeaderRelayConfig {

    @Bean
    RequestInterceptor relayGatewayHeaders() {
        return requestTemplate -> {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
                return;
            }

            var request = servletAttributes.getRequest();
            List.of(
                HeaderConstants.USER_ID,
                HeaderConstants.USER_LOGIN_ID,
                HeaderConstants.USER_ROLE,
                HeaderConstants.USER_EMAIL,
                HeaderConstants.REQUEST_ID
            ).forEach(header -> {
                String value = request.getHeader(header);
                if (value != null && !value.isBlank()) {
                    requestTemplate.header(header, value);
                }
            });
        };
    }
}
