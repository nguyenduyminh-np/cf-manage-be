package com.duyminhdev.cf_manager.service.booking;

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BookingUseCaseService {

    TableBooking createBooking(TableBookingCreateRequestDTO request);

    TableBooking confirmBooking(Integer bookingId);

    TableBooking checkIn(Integer bookingId, LocalDateTime requestedCheckInAt, boolean force);

    TableBooking checkOut(Integer bookingId, LocalDateTime requestedCheckOutAt);

    TableBooking cancelBooking(Integer bookingId);

    TableBooking expireBooking(Integer bookingId);

    TableBooking extendBooking(Integer bookingId, LocalDateTime newExpectedCheckOut, boolean force);

    TableBooking createWalkIn(TableBookingCreateRequestDTO request, boolean force);

    TableBooking createWalkInFromLateArrival(Integer lateBookingId, TableBookingCreateRequestDTO walkInRequest, boolean force);

    TableBooking cancelBookingNoOrderTimeout(Integer bookingId, LocalDateTime now);

    TableBooking markDepositPaid(Integer bookingId, BigDecimal depositAmount, String depositTxnRef, LocalDateTime paidAt);
}
