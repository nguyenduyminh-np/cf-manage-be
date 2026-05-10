package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    /**
     * Lấy tập notification_id mà account đã đọc trong danh sách cho trước.
     * Dùng để map trạng thái đọc khi trả về DTO.
     */
    @Query("""
            SELECT nr.notification.id
            FROM NotificationRead nr
            WHERE nr.account.id = :accountId
              AND nr.notification.id IN :notifIds
            """)
    Set<Integer> findReadIdsByAccountIdAndNotifIds(
            @Param("accountId") Integer accountId,
            @Param("notifIds") List<Integer> notifIds);

    /**
     * Đánh dấu 1 notification đã đọc.
     * INSERT IGNORE → không lỗi nếu đã đọc trước đó (duplicate key).
     */
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO notification_read (notification_id, account_id, read_at)
            VALUES (:notifId, :accountId, NOW())
            """, nativeQuery = true)
    void markReadById(@Param("notifId") Integer notifId, @Param("accountId") Integer accountId);

    /**
     * Đánh dấu TẤT CẢ notification chưa đọc của account → đã đọc.
     * Dùng INSERT IGNORE ... SELECT để chỉ insert những bản ghi còn thiếu.
     */
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO notification_read (notification_id, account_id, read_at)
            SELECT n.id, :accountId, NOW()
            FROM notification n
            WHERE n.is_active = 1
              AND NOT EXISTS (
                  SELECT 1 FROM notification_read nr
                  WHERE nr.notification_id = n.id
                    AND nr.account_id = :accountId
              )
            """, nativeQuery = true)
    int markAllReadByAccountId(@Param("accountId") Integer accountId);
}
