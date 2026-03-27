package com.erdcloud.app.team;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    Optional<TeamInvitation> findByToken(String token);
}

