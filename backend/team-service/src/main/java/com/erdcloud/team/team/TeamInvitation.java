package com.erdcloud.team.team;

import com.erdcloud.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_invitations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamInvitation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private Long inviteeUserId;

    @Column(nullable = false, length = 50)
    private String inviteeLoginId;

    @Column(nullable = false, length = 80)
    private String inviteeDisplayName;

    @Column(nullable = false, length = 120)
    private String inviteeEmail;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(nullable = false)
    private boolean accepted;

    @Column(nullable = false)
    private boolean rejected;

    @Column(nullable = false)
    private Instant expiresAt;

    public TeamInvitation(
        Team team,
        Long inviteeUserId,
        String inviteeLoginId,
        String inviteeDisplayName,
        String inviteeEmail,
        String token,
        Instant expiresAt
    ) {
        this.team = team;
        this.inviteeUserId = inviteeUserId;
        this.inviteeLoginId = inviteeLoginId;
        this.inviteeDisplayName = inviteeDisplayName;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.expiresAt = expiresAt;
        this.accepted = false;
        this.rejected = false;
    }

    public void accept() {
        this.accepted = true;
        this.rejected = false;
    }

    public void reject() {
        this.rejected = true;
    }

    public boolean isPending() {
        return !accepted && !rejected && expiresAt.isAfter(Instant.now());
    }

    public String getStatus() {
        if (accepted) {
            return "ACCEPTED";
        }
        if (rejected) {
            return "REJECTED";
        }
        if (expiresAt.isBefore(Instant.now())) {
            return "EXPIRED";
        }
        return "PENDING";
    }
}
