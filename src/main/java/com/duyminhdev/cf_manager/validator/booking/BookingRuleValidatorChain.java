package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingRuleValidatorChain {

    private final List<BookingValidationRule> rules;

    public void validate(BookingValidationUseCase useCase, BookingValidationContext context) {
        if (useCase == null) {
            throw new IllegalArgumentException("useCase is required");
        }
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }

        rules.stream()
                .filter(rule -> rule.supports(useCase))
                .sorted(Comparator.comparingInt(BookingValidationRule::order))
                .forEach(rule -> rule.validate(context));
    }
}
