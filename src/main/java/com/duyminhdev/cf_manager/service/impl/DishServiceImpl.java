package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish.*;
import com.duyminhdev.cf_manager.dto.response.dish.*;
import com.duyminhdev.cf_manager.entity.Dish;
import com.duyminhdev.cf_manager.entity.DishCategory;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.DishMapper;
import com.duyminhdev.cf_manager.repository.DishCategoryRepository;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishServiceImpl implements DishService {
    private final DishRepository dishRepository;
    private final DishCategoryRepository dishCategoryRepository;
    private final DishMapper dishMapper;

    @Override
    public List<DishResponseDTO> getAll(DishListRequestDTO request) {
        List<Dish> dishes;
        boolean onlyActive = request.getActive() == null || request.getActive();
        Integer categoryId = request.getDishCategoryId();

        if (categoryId != null) {
            dishes = onlyActive
                    ? dishRepository.findAllByDishCategory_IdAndActiveTrueOrderByDishNameAsc(categoryId)
                    : dishRepository.findAllByDishCategory_IdOrderByDishNameAsc(categoryId);
        } else {
            dishes = onlyActive
                    ? dishRepository.findAllByActiveTrueOrderByDishNameAsc()
                    : dishRepository.findAll(Sort.by(Sort.Direction.ASC, "dishName"));
        }

        return dishes.stream()
                .map(dishMapper::toResponseDTO)
                .toList();
    }


    @Override
    public PageResponse<List<DishResponseDTO>> search(DishSearchRequestDTO request) {
        int pageNo = PageUtils.normalizePage(request.getPage());
        int pageSize = PageUtils.normalizeLimit(request.getLimit());

        Pageable pageable = PageRequest.of(
                pageNo,
                pageSize,
                DishSpec.resolveSort(request.getSortField(), request.getSortDir())
        );

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


    // trong DishServiceImpl
    @Override
    public List<DishExportDTO> exportData(DishSearchRequestDTO request) {
        // Lấy tất cả dữ liệu thỏa mãn điều kiện, không phân trang
        List<Dish> dishes = dishRepository.findAll(
                DishSpec.byCriteria(request),
                DishSpec.resolveSort(request.getSortField(), request.getSortDir())
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return dishes.stream().map(dish -> {
            DishExportDTO dto = new DishExportDTO();
            dto.setDishCode(dish.getDishCode());
            dto.setDishName(dish.getDishName());
            dto.setPrice(dish.getPrice());
            dto.setPhoto(dish.getPhoto());

            if (dish.getCreatedTime() != null) {
                dto.setCreatedTime(formatter.format(dish.getCreatedTime()));
            }

            dto.setActive(Boolean.TRUE.equals(dish.getActive()) ? "Hoạt động" : "Không hoạt động");

            if (dish.getDishCategory() != null) {
                dto.setDishCategoryName(dish.getDishCategory().getDishCategoryName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DishDetailResponseDTO create(DishCreateRequestDTO request) {
        Dish dish = new Dish();
        dish.setDishCode(request.getDishCode());
        dish.setDishName(request.getDishName());
        dish.setPrice(request.getPrice());
        dish.setPhoto(request.getPhoto());
        dish.setCreatedTime(Instant.now());
        dish.setActive(true);

        // Gán danh mục
        DishCategory category = dishCategoryRepository.findById(request.getDishCategoryId())
                .orElseThrow(() -> new InvalidDataException("Danh mục món ăn không tồn tại"));
        dish.setDishCategory(category);

        dish = dishRepository.save(dish);
        return mapToDetail(dish);
    }

    @Override
    @Transactional
    public DishDetailResponseDTO update(DishUpdateRequestDTO request) {
        Dish dish = dishRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Món ăn không tồn tại"));

        if (request.getDishCode() != null) {
            dish.setDishCode(request.getDishCode());
        }
        if (request.getDishName() != null && !request.getDishName().isBlank()) {
            dish.setDishName(request.getDishName());
        }
        if (request.getPrice() != null) {
            dish.setPrice(request.getPrice());
        }
        if (request.getPhoto() != null) {
            dish.setPhoto(request.getPhoto());
        }
        if (request.getDishCategoryId() != null) {
            DishCategory category = dishCategoryRepository.findById(request.getDishCategoryId())
                    .orElseThrow(() -> new InvalidDataException("Danh mục món ăn không tồn tại"));
            dish.setDishCategory(category);
        }
        if (request.getActive() != null) {
            dish.setActive(request.getActive());
        }

        dish = dishRepository.save(dish);
        return mapToDetail(dish);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Món ăn không tồn tại"));
        dish.setActive(false);
        dishRepository.save(dish);
    }

    @Override
    public DishDetailResponseDTO getDetail(Integer id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Món ăn không tồn tại"));
        return mapToDetail(dish);
    }

    private DishDetailResponseDTO mapToDetail(Dish dish) {
        return DishDetailResponseDTO.builder()
                .id(dish.getId())
                .dishCode(dish.getDishCode())
                .dishName(dish.getDishName())
                .price(dish.getPrice())
                .photo(dish.getPhoto())
                .createdTime(dish.getCreatedTime())
                .active(dish.getActive())
                .dishCategoryId(dish.getDishCategory().getId())
                .dishCategoryName(dish.getDishCategory().getDishCategoryName())
                .build();
    }
}