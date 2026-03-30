package com.duyminhdev.cf_manager.dto.response.payment_method;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponseDTO {

    private String code;
    private String name;
    private Boolean active;
    private Integer displayOrder;
}

