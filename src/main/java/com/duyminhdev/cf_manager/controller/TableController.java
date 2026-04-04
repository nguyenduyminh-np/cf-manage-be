package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;
import com.duyminhdev.cf_manager.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/table")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    /**
     * Tim kiem danh sach ban theo bo loc va phan trang.
     */
    @PostMapping("/search")
    public ApiResponse<PageResponse<List<TableSearchResponseDTO>>> search(
            @Valid @RequestBody TableSearchRequestDTO request
    ) {
        /**
         * Flow API table search:
         * 1. Nhan request tim kiem ban tu FE
         * 2. Goi service de query va phan trang du lieu
         * 3. Tra ket qua theo format ApiResponse
         */
        return new ApiResponse<>(200, "SEARCH_TABLE_SUCCESS", tableService.search(request));
    }

    /**
     * Lay thong tin chi tiet cua mot ban.
     */
    @PostMapping("/detail")
    public ApiResponse<TableDetailResponseDTO> detail(
            @Valid @RequestBody TableDetailRequestDTO request
    ) {
        /**
         * Flow API table detail:
         * 1. Nhan tableId can xem chi tiet
         * 2. Goi service lay du lieu chi tiet cua ban
         * 3. Tra response cho man hinh hien thi
         */
        return new ApiResponse<>(200, "GET_TABLE_DETAIL_SUCCESS", tableService.detail(request));
    }

    /**
     * Cap nhat thu cong trang thai cua ban.
     */
    @PostMapping("/update-status")
    public ApiResponse<Boolean> updateStatus(
            @Valid @RequestBody TableStatusUpdateRequestDTO request
    ) {
        /**
         * Flow API table update status:
         * 1. Nhan tableId va tableStatus moi
         * 2. Goi service de validate va cap nhat trang thai
         * 3. Tra ket qua thao tac cho FE
         */
        return new ApiResponse<>(200, "UPDATE_TABLE_STATUS_SUCCESS", tableService.updateStatus(request));
    }
}
