package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, Integer> {
}
