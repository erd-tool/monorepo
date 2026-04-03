package com.erdcloud.erd.internal;

import com.erdcloud.common.HeaderConstants;
import com.erdcloud.erd.erd.ErdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/erds")
@RequiredArgsConstructor
public class ErdInternalController {

    private final ErdService erdService;

    @GetMapping("/{erdId}/access")
    public ResponseEntity<Void> verifyReadableAccess(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId
    ) {
        erdService.verifyReadableAccess(userId, erdId);
        return ResponseEntity.noContent().build();
    }
}
