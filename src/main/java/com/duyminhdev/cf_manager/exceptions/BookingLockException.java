package com.duyminhdev.cf_manager.exceptions;

public class BookingLockException extends RuntimeException {

    public BookingLockException(String message) {
        super(message);
    }

    public BookingLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
