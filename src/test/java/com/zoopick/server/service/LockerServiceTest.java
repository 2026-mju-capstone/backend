package com.zoopick.server.service;

import com.zoopick.server.entity.*;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.repository.ItemRepository;
import com.zoopick.server.repository.LockerCommandRepository;
import com.zoopick.server.repository.LockerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockerServiceTest {

    @InjectMocks
    private LockerService lockerService;

    @Mock
    private LockerRepository lockerRepository;
    @Mock
    private LockerCommandRepository commandRepository;
    @Mock
    private ItemRepository itemRepository;

    private Locker emptyLocker;
    private Locker fullLocker;
    private Item validItem;
    private Item storedItem;

    @BeforeEach
    void setUp() {
        emptyLocker = Locker.builder()
                .id(1L)
                .status(LockerStatus.EMPTY)
                .currentItem(null)
                .build();

        storedItem = Item.builder()
                .id(100L)
                .status(ItemStatus.IN_LOCKER)
                .build();

        fullLocker = Locker.builder()
                .id(2L)
                .status(LockerStatus.IN_USE)
                .currentItem(storedItem)
                .build();

        validItem = Item.builder()
                .id(200L)
                .type(ItemType.FOUND)
                .status(ItemStatus.REPORTED)
                .build();
    }

    @Test
    @DisplayName("사물함 열기 요청 - 보관 (빈 사물함 + 유효한 아이템)")
    void requestUnlock_Storage_Success() {
        // given
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(emptyLocker));
        when(itemRepository.findById(200L)).thenReturn(Optional.of(validItem));
        when(commandRepository.save(any(LockerCommand.class))).thenAnswer(i -> i.getArgument(0));

        // when
        LockerCommand command = lockerService.requestUnlock(1L, 200L);

        // then
        assertEquals(LockerStatus.IN_USE, emptyLocker.getStatus());
        assertEquals(validItem, emptyLocker.getCurrentItem());
        assertEquals(ItemStatus.IN_LOCKER, validItem.getStatus());

        assertEquals(LockerCommandType.OPEN, command.getCommand());
        assertEquals(LockerCommandStatus.PENDING, command.getStatus());
        verify(commandRepository, times(1)).save(any(LockerCommand.class));
    }

    @Test
    @DisplayName("사물함 열기 요청 - 보관 실패 (잘못된 아이템 상태)")
    void requestUnlock_Storage_Fail_InvalidItemStatus() {
        // given
        validItem.setStatus(ItemStatus.RETURNED); // REPORTED가 아님
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(emptyLocker));
        when(itemRepository.findById(200L)).thenReturn(Optional.of(validItem));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> lockerService.requestUnlock(1L, 200L));
        assertEquals("보관 가능한 상태의 물품이 아닙니다.", exception.getClientMessage());
    }

    @Test
    @DisplayName("사물함 열기 요청 - 회수 (물건이 있는 사물함)")
    void requestUnlock_Retrieval_Success() {
        // given
        when(lockerRepository.findById(2L)).thenReturn(Optional.of(fullLocker));
        when(commandRepository.save(any(LockerCommand.class))).thenAnswer(i -> i.getArgument(0));

        // when
        LockerCommand command = lockerService.requestUnlock(2L, null); // 회수 시에는 itemId 불필요

        // then
        assertEquals(LockerStatus.EMPTY, fullLocker.getStatus());
        assertNull(fullLocker.getCurrentItem());
        assertEquals(ItemStatus.RETURNED, storedItem.getStatus());
        assertNotNull(storedItem.getReturnedAt());

        assertEquals(LockerCommandType.OPEN, command.getCommand());
        verify(commandRepository, times(1)).save(any(LockerCommand.class));
    }

    @Test
    @DisplayName("사물함 열기 요청 - 점검 중인 사물함")
    void requestUnlock_Fail_Maintenance() {
        // given
        emptyLocker.setStatus(LockerStatus.MAINTENANCE);
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(emptyLocker));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> lockerService.requestUnlock(1L, 200L));
        assertEquals("사물함이 점검 중입니다.", exception.getClientMessage());
    }

    @Test
    @DisplayName("사물함 닫기 요청 - 성공")
    void requestLock_Success() {
        // given
        when(lockerRepository.findById(1L)).thenReturn(Optional.of(emptyLocker));
        when(commandRepository.save(any(LockerCommand.class))).thenAnswer(i -> i.getArgument(0));

        // when
        LockerCommand command = lockerService.requestLock(1L);

        // then
        assertEquals(LockerCommandType.CLOSE, command.getCommand());
        verify(commandRepository, times(1)).save(any(LockerCommand.class));
    }

    @Test
    @DisplayName("명령 폴링 (Poll) - 성공 시 CONSUMED 상태로 변경")
    void pollNextCommand_Success() {
        // given
        LockerCommand pendingCommand = LockerCommand.builder()
                .id(10L)
                .locker(emptyLocker)
                .command(LockerCommandType.OPEN)
                .status(LockerCommandStatus.PENDING)
                .build();

        when(commandRepository.findFirstByLocker_IdAndStatusOrderByCreatedAtAsc(1L, LockerCommandStatus.PENDING))
                .thenReturn(Optional.of(pendingCommand));

        // when
        Optional<LockerCommand> result = lockerService.pollNextCommand(1L);

        // then
        assertTrue(result.isPresent());
        assertEquals(LockerCommandStatus.CONSUMED, result.get().getStatus());
        assertNotNull(result.get().getConsumedAt());
    }

    @Test
    @DisplayName("명령 완료 처리 (Ack) - 성공 시 COMPLETED 상태로 변경")
    void ackCommand_Success() {
        // given
        LockerCommand consumedCommand = LockerCommand.builder()
                .id(10L)
                .locker(emptyLocker) // lockerId = 1L
                .status(LockerCommandStatus.CONSUMED)
                .build();

        when(commandRepository.findById(10L)).thenReturn(Optional.of(consumedCommand));

        // when
        lockerService.ackCommand(1L, 10L);

        // then
        assertEquals(LockerCommandStatus.COMPLETED, consumedCommand.getStatus());
        assertNotNull(consumedCommand.getCompletedAt());
    }

    @Test
    @DisplayName("명령 완료 처리 (Ack) - 다른 사물함의 명령인 경우 예외 발생")
    void ackCommand_Fail_LockerMismatch() {
        // given
        LockerCommand consumedCommand = LockerCommand.builder()
                .id(10L)
                .locker(fullLocker) // lockerId = 2L
                .build();

        when(commandRepository.findById(10L)).thenReturn(Optional.of(consumedCommand));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> lockerService.ackCommand(1L, 10L));
        assertEquals("잘못된 요청입니다.", exception.getClientMessage());
    }
}