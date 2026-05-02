package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table.TableCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableAvailableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableExportDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.TableService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/table")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class TableController {

    private final TableService tableService;

    @PostMapping("/grid/list")
    public ApiResponse<List<TableSearchResponseDTO>> gridList(
            @RequestBody(required = false) TableListRequestDTO request
    ) {
        TableListRequestDTO safeRequest = request != null ? request : new TableListRequestDTO();
        return new ApiResponse<>(200, "GET_TABLE_LIST_SUCCESS", tableService.list(safeRequest));
    }

    @PostMapping("/grid/search")
    public ApiResponse<PageResponse<List<TableSearchResponseDTO>>> gridSearch(
            @Valid @RequestBody TableSearchRequestDTO request
    ) {
        return new ApiResponse<>(200, "SEARCH_TABLE_SUCCESS", tableService.search(request));
    }

    @PostMapping("/grid/export")
    public void gridExport(@Valid @RequestBody TableSearchRequestDTO request,
                           HttpServletResponse response) throws IOException {
        List<TableExportDTO> items = tableService.exportData(request);

        String fileName = "DANH_SACH_BAN_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH BÀN";

        ExcelUtils.export(response, TableExportDTO.class, items, fileName, title);
    }

    @PostMapping("/grid/create")
    public ApiResponse<TableDetailResponseDTO> gridCreate(
            @Valid @RequestBody TableCreateRequestDTO request
    ) {
        return new ApiResponse<>(201, "CREATE_TABLE_SUCCESS", tableService.create(request));
    }

    @PostMapping("/grid/update")
    public ApiResponse<TableDetailResponseDTO> gridUpdate(
            @Valid @RequestBody TableUpdateRequestDTO request
    ) {
        return new ApiResponse<>(200, "UPDATE_TABLE_SUCCESS", tableService.update(request));
    }

    @PostMapping("/grid/delete")
    public ApiResponse<Void> gridDelete(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        tableService.delete(id);
        return new ApiResponse<>(200, "DELETE_TABLE_SUCCESS");
    }

    @PostMapping("/grid/detail")
    public ApiResponse<TableDetailResponseDTO> gridDetail(
            @Valid @RequestBody TableDetailRequestDTO request
    ) {
        return new ApiResponse<>(200, "GET_TABLE_DETAIL_SUCCESS", tableService.detail(request));
    }

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
     * Lay danh sach ban dang trong trang thai AVAILABLE.
     */
    @PostMapping("/available")
    public ApiResponse<List<TableAvailableResponseDTO>> available(
            @Valid @RequestBody(required = false) TableAvailableSearchRequestDTO request
    ) {
        /**
         * Flow API table available:
         * 1. Nhan request body filter dong theo table_name, floor, seat
         * 2. Goi service lay danh sach ban co trang thai AVAILABLE theo bo loc
         * 3. Tra response ve FE
         */
        return new ApiResponse<>(200, "GET_AVAILABLE_TABLES_SUCCESS", tableService.availableTables(request));
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
