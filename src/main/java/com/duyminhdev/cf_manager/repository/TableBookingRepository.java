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
}
