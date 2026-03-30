package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.response.payment_method.PaymentMethodResponseDTO;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface PaymentMethodMapper {

    default PaymentMethodResponseDTO toResponseDTO(PaymentMethodEnum source) {
        if (source == null) {
            return null;
        }

        return PaymentMethodResponseDTO.builder()
                .code(source.getCode())
                .name(source.getLabel())
                .active(true)
                .displayOrder(source.ordinal() + 1)
                .build();
    }

    default List<PaymentMethodResponseDTO> toResponseDTOList(PaymentMethodEnum[] sources) {
        if (sources == null) {
            return List.of();
        }

        return Arrays.stream(sources)
                .map(this::toResponseDTO)
                .toList();
    }
}

