package com.erdcloud.erd.erd;

import com.erdcloud.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false, length = 16)
    private String visibility;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contentJson;

    // MSA: UserAccount 대신 userId만 저장
    @Column(nullable = false)
    private Long ownerUserId;

    // MSA: Team 대신 teamId만 저장 (null이면 개인 문서)
    @Column
    private Long ownerTeamId;

    public ErdDocument(String title, String description, String visibility, String contentJson,
        Long ownerUserId, Long ownerTeamId) {
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.contentJson = contentJson;
        this.ownerUserId = ownerUserId;
        this.ownerTeamId = ownerTeamId;
    }

    public void update(String title, String description, String visibility, String contentJson) {
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.contentJson = contentJson;
    }
}
