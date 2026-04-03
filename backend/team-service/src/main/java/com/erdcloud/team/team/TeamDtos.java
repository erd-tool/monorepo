package com.erdcloud.team.team;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class TeamDtos {

    private TeamDtos() {
    }

    public record CreateTeamRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description
    ) {
    }

    public record InviteMemberRequest(
        @NotBlank @Email String email
    ) {
    }

    public record TeamSummary(
        Long id,
        String name,
        String description,
        String role
    ) {
    }

    public record TeamInvitationResponse(
        Long id,
        String email,
        String token,
        String teamName
    ) {
    }

    public record TeamAccessResponse(
        Long teamId,
        Long userId,
        String role
    ) {
    }
}
