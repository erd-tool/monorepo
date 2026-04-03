package com.erdcloud.erd.erd;

import com.erdcloud.common.HeaderConstants;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/erds")
@RequiredArgsConstructor
public class ErdController {

    private final ErdService erdService;

    @GetMapping
    public List<ErdDtos.ErdSummary> list(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return erdService.list(userId);
    }

    @PostMapping
    public ErdDtos.ErdSummary create(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @Valid @RequestBody ErdDtos.CreateErdRequest request
    ) {
        return erdService.create(userId, request);
    }

    @GetMapping("/{erdId}")
    public ErdDtos.ErdDetail get(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId
    ) {
        return erdService.get(userId, erdId);
    }

    @PatchMapping("/{erdId}")
    public ErdDtos.ErdDetail update(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId,
        @Valid @RequestBody ErdDtos.UpdateErdRequest request
    ) {
        return erdService.update(userId, erdId, request);
    }

    @DeleteMapping("/{erdId}")
    public ResponseEntity<Void> delete(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId
    ) {
        erdService.delete(userId, erdId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{erdId}/export/sql")
    public ResponseEntity<String> exportSql(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId,
        @RequestParam(defaultValue = "postgresql") String dialect
    ) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=erd-" + erdId + "-" + dialect + ".sql")
            .contentType(MediaType.TEXT_PLAIN)
            .body(erdService.exportSql(userId, erdId, dialect));
    }
}
