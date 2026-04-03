package com.erdcloud.erd.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "team-service", url = "${services.team-service.url}")
public interface TeamServiceClient {

    @GetMapping("/internal/teams/{teamId}/membership")
    TeamAccessResponse getMembership(@PathVariable Long teamId);

    @GetMapping("/internal/users/me/teams")
    List<Long> getUserTeamIds();

    record TeamAccessResponse(
        Long teamId,
        Long userId,
        String role
    ) {
    }
}
