package com.duyminhdev.cf_manager.lock.booking;

import com.duyminhdev.cf_manager.constant.BookingLockConstant;
import com.duyminhdev.cf_manager.exceptions.BookingLockException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class BookingLockServiceImpl implements BookingLockService {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithTableLock(Integer tableId, Supplier<T> action) {
        return executeWithTableLocks(List.of(tableId), action);
    }

    @Override
    public <T> T executeWithTableLocks(Collection<Integer> tableIds, Supplier<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }

        List<Integer> normalizedTableIds = normalizeTableIds(tableIds);
        List<RLock> acquiredLocks = new ArrayList<>();

        try {
            for (Integer tableId : normalizedTableIds) {
                RLock lock = redissonClient.getLock(toLockKey(tableId));
                boolean acquired = tryAcquire(lock, tableId);
                if (!acquired) {
                    throw new BookingLockException("Table is being updated by another request (tableId=" + tableId + ")");
                }
                acquiredLocks.add(lock);
            }

            return action.get();
        } finally {
            releaseLocks(acquiredLocks);
        }
    }

    private List<Integer> normalizeTableIds(Collection<Integer> tableIds) {
        if (tableIds == null || tableIds.isEmpty()) {
            throw new IllegalArgumentException("tableIds is required");
        }

        List<Integer> normalized = tableIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("tableIds must contain at least one positive id");
        }

        return normalized;
    }

    private boolean tryAcquire(RLock lock, Integer tableId) {
        try {
            return lock.tryLock(
                    BookingLockConstant.TABLE_LOCK_WAIT_SECONDS,
                    BookingLockConstant.TABLE_LOCK_LEASE_SECONDS,
                    TimeUnit.SECONDS
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BookingLockException("Interrupted while waiting table lock (tableId=" + tableId + ")", ex);
        }
    }

    private void releaseLocks(List<RLock> acquiredLocks) {
        for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
            RLock lock = acquiredLocks.get(i);
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception ignored) {
                // Best-effort unlock to avoid masking business exceptions.
            }
        }
    }

    private String toLockKey(Integer tableId) {
        return BookingLockConstant.TABLE_LOCK_KEY_PREFIX + tableId;
    }
}
