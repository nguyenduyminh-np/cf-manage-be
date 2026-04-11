package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.TableBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Integer>, JpaSpecificationExecutor<TableBooking> {

    @EntityGraph(attributePaths = {"table", "account"})
    Optional<TableBooking> findByIdAndActiveTrue(Integer id);

    boolean existsByIdAndActiveTrue(Integer id);

    /**
     * Dùng để kiểm tra xem bàn có booking upcoming hợp lệ trong cửa sổ 2 giờ hay không.
     * ServiceSupport sẽ gọi method này để quyết định trạng thái bàn là BOOKED hay không.
     */
    @Query("""
            select case when count(tb) > 0 then true else false end
            from TableBooking tb
            where tb.table.id = :tableId
              and tb.active = true
              and upper(tb.bookingStatus) in :statuses
                                                        and tb.expectedArriveTime >= :fromTime
                                                        and tb.expectedArriveTime <= :toTime
            """)
    boolean existsUpcomingActiveBookingByTableIdAndStatuses(
            @Param("tableId") Integer tableId,
            @Param("statuses")Collection<String> statuses,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime
            );

    @Query("""
            select case when count(tb) > 0 then true else false end
            from TableBooking tb
            where tb.table.id = :tableId
              and tb.active = true
              and upper(tb.bookingStatus) in :statuses
              and tb.expectedArriveTime < :endTime
              and tb.expectedCheckOut > :startTime
              and (:excludeBookingId is null or tb.id <> :excludeBookingId)
            """)
    boolean existsConflictBookingOnTable(
            @Param("tableId") Integer tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeBookingId") Integer excludeBookingId
    );

    default boolean existsConflictBookingOnTable(
            Integer tableId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Collection<String> statuses
    ) {
        return existsConflictBookingOnTable(tableId, startTime, endTime, statuses, null);
    }

    @Query("""
            select case when count(tb) > 0 then true else false end
            from TableBooking tb
            where tb.table.id = :tableId
              and tb.active = true
              and upper(tb.bookingStatus) in :statuses
              and tb.expectedArriveTime >= :fromTime
              and tb.expectedArriveTime <= :toTime
              and (:excludeBookingId is null or tb.id <> :excludeBookingId)
            """)
    boolean existsActiveBookingOnTableInWindowAndStatuses(
            @Param("tableId") Integer tableId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeBookingId") Integer excludeBookingId
    );

    @Query("""
            select case when count(tb) > 0 then true else false end
            from TableBooking tb
            where tb.table.id = :tableId
              and tb.active = true
              and upper(tb.bookingStatus) in :statuses
              and tb.expectedArriveTime >= :fromTime
              and (:excludeBookingId is null or tb.id <> :excludeBookingId)
            """)
    boolean existsActiveBookingOnTableFromTimeAndStatuses(
            @Param("tableId") Integer tableId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("statuses") Collection<String> statuses,
            @Param("excludeBookingId") Integer excludeBookingId
    );

    @Query("""
            select tb
            from TableBooking tb
            where tb.table.id = :tableId
              and tb.active = true
              and upper(tb.bookingStatus) = upper(:status)
              and tb.expectedArriveTime >= :fromTime
              and (:excludeBookingId is null or tb.id <> :excludeBookingId)
            order by tb.expectedArriveTime asc
            """)
    List<TableBooking> findConfirmedBookingsFromTime(
            @Param("tableId") Integer tableId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("status") String status,
            @Param("excludeBookingId") Integer excludeBookingId
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and upper(tb.bookingStatus) = upper('CONFIRMED')
              and tb.expectedArriveTime > :startTime
              and tb.expectedArriveTime <= :endTime
            order by tb.expectedArriveTime asc
            """)
    List<TableBooking> findConfirmedBookingsComingInWindow(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query(value = """
            select tb.*
            from table_booking tb
            where tb.is_active = 1
              and upper(tb.booking_status) = upper('CONFIRMED')
              and tb.dining_table_id = :tableId
              and tb.expected_arrive_time < :endTime
              and tb.expected_check_out > :startTime
            order by tb.expected_arrive_time asc
            """, nativeQuery = true)
    List<TableBooking> findConfirmedBookingsOnTableBetween(
            @Param("tableId") Integer tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query(value = """
            select tb.*
            from table_booking tb
            where tb.is_active = 1
              and upper(tb.booking_status) = upper('CONFIRMED')
              and tb.dining_table_id = :tableId
              and tb.expected_arrive_time >= :afterTime
            order by tb.expected_arrive_time asc
            limit 1
            """, nativeQuery = true)
    List<TableBooking> findNextConfirmedBookingOnTableInternal(
            @Param("tableId") Integer tableId,
            @Param("afterTime") LocalDateTime afterTime
    );

    default Optional<TableBooking> findNextConfirmedBookingOnTable(Integer tableId, LocalDateTime afterTime) {
        return findNextConfirmedBookingOnTableInternal(tableId, afterTime).stream().findFirst();
    }

    @Query(value = """
            select tb.*
            from table_booking tb
            where tb.is_active = 1
              and upper(tb.booking_status) = upper('CONFIRMED')
              and tb.check_in_at is null
              and tb.expected_arrive_time < date_sub(:nowTime, interval 30 minute)
            order by tb.expected_arrive_time asc
            """, nativeQuery = true)
    List<TableBooking> findConfirmedNoShowCandidates(@Param("nowTime") LocalDateTime nowTime);

    @Query(value = """
            select tb.*
            from table_booking tb
            where tb.is_active = 1
              and upper(tb.booking_status) = upper('CHECKED_IN')
              and tb.check_in_at is not null
              and timestampdiff(minute, tb.check_in_at, :nowTime) >= :minutes
              and not exists (
                    select 1
                    from dish_order do1
                    where do1.is_active = 1
                      and do1.dining_table_id = tb.dining_table_id
                      and do1.created_at >= tb.check_in_at
              )
            order by tb.check_in_at asc
            """, nativeQuery = true)
    List<TableBooking> findCheckedInWithoutOrderOlderThan(
            @Param("nowTime") LocalDateTime nowTime,
            @Param("minutes") int minutes
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and tb.table.id = :tableId
              and upper(tb.bookingStatus) = upper('PENDING_CONFIRMATION')
              and tb.expectedArriveTime >= :fromTime
            order by tb.expectedArriveTime asc
            """)
    List<TableBooking> findPendingConfirmationsOnTableAfter(
            @Param("tableId") Integer tableId,
            @Param("fromTime") LocalDateTime fromTime
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and upper(tb.bookingStatus) = upper(:status)
              and tb.expectedArriveTime > :startTime
              and tb.expectedArriveTime <= :endTime
            order by tb.expectedArriveTime asc
            """)
    List<TableBooking> findBookingsByStatusAndExpectedArriveWindow(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and upper(tb.bookingStatus) = upper(:status)
              and tb.checkInAt is null
              and tb.expectedArriveTime < :cutoffTime
            order by tb.expectedArriveTime asc
            """)
    List<TableBooking> findNoShowCandidates(
            @Param("status") String status,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and upper(tb.bookingStatus) = upper(:status)
              and tb.checkInAt is not null
              and tb.checkInAt <= :cutoffTime
            order by tb.checkInAt asc
            """)
    List<TableBooking> findCheckedInByCheckInBefore(
            @Param("status") String status,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    @EntityGraph(attributePaths = {"table", "account"})
    @Query("""
            select tb
            from TableBooking tb
            where tb.active = true
              and upper(tb.bookingStatus) = upper(:status)
              and tb.expectedCheckOut > :startTime
              and tb.expectedCheckOut <= :endTime
            order by tb.expectedCheckOut asc
            """)
    List<TableBooking> findByStatusAndExpectedCheckOutWindow(
            @Param("status") String status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
