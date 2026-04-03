package com.erdcloud.auth.auth;

import com.erdcloud.auth.user.UserAccount;
import com.erdcloud.auth.user.UserRepository;
import com.erdcloud.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        UserAccount user = userRepository.save(new UserAccount(
            request.loginId(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.displayName()
        ));

        UserPrincipal principal = UserPrincipal.from(user);
        return new AuthDtos.AuthResponse(
            jwtService.generateAccessToken(principal),
            principal.id(),
            principal.loginId(),
            principal.displayName()
        );
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.loginId(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return new AuthDtos.AuthResponse(
            jwtService.generateAccessToken(principal),
            principal.id(),
            principal.loginId(),
            principal.displayName()
        );
    }

    @Transactional(readOnly = true)
    public AuthDtos.MeResponse me(Long userId) {
        UserAccount user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new AuthDtos.MeResponse(user.getId(), user.getLoginId(), user.getEmail(), user.getDisplayName());
    }
}
