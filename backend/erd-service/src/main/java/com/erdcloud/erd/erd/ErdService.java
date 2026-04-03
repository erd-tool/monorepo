package com.erdcloud.erd.erd;

import com.erdcloud.common.ApiException;
import com.erdcloud.erd.client.TeamServiceClient;
import feign.FeignException;
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
    private final TeamServiceClient teamServiceClient;
    private final SqlExportService sqlExportService;

    @Transactional
    public ErdDtos.ErdSummary create(Long userId, ErdDtos.CreateErdRequest request) {
        if (request.teamId() != null) {
            ensureTeamAccess(request.teamId(), userId);
        }

        ErdDocument document = erdDocumentRepository.save(new ErdDocument(
            request.title(),
            request.description(),
            request.visibility() != null ? request.visibility() : "private",
            EMPTY_DOCUMENT,
            userId,
            request.teamId()
        ));
        return toSummary(document);
    }

    @Transactional(readOnly = true)
    public List<ErdDtos.ErdSummary> list(Long userId) {
        List<ErdDtos.ErdSummary> results = new ArrayList<>();
        erdDocumentRepository.findByOwnerUserId(userId).stream().map(this::toSummary).forEach(results::add);

        try {
            List<Long> teamIds = teamServiceClient.getUserTeamIds();
            if (!teamIds.isEmpty()) {
                erdDocumentRepository.findByOwnerTeamIdIn(teamIds).stream().map(this::toSummary).forEach(results::add);
            }
        } catch (FeignException e) {
            // team-service가 일시적으로 불안정해도 개인 ERD 목록은 조회 가능해야 한다.
        }
        return results.stream().distinct().toList();
    }

    @Transactional(readOnly = true)
    public ErdDtos.ErdDetail get(Long userId, Long erdId) {
        ErdDocument document = getReadableDocument(userId, erdId);
        return toDetail(document);
    }

    @Transactional
    public ErdDtos.ErdDetail update(Long userId, Long erdId, ErdDtos.UpdateErdRequest request) {
        ErdDocument document = getWritableDocument(userId, erdId);
        document.update(
            request.title(),
            request.description(),
            request.visibility() != null ? request.visibility() : document.getVisibility(),
            request.contentJson()
        );
        return toDetail(document);
    }

    @Transactional
    public void delete(Long userId, Long erdId) {
        ErdDocument document = getWritableDocument(userId, erdId);
        erdDocumentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public String exportSql(Long userId, Long erdId, String dialect) {
        ErdDocument document = getReadableDocument(userId, erdId);
        return sqlExportService.export(document.getContentJson(), dialect);
    }

    @Transactional(readOnly = true)
    public void verifyReadableAccess(Long userId, Long erdId) {
        getReadableDocument(userId, erdId);
    }

    private ErdDocument getReadableDocument(Long userId, Long erdId) {
        ErdDocument document = erdDocumentRepository.findById(erdId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ERD를 찾을 수 없습니다."));

        boolean accessible = document.getOwnerUserId().equals(userId);
        if (!accessible && document.getOwnerTeamId() != null) {
            accessible = hasTeamAccess(document.getOwnerTeamId(), userId);
        }
        if (!accessible) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ERD 접근 권한이 없습니다.");
        }
        return document;
    }

    private ErdDocument getWritableDocument(Long userId, Long erdId) {
        ErdDocument document = getReadableDocument(userId, erdId);
        if (document.getOwnerUserId().equals(userId)) {
            return document;
        }
        if (document.getOwnerTeamId() != null && hasTeamOwnerAccess(document.getOwnerTeamId(), userId)) {
            return document;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "ERD 수정 권한이 없습니다.");
    }

    private boolean hasTeamAccess(Long teamId, Long userId) {
        return resolveTeamRole(teamId, userId) != null;
    }

    private boolean hasTeamOwnerAccess(Long teamId, Long userId) {
        return "OWNER".equals(resolveTeamRole(teamId, userId));
    }

    private void ensureTeamAccess(Long teamId, Long userId) {
        if (!hasTeamAccess(teamId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다.");
        }
    }

    private String resolveTeamRole(Long teamId, Long userId) {
        try {
            var membership = teamServiceClient.getMembership(teamId);
            if (membership == null || membership.userId() == null || !membership.userId().equals(userId)) {
                return null;
            }
            return membership.role();
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            return null;
        }
    }

    private ErdDtos.ErdSummary toSummary(ErdDocument document) {
        return new ErdDtos.ErdSummary(
            document.getId(),
            document.getTitle(),
            document.getDescription(),
            document.getVisibility(),
            document.getOwnerTeamId(),
            document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : Instant.now().toString()
        );
    }

    private ErdDtos.ErdDetail toDetail(ErdDocument document) {
        return new ErdDtos.ErdDetail(
            document.getId(),
            document.getTitle(),
            document.getDescription(),
            document.getVisibility(),
            document.getOwnerTeamId(),
            document.getContentJson()
        );
    }
}
