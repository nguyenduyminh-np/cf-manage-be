package com.duyminhdev.cf_manager.utils;

import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import com.duyminhdev.cf_manager.repository.AccountRepository;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.repository.DishOrderStatusRepository;
import com.duyminhdev.cf_manager.repository.DishRepository;
import com.duyminhdev.cf_manager.repository.TableBookingRepository;
import com.duyminhdev.cf_manager.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceSupportTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishOrderStatusRepository dishOrderStatusRepository;

    @Mock
    private TableBookingRepository tableBookingRepository;

    @Mock
    private DishOrderRepository dishOrderRepository;

    private ServiceSupport serviceSupport;

    @BeforeEach
    void setUp() {
        serviceSupport = new ServiceSupport(
                accountRepository,
                tableRepository,
                dishRepository,
                dishOrderStatusRepository,
                tableBookingRepository,
                dishOrderRepository
        );
    }

    @Test
    void resolveTableStatus_shouldReturnOccupied_whenTableHasCheckedInBooking() {
        Integer tableId = 11;

        when(tableBookingRepository.existsActiveBookingOnTableByStatuses(eq(tableId), anyCollection()))
                .thenReturn(true);

        TableStatusEnum resolved = serviceSupport.resolveTableStatus(tableId);

        assertEquals(TableStatusEnum.OCCUPIED, resolved);
        verify(tableBookingRepository, never())
                .existsUpcomingActiveBookingByTableIdAndStatuses(any(), anyCollection(), any(), any());
    }

    @Test
    void resolveTableStatus_shouldReturnBooked_whenNoCheckedInAndHasUpcomingConfirmedWithinWindow() {
        Integer tableId = 11;

        when(tableBookingRepository.existsActiveBookingOnTableByStatuses(eq(tableId), anyCollection()))
                .thenReturn(false);
        when(tableBookingRepository.existsUpcomingActiveBookingByTableIdAndStatuses(eq(tableId), anyCollection(), any(), any()))
                .thenReturn(true);

        TableStatusEnum resolved = serviceSupport.resolveTableStatus(tableId);

        assertEquals(TableStatusEnum.BOOKED, resolved);
    }

    @Test
    void resolveTableStatus_shouldReturnAvailable_whenNoCheckedInAndNoUpcomingBooking() {
        Integer tableId = 11;

        when(tableBookingRepository.existsActiveBookingOnTableByStatuses(eq(tableId), anyCollection()))
                .thenReturn(false);
        when(tableBookingRepository.existsUpcomingActiveBookingByTableIdAndStatuses(eq(tableId), anyCollection(), any(), any()))
                .thenReturn(false);

        TableStatusEnum resolved = serviceSupport.resolveTableStatus(tableId);

        assertEquals(TableStatusEnum.AVAILABLE, resolved);
        verifyNoInteractions(dishOrderRepository);
    }
}