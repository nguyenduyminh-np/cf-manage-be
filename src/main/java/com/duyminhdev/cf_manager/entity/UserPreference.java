package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Lưu sở thích / hành vi của người dùng chatbot để cá nhân hóa câu trả lời.
 * Mỗi userId có tối đa 1 bản ghi (unique).
 */
@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_pref_user_id", columnList = "user_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Username của nhân viên, hoặc "guest_<sessionId>" cho khách.
     */
    @Column(name = "user_id", nullable = false, unique = true, length = 255)
    private String userId;

    /**
     * Loại đồ uống yêu thích (VD: "cà phê", "trà", "freeze").
     */
    @Column(name = "preferred_dish_type", length = 100)
    private String preferredDishType;

    /**
     * Tầng hay ngồi (1, 2, 3...).
     */
    @Column(name = "preferred_table_floor")
    private Integer preferredTableFloor;

    /**
     * Mức giá ưa thích: "LOW" (<50k), "MEDIUM" (50k-100k), "HIGH" (>100k).
     */
    @Column(name = "preferred_price_range", length = 20)
    private String preferredPriceRange;

    /**
     * Tổng số lần đặt bàn qua chatbot.
     */
    @Builder.Default
    @Column(name = "total_bookings", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer totalBookings = 0;

    /**
     * ID bàn đã đặt lần gần nhất.
     */
    @Column(name = "last_booked_table_id")
    private Long lastBookedTableId;

    /**
     * Ghi chú tùy do nhân viên / hệ thống cập nhật.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
