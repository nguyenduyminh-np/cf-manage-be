package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Integer> {
}