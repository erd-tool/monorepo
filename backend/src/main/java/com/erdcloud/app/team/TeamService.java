package com.erdcloud.app.team;

import com.erdcloud.app.auth.UserPrincipal;
import com.erdcloud.app.common.ApiException;
import com.erdcloud.app.user.UserAccount;
import com.erdcloud.app.user.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public TeamDtos.TeamSummary create(UserPrincipal principal, TeamDtos.CreateTeamRequest request) {
        UserAccount user = getUser(principal.id());
        Team team = teamRepository.save(new Team(request.name(), request.description()));
        teamMemberRepository.save(new TeamMember(team, user, TeamRole.OWNER));
        return new TeamDtos.TeamSummary(team.getId(), team.getName(), team.getDescription(), TeamRole.OWNER.name());
    }

    @Transactional(readOnly = true)
    public List<TeamDtos.TeamSummary> list(UserPrincipal principal) {
        UserAccount user = getUser(principal.id());
        return teamMemberRepository.findByUser(user).stream()
            .map(member -> new TeamDtos.TeamSummary(
                member.getTeam().getId(),
                member.getTeam().getName(),
                member.getTeam().getDescription(),
                member.getRole().name()
            ))
            .toList();
    }

    @Transactional
    public TeamDtos.TeamInvitationResponse invite(UserPrincipal principal, Long teamId, TeamDtos.InviteMemberRequest request) {
        UserAccount user = getUser(principal.id());
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."));
        if (member.getRole() != TeamRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "초대는 OWNER만 가능합니다.");
        }

        TeamInvitation invitation = teamInvitationRepository.save(new TeamInvitation(
            team,
            request.email(),
            UUID.randomUUID().toString(),
            Instant.now().plusSeconds(60L * 60L * 24L * 7L)
        ));
        return new TeamDtos.TeamInvitationResponse(
            invitation.getId(),
            invitation.getInviteeEmail(),
            invitation.getToken(),
            team.getName()
        );
    }

    @Transactional
    public TeamDtos.TeamSummary accept(UserPrincipal principal, String token) {
        UserAccount user = getUser(principal.id());
        TeamInvitation invitation = teamInvitationRepository.findByToken(token)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다."));
        if (invitation.isAccepted()) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 수락된 초대입니다.");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.GONE, "만료된 초대입니다.");
        }
        if (!invitation.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "초대된 이메일 계정만 수락할 수 있습니다.");
        }
        if (!teamMemberRepository.existsByTeamAndUser(invitation.getTeam(), user)) {
            teamMemberRepository.save(new TeamMember(invitation.getTeam(), user, TeamRole.MEMBER));
        }
        invitation.accept();
        return new TeamDtos.TeamSummary(
            invitation.getTeam().getId(),
            invitation.getTeam().getName(),
            invitation.getTeam().getDescription(),
            TeamRole.MEMBER.name()
        );
    }

    public boolean hasAccess(UserAccount user, Team team) {
        return teamMemberRepository.existsByTeamAndUser(team, user);
    }

    public UserAccount getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}

