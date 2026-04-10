package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.TableEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    Optional<TableEntity> findByIdAndActiveTrue(Integer id);
    Optional<TableEntity> findByIdAndActiveIsTrue(Integer id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        select t
                        from TableEntity t
                        where t.id = :tableId
                            and t.active = true
                        """)
        Optional<TableEntity> findByIdAndActiveTrueForUpdate(@Param("tableId") Integer tableId);

    List<TableEntity> findAllByActiveTrueOrderByFloorAscSlotAscIdAsc();
    boolean existsByIdAndActiveTrue(Integer id);
}
