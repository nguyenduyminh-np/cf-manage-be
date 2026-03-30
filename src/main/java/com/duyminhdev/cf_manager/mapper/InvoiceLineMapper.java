package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceLineResponseDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.entity.InvoiceDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface InvoiceLineMapper {

    @Mapping(target = "invoiceDetailId", source = "id")
    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishCode", source = "dish.dishCode")
    @Mapping(target = "dishName", source = "dish.dishName")
    @Mapping(target = "lineTotal", expression = "java(entity.getUnitPrice() != null && entity.getQuantity() != null ? entity.getUnitPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())) : null)")
    InvoiceLineResponseDTO toResponseDTO(InvoiceDetail entity);

    InvoiceLineResponseDTO toResponseDTO(InvoiceDetailNativeResultDTO source);
}

