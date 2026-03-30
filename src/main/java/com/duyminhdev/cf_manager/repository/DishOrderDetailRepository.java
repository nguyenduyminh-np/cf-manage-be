package main.java.com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.DishOrderDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishOrderDetailRepository extends JpaRepository<DishOrderDetail, Integer> {

    @EntityGraph(attributePaths = {"dishOrder", "dish"})
    List<DishOrderDetail> findAllByDishOrderIdAndActiveTrueOrderByCreatedTimeAsc(Integer dishOrderId);

    void deleteAllByDishOrderId(Integer dishOrderId);
}
