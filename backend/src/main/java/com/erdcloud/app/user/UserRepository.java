package com.erdcloud.app.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByLoginId(String loginId);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);
}

