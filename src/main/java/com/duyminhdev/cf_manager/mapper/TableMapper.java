package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableAvailableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table.TableSearchResponseDTO;
import com.duyminhdev.cf_manager.entity.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = EnumDisplayMapper.class)
public interface TableMapper {

    @Mapping(target = "tableId", source = "id")
    @Mapping(target = "tableStatusName", source = "tableStatus", qualifiedByName = "toTableStatusName")
    TableDetailResponseDTO toDetailResponseDTO(TableEntity entity);

    @Mapping(target = "tableStatusName", source = "tableStatus", qualifiedByName = "toTableStatusName")
    TableSearchResponseDTO toSearchResponseDTO(TableSearchNativeResultDTO source);

    TableAvailableResponseDTO toAvailableResponseDTO(TableAvailableNativeResultDTO source);
}

