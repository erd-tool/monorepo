package com.erdcloud.team.team;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByUserId(Long userId);

    List<TeamMember> findByTeam(Team team);

    Optional<TeamMember> findByTeamAndUserId(Team team, Long userId);

    long countByTeam(Team team);

    boolean existsByTeamAndUserId(Team team, Long userId);

    void deleteAllByTeam(Team team);
}
