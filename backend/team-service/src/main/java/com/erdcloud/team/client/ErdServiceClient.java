package com.erdcloud.team.client;

import com.erdcloud.team.config.FeignHeaderRelayConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "erd-service",
    url = "${app.services.erd-service.url}",
    configuration = FeignHeaderRelayConfig.class
)
public interface ErdServiceClient {

    @DeleteMapping("/internal/erds/teams/{teamId}/erds")
    void deleteTeamDocuments(@PathVariable Long teamId);
}
