package main.java.com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.DishOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DishOrderStatusRepository extends JpaRepository<DishOrderStatus, Integer> {

    Optional<DishOrderStatus> findByDishOrderStatusCode(String dishOrderStatusCode);
}
