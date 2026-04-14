package com.erdcloud.erd.erd;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.stereotype.Component;

@Component
public class ErdRepresentationAssembler {

    public ErdResourceModels.ErdSummaryModel toSummaryModel(ErdDtos.ErdSummary summary) {
        ErdResourceModels.ErdSummaryModel model = new ErdResourceModels.ErdSummaryModel(
            summary.id(),
            summary.title(),
            summary.description(),
            summary.visibility(),
            summary.teamId(),
            summary.updatedAt()
        );
        model.add(linkTo(methodOn(ErdController.class).get(null, summary.id())).withSelfRel());
        model.add(linkTo(methodOn(ErdController.class).list(null)).withRel("collection"));
        model.add(linkTo(methodOn(ErdController.class).exportSql(null, summary.id(), "postgresql")).withRel("export-sql"));
        if ("public".equals(summary.visibility())) {
            model.add(linkTo(methodOn(PublicErdController.class).get(summary.id())).withRel("public-view"));
        }
        return model;
    }

    public ErdResourceModels.ErdDetailModel toDetailModel(ErdDtos.ErdDetail detail) {
        ErdResourceModels.ErdDetailModel model = new ErdResourceModels.ErdDetailModel(
            detail.id(),
            detail.title(),
            detail.description(),
            detail.visibility(),
            detail.teamId(),
            detail.contentJson()
        );
        model.add(linkTo(methodOn(ErdController.class).get(null, detail.id())).withSelfRel());
        model.add(linkTo(methodOn(ErdController.class).list(null)).withRel("collection"));
        model.add(linkTo(methodOn(ErdController.class).update(null, detail.id(), null)).withRel("update"));
        model.add(linkTo(methodOn(ErdController.class).delete(null, detail.id())).withRel("delete"));
        model.add(linkTo(methodOn(ErdController.class).exportSql(null, detail.id(), "postgresql")).withRel("export-sql"));
        if ("public".equals(detail.visibility())) {
            model.add(linkTo(methodOn(PublicErdController.class).get(detail.id())).withRel("public-view"));
        }
        return model;
    }

    public ErdResourceModels.ErdDetailModel toPublicDetailModel(ErdDtos.ErdDetail detail) {
        ErdResourceModels.ErdDetailModel model = new ErdResourceModels.ErdDetailModel(
            detail.id(),
            detail.title(),
            detail.description(),
            detail.visibility(),
            detail.teamId(),
            detail.contentJson()
        );
        model.add(linkTo(methodOn(PublicErdController.class).get(detail.id())).withSelfRel());
        return model;
    }
}
