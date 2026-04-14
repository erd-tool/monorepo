package com.erdcloud.team.team;

import java.time.Instant;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

public final class TeamResourceModels {

    private TeamResourceModels() {
    }

    @Getter
    public static class TeamSummaryModel extends RepresentationModel<TeamSummaryModel> {

        private final Long id;
        private final String name;
        private final String description;
        private final String role;
        private final long memberCount;
        private final long invitationCount;
        private final Instant updatedAt;

        public TeamSummaryModel(
            Long id,
            String name,
            String description,
            String role,
            long memberCount,
            long invitationCount,
            Instant updatedAt
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.role = role;
            this.memberCount = memberCount;
            this.invitationCount = invitationCount;
            this.updatedAt = updatedAt;
        }
    }

    @Getter
    public static class TeamInvitationModel extends RepresentationModel<TeamInvitationModel> {

        private final Long id;
        private final Long teamId;
        private final String teamName;
        private final String inviteeLoginId;
        private final String inviteeDisplayName;
        private final String status;
        private final Instant expiresAt;
        private final Instant createdAt;

        public TeamInvitationModel(
            Long id,
            Long teamId,
            String teamName,
            String inviteeLoginId,
            String inviteeDisplayName,
            String status,
            Instant expiresAt,
            Instant createdAt
        ) {
            this.id = id;
            this.teamId = teamId;
            this.teamName = teamName;
            this.inviteeLoginId = inviteeLoginId;
            this.inviteeDisplayName = inviteeDisplayName;
            this.status = status;
            this.expiresAt = expiresAt;
            this.createdAt = createdAt;
        }
    }
}
