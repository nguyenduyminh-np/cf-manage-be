package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish.DishListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish.DishResponseDTO;
import com.duyminhdev.cf_manager.entity.Dish;
import com.duyminhdev.cf_manager.mapper.DishMapper;
import com.duyminhdev.cf_manager.repository.DishRepository;
import com.duyminhdev.cf_manager.repository.spec.DishSpec;
import com.duyminhdev.cf_manager.service.DishService;
import com.duyminhdev.cf_manager.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @Override
    /**
     * Lấy danh sách món theo cờ active để hiển thị menu.
     */
    public List<DishResponseDTO> getAll(DishListRequestDTO request) {
        /**
         * Flow:
         * 1. Nếu FE chỉ cần menu active -> load active
         * 2. Nếu cần full list -> load toàn bộ
         * 3. Sort theo tên món
         * 4. Map DTO trả ra ngoài
         */
        List<Dish> dishes;
        if (request.getActive() == null || request.getActive()) {
            dishes = dishRepository.findAllByActiveTrueOrderByDishNameAsc();
        } else {
            dishes = dishRepository.findAll(Sort.by(Sort.Direction.ASC, "dishName"));
        }
        return dishes.stream()
                .map(dishMapper::toResponseDTO)
                .toList();
    }

    @Override
    /**
     * Tìm kiếm món ăn có phân trang theo tiêu chí lọc.
     */
    public PageResponse<List<DishResponseDTO>> search(DishSearchRequestDTO request) {
        int pageNo = PageUtils.normalizePage(request.getPage());
        int pageSize = PageUtils.normalizeLimit(request.getLimit());

        Pageable pageable = PageRequest.of(
                pageNo,
                pageSize,
                DishSpec.resolveSort(request.getSortField(), request.getSortDir())
        );

        /**
         * Flow:
         * 1. Build spec từ search criteria
         * 2. Query paging
         * 3. Map DTO
         * 4. Giữ metadata page
         */
        Page<Dish> page = dishRepository.findAll(DishSpec.byCriteria(request), pageable);

        List<DishResponseDTO> data = page.getContent().stream()
                .map(dishMapper::toResponseDTO)
                .toList();

        PageResponse<List<DishResponseDTO>> response = new PageResponse<>();
        response.setRows(data);
        response.setPageNo(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }
}
