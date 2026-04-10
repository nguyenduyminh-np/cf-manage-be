package com.duyminhdev.cf_manager.state_machine.booking;

import com.duyminhdev.cf_manager.entity.TableBooking;
import com.duyminhdev.cf_manager.enums.BookingStatusEnum;

public interface BookingStateMachine {

    TableBooking initialize(TableBooking booking, BookingStatusEnum initialStatus);

    TableBooking transition(BookingTransitionContext context);
}
