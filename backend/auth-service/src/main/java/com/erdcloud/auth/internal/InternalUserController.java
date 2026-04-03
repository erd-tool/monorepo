package com.erdcloud.auth.internal;

import com.erdcloud.auth.auth.AuthDtos;
import com.erdcloud.auth.user.UserAccount;
import com.erdcloud.auth.user.UserRepository;
import com.erdcloud.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;

    @GetMapping("/by-login-id")
    public AuthDtos.UserLookupResponse findByLoginId(@RequestParam String loginId) {
        UserAccount user = userRepository.findByLoginId(loginId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new AuthDtos.UserLookupResponse(
            user.getId(),
            user.getLoginId(),
            user.getEmail(),
            user.getDisplayName()
        );
    }
}
