package com.erdcloud.app.team;

import com.erdcloud.app.auth.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamDtos.TeamSummary> list(@AuthenticationPrincipal UserPrincipal principal) {
        return teamService.list(principal);
    }

    @PostMapping
    public TeamDtos.TeamSummary create(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody TeamDtos.CreateTeamRequest request
    ) {
        return teamService.create(principal, request);
    }

    @PostMapping("/{teamId}/invitations")
    public TeamDtos.TeamInvitationResponse invite(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long teamId,
        @Valid @RequestBody TeamDtos.InviteMemberRequest request
    ) {
        return teamService.invite(principal, teamId, request);
    }

    @PostMapping("/invitations/{token}/accept")
    public TeamDtos.TeamSummary accept(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String token
    ) {
        return teamService.accept(principal, token);
    }
}

