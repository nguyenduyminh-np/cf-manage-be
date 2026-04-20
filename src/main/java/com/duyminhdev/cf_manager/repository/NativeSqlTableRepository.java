package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableAvailableSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;

import java.util.List;

public interface NativeSqlTableRepository {

    PageResponse<List<TableSearchNativeResultDTO>> search(TableSearchRequestDTO request);

    List<TableAvailableNativeResultDTO> findAvailableTables(TableAvailableSearchRequestDTO request);
}
