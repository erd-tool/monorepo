package com.erdcloud.erd.erd;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/erds")
@RequiredArgsConstructor
public class PublicErdController {

    private final ErdService erdService;
    private final ErdRepresentationAssembler assembler;

    @GetMapping("/{erdId}")
    public ErdResourceModels.ErdDetailModel get(@PathVariable Long erdId) {
        return assembler.toPublicDetailModel(erdService.getPublic(erdId));
    }
}
