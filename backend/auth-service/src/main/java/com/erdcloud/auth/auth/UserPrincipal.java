package com.erdcloud.auth.auth;

import com.erdcloud.auth.user.UserAccount;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(Long id, String loginId, String email, String password, String displayName)
    implements UserDetails {

    public static UserPrincipal from(UserAccount user) {
        return new UserPrincipal(
            user.getId(), user.getLoginId(), user.getEmail(), user.getPasswordHash(), user.getDisplayName()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }
}
