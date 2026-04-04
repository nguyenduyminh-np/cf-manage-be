package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.DishOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DishOrderRepository extends JpaRepository<DishOrder, Integer> {

    @EntityGraph(attributePaths = {"table", "account", "status"})
    List<DishOrder> findAllByTableIdAndActiveTrueOrderByCreatedTimeDesc(Integer tableId);

    @EntityGraph(attributePaths = {"table", "account", "status"})
    Optional<DishOrder> findByIdAndActiveTrue(Integer id);

    /**
     * Kiểm tra bàn còn order unfinished hay không.
     * "unfinished" = status KHÔNG thuộc CANCELLED, DONE.
     */
    @Query("""
            select case when count(do1) > 0 then true else false end
            from DishOrder do1
            join do1.status s
            where do1.table.id = :tableId
              and do1.active = true
              and upper(s.dishOrderStatusCode) not in :finishedStatusCodes
            """)
    boolean existsUnfinishedOrdersByTableId(
            @Param("tableId") Integer tableId,
            @Param("finishedStatusCodes") Collection<String> finishedStatusCodes
    );

    /**
     * Lấy toàn bộ order unfinished để dùng khi confirm payment/finalize payment.
     */
    @Query("""
            select do1
            from DishOrder do1
            join fetch do1.table t
            join fetch do1.account a
            join fetch do1.status s
            where do1.table.id = :tableId
              and do1.active = true
              and upper(s.dishOrderStatusCode) not in :finishedStatusCodes
            order by do1.createdTime desc
            """)
    List<DishOrder> findAllUnfinishedOrdersByTableId(
            @Param("tableId") Integer tableId,
            @Param("finishedStatusCodes") Collection<String> finishedStatusCodes
    );
}
