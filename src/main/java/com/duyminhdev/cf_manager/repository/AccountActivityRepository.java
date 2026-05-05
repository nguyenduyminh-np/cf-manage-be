package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.AccountActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountActivityRepository extends JpaRepository<AccountActivity, Integer> {
}