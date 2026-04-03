package com.erdcloud.team.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class TeamDtos {

    private TeamDtos() {
    }

    public record CreateTeamRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description
    ) {
    }

    public record InviteMemberRequest(
        @NotBlank @Size(min = 4, max = 50) String loginId
    ) {
    }

    public record TeamSummary(
        Long id,
        String name,
        String description,
        String role,
        long memberCount,
        long invitationCount,
        Instant updatedAt
    ) {
    }

    public record TeamInvitationResponse(
        Long id,
        Long teamId,
        String teamName,
        String inviteeLoginId,
        String inviteeDisplayName,
        String status,
        Instant expiresAt,
        Instant createdAt
    ) {
    }

    public record TeamAccessResponse(
        Long teamId,
        Long userId,
        String role
    ) {
    }
}
