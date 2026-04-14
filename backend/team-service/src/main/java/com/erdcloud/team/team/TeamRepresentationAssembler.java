package com.erdcloud.team.team;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.stereotype.Component;

@Component
public class TeamRepresentationAssembler {

    public TeamResourceModels.TeamSummaryModel toSummaryModel(TeamDtos.TeamSummary summary) {
        TeamResourceModels.TeamSummaryModel model = new TeamResourceModels.TeamSummaryModel(
            summary.id(),
            summary.name(),
            summary.description(),
            summary.role(),
            summary.memberCount(),
            summary.invitationCount(),
            summary.updatedAt()
        );
        model.add(linkTo(methodOn(TeamController.class).get(null, summary.id())).withSelfRel());
        model.add(linkTo(methodOn(TeamController.class).list(null)).withRel("collection"));
        model.add(linkTo(methodOn(TeamController.class).invite(null, summary.id(), null)).withRel("invite"));
        model.add(linkTo(methodOn(TeamController.class).delete(null, summary.id())).withRel("delete"));
        return model;
    }

    public TeamResourceModels.TeamInvitationModel toInvitationModel(TeamDtos.TeamInvitationResponse invitation) {
        TeamResourceModels.TeamInvitationModel model = new TeamResourceModels.TeamInvitationModel(
            invitation.id(),
            invitation.teamId(),
            invitation.teamName(),
            invitation.inviteeLoginId(),
            invitation.inviteeDisplayName(),
            invitation.status(),
            invitation.expiresAt(),
            invitation.createdAt()
        );
        model.add(linkTo(methodOn(TeamController.class).getInvitation(null, invitation.id())).withSelfRel());
        model.add(linkTo(methodOn(TeamController.class).listInvitations(null)).withRel("collection"));
        if ("PENDING".equals(invitation.status())) {
            model.add(linkTo(methodOn(TeamController.class).accept(null, invitation.id())).withRel("accept"));
            model.add(linkTo(methodOn(TeamController.class).reject(null, invitation.id())).withRel("reject"));
        }
        return model;
    }
}
