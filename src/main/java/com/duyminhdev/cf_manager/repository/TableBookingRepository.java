package main.java.com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.TableBooking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Integer>, JpaSpecificationExecutor<TableBooking> {

    @EntityGraph(attributePaths = {"table", "account"})
    Optional<TableBooking> findByIdAndActiveTrue(Integer id);

    boolean existsByIdAndActiveTrue(Integer id);
}
