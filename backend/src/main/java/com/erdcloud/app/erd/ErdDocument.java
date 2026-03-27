package com.erdcloud.app.erd;

import com.erdcloud.app.common.BaseEntity;
import com.erdcloud.app.team.Team;
import com.erdcloud.app.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erd_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErdDocument extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 255)
    private String description;

    @Lob
    @Column(nullable = false)
    private String contentJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id")
    private UserAccount ownerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_team_id")
    private Team ownerTeam;

    public ErdDocument(String title, String description, String contentJson, UserAccount ownerUser, Team ownerTeam) {
        this.title = title;
        this.description = description;
        this.contentJson = contentJson;
        this.ownerUser = ownerUser;
        this.ownerTeam = ownerTeam;
    }

    public void update(String title, String description, String contentJson) {
        this.title = title;
        this.description = description;
        this.contentJson = contentJson;
    }
}

