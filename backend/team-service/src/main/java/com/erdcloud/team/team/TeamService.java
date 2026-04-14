package com.erdcloud.team.team;

import com.erdcloud.common.ApiException;
import com.erdcloud.team.client.AuthServiceClient;
import com.erdcloud.team.client.ErdServiceClient;
import feign.FeignException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final AuthServiceClient authServiceClient;
    private final ErdServiceClient erdServiceClient;

    @Transactional
    public TeamDtos.TeamSummary create(Long userId, TeamDtos.CreateTeamRequest request) {
        Team team = teamRepository.save(new Team(request.name(), request.description()));
        teamMemberRepository.save(new TeamMember(team, userId, TeamRole.OWNER));
        return toTeamSummary(team, TeamRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<TeamDtos.TeamSummary> list(Long userId) {
        return teamMemberRepository.findByUserId(userId).stream()
            .map(member -> toTeamSummary(member.getTeam(), member.getRole()))
            .toList();
    }

    @Transactional(readOnly = true)
    public TeamDtos.TeamSummary get(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
        TeamMember member = teamMemberRepository.findByTeamAndUserId(team, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."));
        return toTeamSummary(team, member.getRole());
    }

    @Transactional
    public TeamDtos.TeamInvitationResponse invite(Long userId, Long teamId, TeamDtos.InviteMemberRequest request) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
        TeamMember member = teamMemberRepository.findByTeamAndUserId(team, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."));
        if (member.getRole() != TeamRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "초대는 OWNER만 가능합니다.");
        }

        AuthServiceClient.AuthUserSummary invitee;
        try {
            invitee = authServiceClient.findByLoginId(request.loginId());
        } catch (FeignException.NotFound e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "초대할 아이디를 찾을 수 없습니다.");
        } catch (FeignException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "사용자 정보를 확인하지 못했습니다.");
        }

        if (invitee.id().equals(userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "본인은 초대할 수 없습니다.");
        }
        if (teamMemberRepository.existsByTeamAndUserId(team, invitee.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 팀에 참여 중인 사용자입니다.");
        }
        if (teamInvitationRepository.findByTeamAndInviteeUserIdAndAcceptedFalseAndRejectedFalseAndExpiresAtAfter(team, invitee.id(), Instant.now()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 초대된 사용자입니다.");
        }

        TeamInvitation invitation = teamInvitationRepository.save(new TeamInvitation(
            team,
            invitee.id(),
            invitee.loginId(),
            invitee.displayName(),
            invitee.email(),
            UUID.randomUUID().toString(),
            Instant.now().plusSeconds(60L * 60L * 24L * 7L)
        ));
        return toInvitationResponse(invitation);
    }

    @Transactional(readOnly = true)
    public List<TeamDtos.TeamInvitationResponse> listInvitations(Long userId) {
        return teamInvitationRepository.findByInviteeUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toInvitationResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TeamDtos.TeamInvitationResponse getInvitation(Long userId, Long invitationId) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."));
        validateInvitationOwnership(userId, invitation);
        return toInvitationResponse(invitation);
    }

    @Transactional
    public TeamDtos.TeamSummary accept(Long userId, Long invitationId) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."));
        validateInvitationOwnership(userId, invitation);
        validateInvitationPending(invitation);
        if (!teamMemberRepository.existsByTeamAndUserId(invitation.getTeam(), userId)) {
            teamMemberRepository.save(new TeamMember(invitation.getTeam(), userId, TeamRole.MEMBER));
        }
        invitation.accept();
        return toTeamSummary(invitation.getTeam(), TeamRole.MEMBER);
    }

    @Transactional
    public TeamDtos.TeamSummary accept(Long userId, String token) {
        TeamInvitation invitation = teamInvitationRepository.findByToken(token)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."));
        validateInvitationOwnership(userId, invitation);
        validateInvitationPending(invitation);
        if (!teamMemberRepository.existsByTeamAndUserId(invitation.getTeam(), userId)) {
            teamMemberRepository.save(new TeamMember(invitation.getTeam(), userId, TeamRole.MEMBER));
        }
        invitation.accept();
        return toTeamSummary(invitation.getTeam(), TeamRole.MEMBER);
    }

    @Transactional
    public void reject(Long userId, Long invitationId) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."));
        validateInvitationOwnership(userId, invitation);
        validateInvitationPending(invitation);
        invitation.reject();
    }

    @Transactional
    public void delete(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
        TeamMember member = teamMemberRepository.findByTeamAndUserId(team, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."));
        if (member.getRole() != TeamRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "팀 삭제는 OWNER만 가능합니다.");
        }

        try {
            erdServiceClient.deleteTeamDocuments(teamId);
        } catch (FeignException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "팀 문서를 정리하지 못해 팀 삭제에 실패했습니다.");
        }

        teamInvitationRepository.deleteAllByTeam(team);
        teamMemberRepository.deleteAllByTeam(team);
        teamRepository.delete(team);
    }

    private TeamDtos.TeamSummary toTeamSummary(Team team, TeamRole role) {
        return new TeamDtos.TeamSummary(
            team.getId(),
            team.getName(),
            team.getDescription(),
            role.name(),
            teamMemberRepository.countByTeam(team),
            teamInvitationRepository.countByTeamAndAcceptedFalseAndRejectedFalseAndExpiresAtAfter(team, Instant.now()),
            team.getUpdatedAt()
        );
    }

    private TeamDtos.TeamInvitationResponse toInvitationResponse(TeamInvitation invitation) {
        return new TeamDtos.TeamInvitationResponse(
            invitation.getId(),
            invitation.getTeam().getId(),
            invitation.getTeam().getName(),
            invitation.getInviteeLoginId(),
            invitation.getInviteeDisplayName(),
            invitation.getStatus(),
            invitation.getExpiresAt(),
            invitation.getCreatedAt()
        );
    }

    private void validateInvitationOwnership(Long userId, TeamInvitation invitation) {
        if (!invitation.getInviteeUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "본인에게 온 초대만 처리할 수 있습니다.");
        }
    }

    private void validateInvitationPending(TeamInvitation invitation) {
        if (invitation.isAccepted()) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 수락된 초대입니다.");
        }
        if (invitation.isRejected()) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 거절된 초대입니다.");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.GONE, "만료된 초대입니다.");
        }
    }
}
