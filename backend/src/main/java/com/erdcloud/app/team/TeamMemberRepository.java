package com.erdcloud.app.team;

import com.erdcloud.app.user.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByUser(UserAccount user);

    List<TeamMember> findByTeam(Team team);

    Optional<TeamMember> findByTeamAndUser(Team team, UserAccount user);

    boolean existsByTeamAndUser(Team team, UserAccount user);
}

