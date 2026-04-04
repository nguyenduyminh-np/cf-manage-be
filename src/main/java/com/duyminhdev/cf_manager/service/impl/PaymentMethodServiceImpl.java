package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.payment_method.PaymentMethodListRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment_method.PaymentMethodResponseDTO;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import com.duyminhdev.cf_manager.mapper.PaymentMethodMapper;
import com.duyminhdev.cf_manager.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    /**
     * Lấy danh sách phương thức thanh toán theo trạng thái active.
     */
    public List<PaymentMethodResponseDTO> getAll(PaymentMethodListRequestDTO request) {
        /**
         * Flow:
         * 1. Lấy dữ liệu từ enum ảo, không cần query DB
         * 2. Filter theo active nếu request có truyền
         * 3. Sort theo displayOrder để FE luôn hiển thị đúng thứ tự
         * 4. Map ra response DTO
         */
        return Arrays.stream(PaymentMethodEnum.values())
                .filter(item -> request.getActive() == null || item.isActive() == request.getActive())
                .sorted(Comparator.comparingInt(PaymentMethodEnum::getDisplayOrder))
                .map(paymentMethodMapper::toResponseDTO)
                .toList();
    }
}
