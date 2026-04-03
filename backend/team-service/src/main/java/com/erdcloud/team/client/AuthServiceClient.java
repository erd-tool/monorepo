package com.erdcloud.team.client;

import com.erdcloud.team.config.FeignHeaderRelayConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "auth-service",
    url = "${app.services.auth-service.url}",
    configuration = FeignHeaderRelayConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/internal/users/by-login-id")
    AuthUserSummary findByLoginId(@RequestParam String loginId);

    record AuthUserSummary(
        Long id,
        String loginId,
        String email,
        String displayName
    ) {
    }
}
