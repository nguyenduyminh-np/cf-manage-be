package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.InvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.seqDate = :date")
    Optional<InvoiceSequence> findByDateForUpdate(@Param("date") LocalDate date);
}
