package com.erdcloud.erd.erd;

import com.erdcloud.common.HeaderConstants;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
    private final ErdRepresentationAssembler assembler;

    @GetMapping
    public CollectionModel<ErdResourceModels.ErdSummaryModel> list(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        List<ErdResourceModels.ErdSummaryModel> models = erdService.list(userId).stream()
            .map(assembler::toSummaryModel)
            .toList();
        return CollectionModel.of(models, linkTo(methodOn(ErdController.class).list(null)).withSelfRel());
    }

    @PostMapping
    public ErdResourceModels.ErdSummaryModel create(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @Valid @RequestBody ErdDtos.CreateErdRequest request
    ) {
        return assembler.toSummaryModel(erdService.create(userId, request));
    }

    @GetMapping("/{erdId}")
    public ErdResourceModels.ErdDetailModel get(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId
    ) {
        return assembler.toDetailModel(erdService.get(userId, erdId));
    }

    @PatchMapping("/{erdId}")
    public ErdResourceModels.ErdDetailModel update(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long erdId,
        @Valid @RequestBody ErdDtos.UpdateErdRequest request
    ) {
        return assembler.toDetailModel(erdService.update(userId, erdId, request));
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
