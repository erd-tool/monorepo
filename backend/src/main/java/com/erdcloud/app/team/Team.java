package com.erdcloud.app.team;

import com.erdcloud.app.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    public Team(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

