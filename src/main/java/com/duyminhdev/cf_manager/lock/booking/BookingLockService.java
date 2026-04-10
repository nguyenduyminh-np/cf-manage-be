package com.duyminhdev.cf_manager.lock.booking;

import java.util.Collection;
import java.util.function.Supplier;

public interface BookingLockService {

    <T> T executeWithTableLock(Integer tableId, Supplier<T> action);

    <T> T executeWithTableLocks(Collection<Integer> tableIds, Supplier<T> action);

    default void runWithTableLock(Integer tableId, Runnable action) {
        executeWithTableLock(tableId, () -> {
            action.run();
            return null;
        });
    }
}
