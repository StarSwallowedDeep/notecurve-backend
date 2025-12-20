package com.notecurve.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
