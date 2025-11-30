package com.notecurve.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.notecurve.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByLoginId(String loginId);
    boolean existsByName(String name);

    Optional<User> findByLoginId(String loginId);

    default User findByLoginIdOrThrow(String loginId) {
        return findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with loginId: " + loginId));
    }
}
