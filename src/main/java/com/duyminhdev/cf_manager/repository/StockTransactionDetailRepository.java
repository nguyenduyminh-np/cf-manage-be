package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.StockTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransactionDetailRepository extends JpaRepository<StockTransactionDetail, Integer> {
}