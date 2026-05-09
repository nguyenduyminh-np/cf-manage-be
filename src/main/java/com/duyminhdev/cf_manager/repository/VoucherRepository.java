package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    Optional<Voucher> findByCodeIgnoreCaseAndActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    /**
     * Tăng usedCount bằng native UPDATE atomic để chống race condition.
     *
     * <p>Điều kiện thực thi:
     * <ul>
     *   <li>Voucher đang active</li>
     *   <li>usedCount < usageLimit (hoặc usageLimit IS NULL = không giới hạn)</li>
     *   <li>Còn trong khoảng thời gian hiệu lực (hoặc ngày không giới hạn)</li>
     * </ul>
     *
     * @return số dòng bị ảnh hưởng — nếu {@code 0} → voucher hết lượt / không hợp lệ
     */
    @Modifying
    @Query(value = """
            UPDATE voucher
            SET    used_count = used_count + 1
            WHERE  id         = :id
              AND  is_active  = 1
              AND  (usage_limit IS NULL OR used_count < usage_limit)
              AND  (start_date IS NULL OR start_date <= NOW())
              AND  (end_date   IS NULL OR end_date   >= NOW())
            """, nativeQuery = true)
    int incrementUsedCount(@Param("id") Integer id);

    /**
     * Giảm usedCount khi hoàn voucher (hủy đơn).
     * Đảm bảo không xuống dưới 0.
     *
     * @return số dòng bị ảnh hưởng
     */
    @Modifying
    @Query(value = """
            UPDATE voucher
            SET    used_count = used_count - 1
            WHERE  id         = :id
              AND  used_count > 0
            """, nativeQuery = true)
    int decrementUsedCount(@Param("id") Integer id);
}
