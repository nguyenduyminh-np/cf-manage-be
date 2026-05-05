package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * Tìm sở thích theo userId (username hoặc guest_<sessionId>).
     */
    Optional<UserPreference> findByUserId(String userId);
}
