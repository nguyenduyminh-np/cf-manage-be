package com.duyminhdev.cf_manager.scheduler.booking;

import com.duyminhdev.cf_manager.service.booking.BookingSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "booking.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BookingSchedulerTasks {

    private final BookingSchedulerService bookingSchedulerService;

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerReserveTables", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void reserveTablesBeforeArrival() {
        bookingSchedulerService.reserveTablesForUpcomingConfirmedBookings();
    }

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerExpireNoShow", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void expireNoShowBookings() {
        bookingSchedulerService.expireNoShowBookings();
    }

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerOccupiedConflicts", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void notifyOccupiedConflicts() {
        bookingSchedulerService.notifyOccupiedTableConflicts();
    }

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerNoOrderTimeout", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void processNoOrderTimeout() {
        bookingSchedulerService.processNoOrderTimeoutFlow();
    }

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerCheckoutReminder", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void notifyCheckoutReminder() {
        bookingSchedulerService.notifyBeforeExpectedCheckout();
    }
}
