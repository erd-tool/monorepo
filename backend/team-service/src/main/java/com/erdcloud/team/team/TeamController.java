package com.erdcloud.team.team;

import com.erdcloud.common.HeaderConstants;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/invitations")
    public List<TeamDtos.TeamInvitationResponse> listInvitations(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return teamService.listInvitations(userId);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public TeamDtos.TeamSummary accept(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long invitationId
    ) {
        return teamService.accept(userId, invitationId);
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> reject(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long invitationId
    ) {
        teamService.reject(userId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/token/{token}/accept")
    public TeamDtos.TeamSummary accept(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable String token
    ) {
        return teamService.accept(userId, token);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> delete(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long teamId
    ) {
        teamService.delete(userId, teamId);
        return ResponseEntity.noContent().build();
    }
}
