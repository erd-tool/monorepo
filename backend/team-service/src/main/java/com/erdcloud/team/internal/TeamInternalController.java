package com.erdcloud.team.internal;

import com.erdcloud.common.ApiException;
import com.erdcloud.common.HeaderConstants;
import com.erdcloud.team.team.TeamDtos;
import com.erdcloud.team.team.TeamMemberRepository;
import com.erdcloud.team.team.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스 간 내부 통신 전용 엔드포인트.
 * Gateway를 통해 외부에 노출되지 않으며, 내부 네트워크에서만 호출됨.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class TeamInternalController {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * erd-service가 Gateway가 전달한 사용자 컨텍스트 기준으로 팀 멤버십/역할을 조회한다.
     */
    @GetMapping("/teams/{teamId}/membership")
    public TeamDtos.TeamAccessResponse getMembership(
        @PathVariable Long teamId,
        @RequestHeader(HeaderConstants.USER_ID) Long userId
    ) {
        var team = teamRepository.findById(teamId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."));
        var member = teamMemberRepository.findByTeamAndUserId(team, userId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "팀 접근 권한이 없습니다."));
        return new TeamDtos.TeamAccessResponse(teamId, userId, member.getRole().name());
    }

    /**
     * erd-service가 Gateway가 전달한 사용자 컨텍스트 기준으로 속한 팀 목록을 조회한다.
     */
    @GetMapping("/users/me/teams")
    public List<Long> getUserTeamIds(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return teamMemberRepository.findByUserId(userId).stream()
            .map(m -> m.getTeam().getId())
            .toList();
    }
}
