package com.duyminhdev.cf_manager.lock.booking;

import com.duyminhdev.cf_manager.exceptions.BookingLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingLockServiceImplTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lockTable2;

    @Mock
    private RLock lockTable8;

    private BookingLockServiceImpl bookingLockService;

    @BeforeEach
    void setUp() {
        bookingLockService = new BookingLockServiceImpl(redissonClient);
    }

    @Test
    void shouldAcquireSingleTableLockAndExecuteAction() throws Exception {
        when(redissonClient.getLock("lock:table:2")).thenReturn(lockTable2);
        when(lockTable2.tryLock(eq(2L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lockTable2.isHeldByCurrentThread()).thenReturn(true);

        int result = bookingLockService.executeWithTableLock(2, () -> 123);

        assertEquals(123, result);
        verify(lockTable2).unlock();
    }

    @Test
    void shouldAcquireMultipleTableLocksInAscendingOrderAndReleaseReverse() throws Exception {
        when(redissonClient.getLock("lock:table:2")).thenReturn(lockTable2);
        when(redissonClient.getLock("lock:table:8")).thenReturn(lockTable8);
        when(lockTable2.tryLock(eq(2L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lockTable8.tryLock(eq(2L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(lockTable2.isHeldByCurrentThread()).thenReturn(true);
        when(lockTable8.isHeldByCurrentThread()).thenReturn(true);

        String result = bookingLockService.executeWithTableLocks(List.of(8, 2, 8), () -> "ok");

        assertEquals("ok", result);

        InOrder inOrder = inOrder(redissonClient, lockTable8, lockTable2);
        inOrder.verify(redissonClient).getLock("lock:table:2");
        inOrder.verify(redissonClient).getLock("lock:table:8");
        inOrder.verify(lockTable8).unlock();
        inOrder.verify(lockTable2).unlock();
    }

    @Test
    void shouldThrowBookingLockExceptionWhenLockIsBusy() throws Exception {
        when(redissonClient.getLock("lock:table:2")).thenReturn(lockTable2);
        when(lockTable2.tryLock(eq(2L), eq(10L), eq(TimeUnit.SECONDS))).thenReturn(false);

        assertThrows(
                BookingLockException.class,
                () -> bookingLockService.executeWithTableLock(2, () -> "never")
        );
    }
}
