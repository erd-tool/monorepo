package com.erdcloud.team.team;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    Optional<TeamInvitation> findByToken(String token);

    List<TeamInvitation> findByInviteeUserIdOrderByCreatedAtDesc(Long inviteeUserId);

    long countByTeamAndAcceptedFalseAndRejectedFalseAndExpiresAtAfter(Team team, Instant now);

    Optional<TeamInvitation> findByTeamAndInviteeUserIdAndAcceptedFalseAndRejectedFalseAndExpiresAtAfter(
        Team team,
        Long inviteeUserId,
        Instant now
    );

    void deleteAllByTeam(Team team);
}
