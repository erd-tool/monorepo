package com.erdcloud.app.erd;

import com.erdcloud.app.team.Team;
import com.erdcloud.app.user.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErdDocumentRepository extends JpaRepository<ErdDocument, Long> {

    List<ErdDocument> findByOwnerUser(UserAccount user);

    List<ErdDocument> findByOwnerTeamIn(List<Team> teams);
}

