package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;

import java.math.BigDecimal;
import java.time.Instant;

public interface BookingUseCaseService {

    TableBooking createBooking(TableBookingCreateRequestDTO request);

    TableBooking confirmBooking(Integer bookingId);

    TableBooking checkIn(Integer bookingId, Instant requestedCheckInAt, boolean force);

    TableBooking checkOut(Integer bookingId, Instant requestedCheckOutAt);

    TableBooking cancelBooking(Integer bookingId);

    TableBooking expireBooking(Integer bookingId);

    TableBooking extendBooking(Integer bookingId, Instant newExpectedCheckOut, boolean force);

    TableBooking createWalkIn(TableBookingCreateRequestDTO request, boolean force);

    TableBooking createWalkInFromLateArrival(Integer lateBookingId, TableBookingCreateRequestDTO walkInRequest, boolean force);

    TableBooking cancelBookingNoOrderTimeout(Integer bookingId, Instant now);

    TableBooking markDepositPaid(Integer bookingId, BigDecimal depositAmount, String depositTxnRef, Instant paidAt);
}
