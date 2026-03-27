package com.erdcloud.app.team;

import com.erdcloud.app.common.BaseEntity;
import com.erdcloud.app.user.UserAccount;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    private TeamRole role;

    public TeamMember(Team team, UserAccount user, TeamRole role) {
        this.team = team;
        this.user = user;
        this.role = role;
    }
}

