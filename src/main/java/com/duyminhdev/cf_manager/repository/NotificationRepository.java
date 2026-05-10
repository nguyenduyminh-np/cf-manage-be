package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Lấy tất cả notification active (global, không phân biệt user), mới nhất trước.
     * Trạng thái đọc của từng user được query riêng từ NotificationReadRepository.
     */
    @Query("""
            SELECT n FROM Notification n
            WHERE n.active = true
            ORDER BY n.createdTime DESC
            """)
    Page<Notification> findAllActive(Pageable pageable);
}
