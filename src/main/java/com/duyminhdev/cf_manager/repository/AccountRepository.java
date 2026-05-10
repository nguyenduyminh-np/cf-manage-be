package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Dùng cho NotificationServiceImpl.saveFromWsEvent().
     * Lấy id + role của account active — không load toàn bộ fields không cần thiết.
     */
    @Query("SELECT a FROM Account a JOIN FETCH a.role WHERE a.active = true")
    List<Account> findAllActive();
}
