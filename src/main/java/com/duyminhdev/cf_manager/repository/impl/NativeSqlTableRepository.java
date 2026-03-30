package main.java.com.duyminhdev.cf_manager.repository.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.table.TableSearchRequestDTO;

import java.util.List;

public interface NativeSqlTableRepository {

    PageResponse<List<TableSearchNativeResultDTO>> search(TableSearchRequestDTO request);
}
