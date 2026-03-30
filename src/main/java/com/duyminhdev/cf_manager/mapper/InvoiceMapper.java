package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceConfirmPaymentResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceLineResponseDTO;
import com.duyminhdev.cf_manager.dto.response.invoice.InvoiceResponseDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(config = BaseMapperConfig.class, uses = {EnumDisplayMapper.class, InvoiceLineMapper.class})
public abstract class InvoiceMapper {

    @Autowired
    protected InvoiceLineMapper invoiceLineMapper;

    @Autowired
    protected EnumDisplayMapper enumDisplayMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceCode", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "totalGuest", source = "guestCount")
    public abstract Invoice toNewEntity(InvoiceCreateRequestDTO request);

    @Mapping(target = "invoiceId", source = "id")
    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableCode", source = "table.tableCode")
    @Mapping(target = "tableName", source = "table.tableName")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountUsername", source = "account.username")
    @Mapping(target = "accountFullName", source = "account.fullName")
    @Mapping(target = "paymentStatusName", source = "paymentStatus", qualifiedByName = "toPaymentStatusName")
    @Mapping(target = "paymentMethodName", source = "paymentMethod", qualifiedByName = "toPaymentMethodName")
    @Mapping(target = "guestCount", source = "totalGuest")
    public abstract InvoiceResponseDTO toResponseDTO(Invoice entity);

    public InvoiceConfirmPaymentResponseDTO toConfirmPaymentResponseDTO(Invoice entity) {
        if (entity == null) {
            return null;
        }

        return InvoiceConfirmPaymentResponseDTO.builder()
                .invoiceId(entity.getId())
                .invoiceCode(entity.getInvoiceCode())
                .paymentStatus(entity.getPaymentStatus())
                .paymentStatusName(enumDisplayMapper.toPaymentStatusName(entity.getPaymentStatus()))
                .build();
    }

    public InvoiceDetailResponseDTO toDetailResponseDTO(List<InvoiceDetailNativeResultDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        InvoiceDetailNativeResultDTO first = rows.getFirst();

        List<InvoiceLineResponseDTO> lineItems = new ArrayList<>();
        for (InvoiceDetailNativeResultDTO row : rows) {
            if (row.getInvoiceDetailId() != null) {
                lineItems.add(invoiceLineMapper.toResponseDTO(row));
            }
        }

        return InvoiceDetailResponseDTO.builder()
                .invoiceId(first.getInvoiceId())
                .invoiceCode(first.getInvoiceCode())
                .tableId(first.getTableId())
                .tableCode(first.getTableCode())
                .tableName(first.getTableName())
                .accountId(first.getAccountId())
                .accountUsername(first.getAccountUsername())
                .accountFullName(first.getAccountFullName())
                .paymentStatus(first.getPaymentStatus())
                .paymentStatusName(enumDisplayMapper.toPaymentStatusName(first.getPaymentStatus()))
                .paymentMethod(first.getPaymentMethod())
                .paymentMethodName(enumDisplayMapper.toPaymentMethodName(first.getPaymentMethod()))
                .guestCount(first.getGuestCount())
                .totalMoney(first.getTotalMoney())
                .createdTime(first.getCreatedTime())
                .invoiceDetails(lineItems)
                .build();
    }
}

