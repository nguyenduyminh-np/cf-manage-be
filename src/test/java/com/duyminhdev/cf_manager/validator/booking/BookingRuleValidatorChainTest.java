package com.duyminhdev.cf_manager.validator.booking;

import com.duyminhdev.cf_manager.enums.BookingValidationUseCase;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingRuleValidatorChainTest {

    @Test
    void shouldRunRulesInOrderForMatchedUseCase() {
        List<String> execution = new ArrayList<>();

        BookingValidationRule ruleA = new FakeRule("A", 30, Set.of(BookingValidationUseCase.CREATE_BOOKING), execution);
        BookingValidationRule ruleB = new FakeRule("B", 10, Set.of(BookingValidationUseCase.CREATE_BOOKING), execution);
        BookingValidationRule ruleC = new FakeRule("C", 20, Set.of(BookingValidationUseCase.CONFIRM_BOOKING), execution);

        BookingRuleValidatorChain chain = new BookingRuleValidatorChain(List.of(ruleA, ruleB, ruleC));

        BookingValidationContext context = BookingValidationContext.builder()
                .now(at(2026, 4, 10, 10, 0))
                .warnings(new ArrayList<>())
                .build();

        chain.validate(BookingValidationUseCase.CREATE_BOOKING, context);

        assertEquals(List.of("B", "A"), execution);
    }

    private static Instant at(int year, int month, int day, int hour, int minute) {
        return java.time.LocalDateTime.of(year, month, day, hour, minute, 0).toInstant(java.time.ZoneOffset.UTC);
    }

    private static class FakeRule implements BookingValidationRule {

        private final String id;
        private final int order;
        private final Set<BookingValidationUseCase> supported;
        private final List<String> execution;

        private FakeRule(
                String id,
                int order,
                Set<BookingValidationUseCase> supported,
                List<String> execution
        ) {
            this.id = id;
            this.order = order;
            this.supported = supported;
            this.execution = execution;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public boolean supports(BookingValidationUseCase useCase) {
            return supported.contains(useCase);
        }

        @Override
        public void validate(BookingValidationContext context) {
            execution.add(id);
        }
    }
}
