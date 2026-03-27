package com.erdcloud.app.erd;

import com.erdcloud.app.auth.UserPrincipal;
import com.erdcloud.app.common.ApiException;
import com.erdcloud.app.team.Team;
import com.erdcloud.app.team.TeamMember;
import com.erdcloud.app.team.TeamMemberRepository;
import com.erdcloud.app.team.TeamRepository;
import com.erdcloud.app.user.UserAccount;
import com.erdcloud.app.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ErdService {

    private static final String EMPTY_DOCUMENT = """
        {"entities":[],"relationships":[],"notes":[]}
        """;

    private final ErdDocumentRepository erdDocumentRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SqlExportService sqlExportService;

    @Transactional
    public ErdDtos.ErdSummary create(UserPrincipal principal, ErdDtos.CreateErdRequest request) {
        UserAccount user = getUser(principal.id());
        Team ownerTeam = null;
        if (request.teamId() != null) {
            ownerTeam = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
            ensureTeamAccess(user, ownerTeam);
        }

        ErdDocument document = erdDocumentRepository.save(new ErdDocument(
            request.title(),
            request.description(),
            EMPTY_DOCUMENT,
            user,
            ownerTeam
        ));
        return toSummary(document);
    }

    @Transactional(readOnly = true)
    public List<ErdDtos.ErdSummary> list(UserPrincipal principal) {
        UserAccount user = getUser(principal.id());
        List<ErdDtos.ErdSummary> results = new ArrayList<>();
        erdDocumentRepository.findByOwnerUser(user).stream().map(this::toSummary).forEach(results::add);
        List<Team> teams = teamMemberRepository.findByUser(user).stream().map(TeamMember::getTeam).toList();
        erdDocumentRepository.findByOwnerTeamIn(teams).stream().map(this::toSummary).forEach(results::add);
        return results.stream().distinct().toList();
    }

    @Transactional(readOnly = true)
    public ErdDtos.ErdDetail get(UserPrincipal principal, Long erdId) {
        ErdDocument document = getAccessibleDocument(principal.id(), erdId);
        return new ErdDtos.ErdDetail(
            document.getId(),
            document.getTitle(),
            document.getDescription(),
            document.getOwnerTeam() != null ? document.getOwnerTeam().getId() : null,
            document.getOwnerTeam() != null ? document.getOwnerTeam().getName() : null,
            document.getContentJson()
        );
    }

    @Transactional
    public ErdDtos.ErdDetail update(UserPrincipal principal, Long erdId, ErdDtos.UpdateErdRequest request) {
        ErdDocument document = getAccessibleDocument(principal.id(), erdId);
        document.update(request.title(), request.description(), request.contentJson());
        return new ErdDtos.ErdDetail(
            document.getId(),
            document.getTitle(),
            document.getDescription(),
            document.getOwnerTeam() != null ? document.getOwnerTeam().getId() : null,
            document.getOwnerTeam() != null ? document.getOwnerTeam().getName() : null,
            document.getContentJson()
        );
    }

    @Transactional
    public void delete(UserPrincipal principal, Long erdId) {
        ErdDocument document = getAccessibleDocument(principal.id(), erdId);
        erdDocumentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public String exportSql(UserPrincipal principal, Long erdId, String dialect) {
        ErdDocument document = getAccessibleDocument(principal.id(), erdId);
        return sqlExportService.export(document.getContentJson(), dialect);
    }

    private ErdDocument getAccessibleDocument(Long userId, Long erdId) {
        UserAccount user = getUser(userId);
        ErdDocument document = erdDocumentRepository.findById(erdId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ERD를 찾을 수 없습니다."));

        boolean accessible = document.getOwnerUser().getId().equals(user.getId());
        if (!accessible && document.getOwnerTeam() != null) {
            accessible = teamMemberRepository.existsByTeamAndUser(document.getOwnerTeam(), user);
        }
        if (!accessible) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ERD 접근 권한이 없습니다.");
        }
        return document;
    }

    private void ensureTeamAccess(UserAccount user, Team team) {
        if (!teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다.");
        }
    }

    private UserAccount getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private ErdDtos.ErdSummary toSummary(ErdDocument document) {
        return new ErdDtos.ErdSummary(
            document.getId(),
            document.getTitle(),
            document.getDescription(),
            document.getOwnerTeam() != null ? document.getOwnerTeam().getId() : null,
            document.getOwnerTeam() != null ? document.getOwnerTeam().getName() : null,
            document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : Instant.now().toString()
        );
    }
}

