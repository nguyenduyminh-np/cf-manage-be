package com.duyminhdev.cf_manager.mapper;


import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingUpdateRequestDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;
import org.mapstruct.*;

@Mapper(config = BaseMapperConfig.class, uses = EnumDisplayMapper.class)
public interface TableBookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "checkInAt", ignore = true)
    @Mapping(target = "checkOutAt", ignore = true)
    TableBooking toNewEntity(TableBookingCreateRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "checkInAt", ignore = true)
    @Mapping(target = "checkOutAt", ignore = true)
    void updateEntityFromRequest(TableBookingUpdateRequestDTO request, @MappingTarget TableBooking entity);

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableCode", source = "table.tableCode")
    @Mapping(target = "tableName", source = "table.tableName")
    @Mapping(target = "bookingStatusName", source = "bookingStatus", qualifiedByName = "toBookingStatusName")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountUsername", source = "account.username")
    @Mapping(target = "accountFullName", source = "account.fullName")
    TableBookingResponseDTO toResponseDTO(TableBooking entity);
}

