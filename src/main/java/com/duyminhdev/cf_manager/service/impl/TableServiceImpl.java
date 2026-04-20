package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableDetailRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableAvailableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.TableMapper;
import com.duyminhdev.cf_manager.repository.NativeSqlTableRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import com.duyminhdev.cf_manager.service.TableService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableServiceImpl implements TableService {
    private final NativeSqlTableRepository nativeSqlTableRepository;
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final ServiceSupport serviceSupport;

    @Override
    /**
     * Tìm kiếm danh sách bàn theo tiêu chí lọc và phân trang.
     */
    public PageResponse<List<TableSearchResponseDTO>> search(TableSearchRequestDTO request) {
        /**
         * Flow:
         * 1. Gọi native repository để lấy aggregate view của bàn
         * 2. Map sang DTO hiển thị
         * 3. Giữ nguyên metadata page
         */
        PageResponse<List<TableSearchNativeResultDTO>> pageResult = nativeSqlTableRepository.search(request);

        List<TableSearchResponseDTO> mapped = pageResult.getRows().stream()
                .map(tableMapper::toSearchResponseDTO)
                .toList();

        PageResponse<List<TableSearchResponseDTO>> response = new PageResponse<>();
        response.setRows(mapped);
        response.setPageNo(pageResult.getPageNo());
        response.setPageSize(pageResult.getPageSize());
        response.setTotalElements(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        return response;
    }

    @Override
    /**
     * Lấy danh sách bàn có trạng thái AVAILABLE.
     */
    public List<TableAvailableResponseDTO> availableTables(TableAvailableSearchRequestDTO request) {
        List<TableAvailableNativeResultDTO> availableTables = nativeSqlTableRepository.findAvailableTables(request);
        return availableTables.stream()
                .map(tableMapper::toAvailableResponseDTO)
                .toList();
    }

    @Override
    /**
     * Lấy chi tiết thông tin của một bàn theo id.
     */
    public TableDetailResponseDTO detail(TableDetailRequestDTO request) {
        /**
         * Flow:
         * 1. Lấy bàn active theo id
         * 2. Không có thì throw InvalidDataException
         * 3. Map detail response
         */
        TableEntity table = tableRepository.findByIdAndActiveTrue(request.getTableId())
                .orElseThrow(() -> new InvalidDataException(
                        "Table not found with id: " + request.getTableId()
                ));

        return tableMapper.toDetailResponseDTO(table);
    }

    @Override
    @Transactional
    /**
     * Cập nhật thủ công trạng thái của bàn.
     */
    public Boolean updateStatus(TableStatusUpdateRequestDTO request) {
        /**
         * Flow update table status:
         * 1. Validate mã trạng thái đầu vào
         * 2. Load bàn đang active
         * 3. Set trạng thái mới cho bàn
         * 4. Save thay đổi và trả kết quả
         */
        serviceSupport.validateTableStatusCode(request.getTableStatus());

        TableEntity table = serviceSupport.getActiveTable(request.getTableId());
        table.setTableStatus(TableStatusEnum.fromCode(request.getTableStatus()).getCode());
        tableRepository.save(table);
        return true;
    }
}
