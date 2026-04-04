package com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishGroupedByTableNativeResultDTO;
import com.duyminhdev.cf_manager.repository.NativeSqlDishOrderDetailRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NativeSqlDishOrderDetailRepositoryImpl implements NativeSqlDishOrderDetailRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<DishGroupedByTableNativeResultDTO> findGroupedByTableId(Integer tableId) {
        /**
         * Mục tiêu:
         * - Gom món theo bàn để checkout
         * - Loại toàn bộ order CANCEL và DONE
         * - Nhờ vậy invoice total không bị kéo lại món đã thanh toán/hủy
         */
        String sql = """
                SELECT
                    d.id AS dishId,
                    d.dish_code AS dishCode,
                    d.dish_name AS dishName,
                    d.photo AS dishPhoto,
                    SUM(dod.quantity) AS totalQuantity,
                    dod.price AS unitPrice,
                    SUM(dod.quantity * dod.price) AS totalPrice
                FROM dish_order_detail dod
                INNER JOIN dish_order do1
                    ON do1.id = dod.dish_order_id
                INNER JOIN dish d
                    ON d.id = dod.dish_id
                INNER JOIN dish_order_status dos
                    ON dos.id = do1.dish_order_status_id
                WHERE do1.dining_table_id = :tableId
                  AND (do1.is_active = 1 OR do1.is_active = true)
                  AND (dod.is_active = 1 OR dod.is_active = true)
                  AND (d.is_active = 1 OR d.is_active = true)
                                    AND UPPER(dos.dish_order_status_code) NOT IN ('CANCEL', 'DONE')
                GROUP BY
                    d.id,
                    d.dish_code,
                    d.dish_name,
                    d.photo,
                    dod.price
                ORDER BY d.dish_name ASC
                """;

        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("tableId", tableId);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();

        return tuples.stream()
                .map(this::mapTupleToDto)
                .toList();
    }

    private DishGroupedByTableNativeResultDTO mapTupleToDto(Tuple tuple) {
        return DishGroupedByTableNativeResultDTO.builder()
                .dishId(NativeSqlTupleUtils.getInteger(tuple, "dishId"))
                .dishCode(NativeSqlTupleUtils.getString(tuple, "dishCode"))
                .dishName(NativeSqlTupleUtils.getString(tuple, "dishName"))
                .dishPhoto(NativeSqlTupleUtils.getString(tuple, "dishPhoto"))
                .totalQuantity(NativeSqlTupleUtils.getInteger(tuple, "totalQuantity"))
                .unitPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "unitPrice"))
                .totalPrice(NativeSqlTupleUtils.getBigDecimal(tuple, "totalPrice"))
                .build();
    }
}
