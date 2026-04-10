package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;

public interface BookingValidationRule {

    int order();

    boolean supports(BookingValidationUseCase useCase);

    void validate(BookingValidationContext context);
}
