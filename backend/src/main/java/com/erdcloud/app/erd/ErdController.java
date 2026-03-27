package com.erdcloud.app.erd;

import com.erdcloud.app.auth.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/erds")
@RequiredArgsConstructor
public class ErdController {

    private final ErdService erdService;

    @GetMapping
    public List<ErdDtos.ErdSummary> list(@AuthenticationPrincipal UserPrincipal principal) {
        return erdService.list(principal);
    }

    @PostMapping
    public ErdDtos.ErdSummary create(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ErdDtos.CreateErdRequest request
    ) {
        return erdService.create(principal, request);
    }

    @GetMapping("/{erdId}")
    public ErdDtos.ErdDetail get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long erdId) {
        return erdService.get(principal, erdId);
    }

    @PatchMapping("/{erdId}")
    public ErdDtos.ErdDetail update(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long erdId,
        @Valid @RequestBody ErdDtos.UpdateErdRequest request
    ) {
        return erdService.update(principal, erdId, request);
    }

    @DeleteMapping("/{erdId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long erdId) {
        erdService.delete(principal, erdId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{erdId}/export/sql")
    public ResponseEntity<String> exportSql(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long erdId,
        @RequestParam(defaultValue = "postgresql") String dialect
    ) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=erd-" + erdId + "-" + dialect + ".sql")
            .contentType(MediaType.TEXT_PLAIN)
            .body(erdService.exportSql(principal, erdId, dialect));
    }
}
