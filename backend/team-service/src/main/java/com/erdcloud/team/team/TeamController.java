package com.erdcloud.team.team;

import com.erdcloud.common.HeaderConstants;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamRepresentationAssembler assembler;

    @GetMapping
    public CollectionModel<TeamResourceModels.TeamSummaryModel> list(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        List<TeamResourceModels.TeamSummaryModel> models = teamService.list(userId).stream()
            .map(assembler::toSummaryModel)
            .toList();
        return CollectionModel.of(models, linkTo(methodOn(TeamController.class).list(null)).withSelfRel());
    }

    @GetMapping("/{teamId}")
    public TeamResourceModels.TeamSummaryModel get(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long teamId
    ) {
        return assembler.toSummaryModel(teamService.get(userId, teamId));
    }

    @PostMapping
    public TeamResourceModels.TeamSummaryModel create(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @Valid @RequestBody TeamDtos.CreateTeamRequest request
    ) {
        return assembler.toSummaryModel(teamService.create(userId, request));
    }

    @PostMapping("/{teamId}/invitations")
    public TeamResourceModels.TeamInvitationModel invite(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long teamId,
        @Valid @RequestBody TeamDtos.InviteMemberRequest request
    ) {
        return assembler.toInvitationModel(teamService.invite(userId, teamId, request));
    }

    @GetMapping("/invitations")
    public CollectionModel<TeamResourceModels.TeamInvitationModel> listInvitations(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        List<TeamResourceModels.TeamInvitationModel> models = teamService.listInvitations(userId).stream()
            .map(assembler::toInvitationModel)
            .toList();
        return CollectionModel.of(models, linkTo(methodOn(TeamController.class).listInvitations(null)).withSelfRel());
    }

    @GetMapping("/invitations/{invitationId}")
    public TeamResourceModels.TeamInvitationModel getInvitation(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long invitationId
    ) {
        return assembler.toInvitationModel(teamService.getInvitation(userId, invitationId));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public TeamResourceModels.TeamSummaryModel accept(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long invitationId
    ) {
        return assembler.toSummaryModel(teamService.accept(userId, invitationId));
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> reject(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long invitationId
    ) {
        teamService.reject(userId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/token/{token}/accept")
    public TeamResourceModels.TeamSummaryModel accept(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable String token
    ) {
        return assembler.toSummaryModel(teamService.accept(userId, token));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> delete(
        @RequestHeader(HeaderConstants.USER_ID) Long userId,
        @PathVariable Long teamId
    ) {
        teamService.delete(userId, teamId);
        return ResponseEntity.noContent().build();
    }
}
