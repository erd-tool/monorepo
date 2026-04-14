package com.erdcloud.erd.erd;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

public final class ErdResourceModels {

    private ErdResourceModels() {
    }

    @Getter
    public static class ErdSummaryModel extends RepresentationModel<ErdSummaryModel> {

        private final Long id;
        private final String title;
        private final String description;
        private final String visibility;
        private final Long teamId;
        private final String updatedAt;

        public ErdSummaryModel(
            Long id,
            String title,
            String description,
            String visibility,
            Long teamId,
            String updatedAt
        ) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.visibility = visibility;
            this.teamId = teamId;
            this.updatedAt = updatedAt;
        }
    }

    @Getter
    public static class ErdDetailModel extends RepresentationModel<ErdDetailModel> {

        private final Long id;
        private final String title;
        private final String description;
        private final String visibility;
        private final Long teamId;
        private final String contentJson;

        public ErdDetailModel(
            Long id,
            String title,
            String description,
            String visibility,
            Long teamId,
            String contentJson
        ) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.visibility = visibility;
            this.teamId = teamId;
            this.contentJson = contentJson;
        }
    }
}
