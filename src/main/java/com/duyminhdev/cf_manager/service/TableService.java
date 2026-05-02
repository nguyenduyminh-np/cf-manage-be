package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table.TableCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableExportDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableAvailableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;

import java.util.List;

public interface TableService {

    /**
     * Lấy danh sách bàn cho màn grid.
     */
    List<TableSearchResponseDTO> list(TableListRequestDTO request);

    /**
     * Tìm kiếm danh sách bàn theo điều kiện lọc và phân trang.
     */
    PageResponse<List<TableSearchResponseDTO>> search(TableSearchRequestDTO request);
    List<TableExportDTO> exportData(TableSearchRequestDTO request);

    /**
     * Lấy thông tin chi tiết của một bàn theo id.
     */
    TableDetailResponseDTO detail(TableDetailRequestDTO request);

    /**
     * Cập nhật thủ công trạng thái bàn theo yêu cầu vận hành.
     */
    Boolean updateStatus(TableStatusUpdateRequestDTO request);

    TableDetailResponseDTO create(TableCreateRequestDTO request);
    TableDetailResponseDTO update(TableUpdateRequestDTO request);
    void delete(Integer id);

    /**
     * Lấy danh sách bàn đang ở trạng thái AVAILABLE.
     */
    List<TableAvailableResponseDTO> availableTables(TableAvailableSearchRequestDTO request);
}