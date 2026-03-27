package com.erdcloud.app.team;

import com.erdcloud.app.common.BaseEntity;
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

    @Column(nullable = false, length = 120)
    private String inviteeEmail;

    @Column(nullable = false, unique = true, length = 80)
    private String token;

    @Column(nullable = false)
    private boolean accepted;

    @Column(nullable = false)
    private Instant expiresAt;

    public TeamInvitation(Team team, String inviteeEmail, String token, Instant expiresAt) {
        this.team = team;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.expiresAt = expiresAt;
        this.accepted = false;
    }

    public void accept() {
        this.accepted = true;
    }
}

