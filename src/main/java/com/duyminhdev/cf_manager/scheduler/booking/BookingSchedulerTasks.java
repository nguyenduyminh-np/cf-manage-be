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

    /**
     * C\u1ea3nh b\u00e1o b\u00e0n qu\u00e1 gi\u1edd checkout nh\u01b0ng ch\u01b0a \u0111\u01b0\u1ee3c gi\u1ea3i ph\u00f3ng.
     * Ch\u1ea1y m\u1ed7i ph\u00fat; logic dedup time-window (epoch/120) \u0111\u1ea3m b\u1ea3o nh\u1eafc l\u1ea1i th\u1ef1c s\u1ef1 m\u1ed7i 2 ph\u00fat.
     */
    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerCheckoutOverdue", lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}", lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void notifyOverdueCheckouts() {
        bookingSchedulerService.notifyOverdueCheckouts();
    }
}
