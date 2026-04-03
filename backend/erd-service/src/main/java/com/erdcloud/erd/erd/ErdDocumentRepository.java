package com.erdcloud.erd.erd;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErdDocumentRepository extends JpaRepository<ErdDocument, Long> {

    List<ErdDocument> findByOwnerUserId(Long userId);

    List<ErdDocument> findByOwnerTeamIdIn(List<Long> teamIds);

    void deleteAllByOwnerTeamId(Long teamId);
}
