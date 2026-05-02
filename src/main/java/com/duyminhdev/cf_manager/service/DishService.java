package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.dish.DishCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish.DishDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish.DishExportDTO;
import com.duyminhdev.cf_manager.dto.response.dish.DishResponseDTO;

import java.util.List;

public interface DishService {

    /**
     * Lấy danh sách món theo điều kiện active để hiển thị menu.
     */
    List<DishResponseDTO> getAll(DishListRequestDTO request);

    /**
     * Tìm kiếm món ăn theo bộ lọc và phân trang.
     */
    PageResponse<List<DishResponseDTO>> search(DishSearchRequestDTO request);

    List<DishExportDTO> exportData(DishSearchRequestDTO request);

    DishDetailResponseDTO getDetail(Integer id);
    void delete(Integer id);
    DishDetailResponseDTO update(DishUpdateRequestDTO request);
    DishDetailResponseDTO create(DishCreateRequestDTO request);
}
