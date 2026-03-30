package main.java.com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer>, JpaSpecificationExecutor<Dish> {

    Optional<Dish> findByIdAndActiveTrue(Integer id);

    List<Dish> findAllByActiveTrueOrderByDishNameAsc();
}
