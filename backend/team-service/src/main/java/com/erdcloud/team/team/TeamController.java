package com.erdcloud.team.team;

import com.erdcloud.common.HeaderConstants;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamDtos.TeamSummary> list(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return teamService.list(userId);
    }

    @PostMapping
    public TeamDtos.TeamSummary create(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @Valid @RequestBody TeamDtos.CreateTeamRequest request
    ) {
        return teamService.create(userId, request);
    }

    @PostMapping("/{teamId}/invitations")
    public TeamDtos.TeamInvitationResponse invite(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long teamId,
        @Valid @RequestBody TeamDtos.InviteMemberRequest request
    ) {
        return teamService.invite(userId, teamId, request);
    }

    @PostMapping("/invitations/{token}/accept")
    public TeamDtos.TeamSummary accept(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @RequestHeader(HeaderConstants.USER_EMAIL) String userEmail,
        @PathVariable String token
    ) {
        return teamService.accept(userId, userEmail, token);
    }
}
