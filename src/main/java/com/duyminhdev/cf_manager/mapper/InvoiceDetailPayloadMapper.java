package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceDetailPayloadDTO;
import com.duyminhdev.cf_manager.entity.InvoiceDetail;
import org.mapstruct.*;

@Mapper(config = BaseMapperConfig.class)
public interface InvoiceDetailPayloadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    InvoiceDetail toNewEntity(InvoiceDetailPayloadDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromPayload(InvoiceDetailPayloadDTO request, @MappingTarget InvoiceDetail entity);
}

