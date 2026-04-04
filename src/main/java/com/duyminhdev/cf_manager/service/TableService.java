package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;

import java.util.List;

public interface TableService {

    /**
     * Tìm kiếm danh sách bàn theo điều kiện lọc và phân trang.
     */
    PageResponse<List<TableSearchResponseDTO>> search(TableSearchRequestDTO request);

    /**
     * Lấy thông tin chi tiết của một bàn theo id.
     */
    TableDetailResponseDTO detail(TableDetailRequestDTO request);

    /**
     * Cập nhật thủ công trạng thái bàn theo yêu cầu vận hành.
     */
    Boolean updateStatus(TableStatusUpdateRequestDTO request);
}