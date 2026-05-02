package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishCategorySearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.dish_category.*;
import com.duyminhdev.cf_manager.dto.response.dish_category.*;
import com.duyminhdev.cf_manager.entity.DishCategory;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.DishCategoryMapper;
import com.duyminhdev.cf_manager.repository.DishCategoryRepository;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlDishCategoryRepository;
import com.duyminhdev.cf_manager.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishCategoryServiceImpl implements DishCategoryService {

    private final DishCategoryRepository dishCategoryRepository;
    private final DishCategoryMapper dishCategoryMapper;
    private final NativeSqlDishCategoryRepository nativeSqlDishCategoryRepository;

    @Override
    public List<DishCategoryResponseDTO> getAll(DishCategoryListRequestDTO request) {
        List<DishCategory> categories;
        if (request.getActive() == null || request.getActive()) {
            categories = dishCategoryRepository.findAllByActiveTrueOrderByDishCategoryNameAsc();
        } else {
            categories = dishCategoryRepository.findAll().stream()
                    .sorted(Comparator.comparing(DishCategory::getDishCategoryName))
                    .toList();
        }
        return categories.stream()
                .map(dishCategoryMapper::toResponseDTO)
                .toList();
    }

    @Override
    public PageResponse<List<DishCategoryListItemDTO>> search(DishCategorySearchRequestDTO request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 20;
        int offset = page * limit;

        List<DishCategorySearchNativeResult> rows =
                nativeSqlDishCategoryRepository.searchCategories(request, offset, limit);
        long total = nativeSqlDishCategoryRepository.countCategories(request);

        List<DishCategoryListItemDTO> list = rows.stream()
                .map(r -> DishCategoryListItemDTO.builder()
                        .id(r.getId())
                        .dishCategoryCode(r.getDishCategoryCode())
                        .dishCategoryName(r.getDishCategoryName())
                        .createdTime(r.getCreatedTime())
                        .active(r.getActive())
                        .build())
                .collect(Collectors.toList());

        PageResponse<List<DishCategoryListItemDTO>> response = new PageResponse<>();
        response.setRows(list);
        response.setPageNo(page);
        response.setPageSize(limit);
        response.setTotalElements((int) total);
        response.setTotalPages((int) Math.ceil((double) total / limit));
        return response;
    }

    @Override
    public List<DishCategoryExportDTO> exportData(DishCategorySearchRequestDTO request) {
        long total = nativeSqlDishCategoryRepository.countCategories(request);
        if (total <= 0) {
            return List.of();
        }

        List<DishCategorySearchNativeResult> rows = nativeSqlDishCategoryRepository.searchCategories(request, 0, (int) total);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return rows.stream()
                .map(r -> DishCategoryExportDTO.builder()
                        .dishCategoryCode(r.getDishCategoryCode())
                        .dishCategoryName(r.getDishCategoryName())
                        .createdTime(r.getCreatedTime() != null ? formatter.format(r.getCreatedTime()) : null)
                        .active(Boolean.TRUE.equals(r.getActive()) ? "Hoạt động" : "Không hoạt động")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DishCategoryDetailResponseDTO create(DishCategoryCreateRequestDTO request) {
        DishCategory entity = new DishCategory();
        entity.setDishCategoryCode(request.getDishCategoryCode());
        entity.setDishCategoryName(request.getDishCategoryName());
        entity.setCreatedTime(Instant.now());
        entity.setActive(true); // mặc định active
        entity = dishCategoryRepository.save(entity);
        return mapToDetail(entity);
    }

    @Override
    @Transactional
    public DishCategoryDetailResponseDTO update(DishCategoryUpdateRequestDTO request) {
        DishCategory entity = dishCategoryRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));
        if (request.getDishCategoryCode() != null) {
            entity.setDishCategoryCode(request.getDishCategoryCode());
        }
        if (request.getDishCategoryName() != null && !request.getDishCategoryName().isBlank()) {
            entity.setDishCategoryName(request.getDishCategoryName());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
        entity = dishCategoryRepository.save(entity);
        return mapToDetail(entity);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        DishCategory entity = dishCategoryRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));
        entity.setActive(false);
        dishCategoryRepository.save(entity);
    }

    @Override
    public DishCategoryDetailResponseDTO getDetail(Integer id) {
        DishCategory entity = dishCategoryRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Danh mục không tồn tại"));
        return mapToDetail(entity);
    }

    @Override
    public List<DishCategoryOptionDTO> getOptions() {
        List<DishCategory> categories = dishCategoryRepository.findAllByActiveTrueOrderByDishCategoryNameAsc();
        return categories.stream()
                .map(c -> DishCategoryOptionDTO.builder()
                        .id(c.getId())
                        .dishCategoryCode(c.getDishCategoryCode())
                        .dishCategoryName(c.getDishCategoryName())
                        .build())
                .toList();
    }

    private DishCategoryDetailResponseDTO mapToDetail(DishCategory entity) {
        long activeDishCount = entity.getDishes() != null
                ? entity.getDishes().stream().filter(d -> d.getActive() != null && d.getActive()).count()
                : 0;
        return DishCategoryDetailResponseDTO.builder()
                .id(entity.getId())
                .dishCategoryCode(entity.getDishCategoryCode())
                .dishCategoryName(entity.getDishCategoryName())
                .createdTime(entity.getCreatedTime())
                .active(entity.getActive())
                .dishCount(activeDishCount)
                .build();
    }
}