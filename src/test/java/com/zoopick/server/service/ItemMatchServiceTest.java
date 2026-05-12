package com.zoopick.server.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.zoopick.server.dto.match.MatchManualRequest;
import com.zoopick.server.dto.match.MatchManualResponse;
import com.zoopick.server.dto.match.SimilarItemProjection;
import com.zoopick.server.entity.*;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.repository.ItemMatchRepository;
import com.zoopick.server.repository.ItemPostRepository;
import com.zoopick.server.repository.ItemRepository;
import com.zoopick.server.repository.LockerRepository;
import com.zoopick.server.service.notification.NotificationService;
import com.zoopick.server.service.notification.SendNotificationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemMatchServiceTest {

    @InjectMocks
    private ItemMatchService itemMatchService;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMatchRepository itemMatchRepository;
    @Mock
    private LockerRepository lockerRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ItemPostRepository itemPostRepository;

    private User user;
    private Item lostItem;
    private Item foundItem;
    private ItemMatch itemMatch;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입
        ReflectionTestUtils.setField(itemMatchService, "similarityThreshold", 0.8f);

        user = User.builder().id(1L).build();

        lostItem = Item.builder()
                .id(100L)
                .reporter(user)
                .type(ItemType.LOST)
                .category(ItemCategory.BAG)
                .color(ItemColor.BLACK)
                .embedding(new float[]{0.1f, 0.2f})
                .status(ItemStatus.REPORTED)
                .build();

        foundItem = Item.builder()
                .id(200L)
                .reporter(user)
                .type(ItemType.FOUND)
                .category(ItemCategory.BAG)
                .color(ItemColor.BLACK)
                .embedding(new float[]{0.1f, 0.2f})
                .status(ItemStatus.REPORTED)
                .build();

        itemMatch = ItemMatch.builder()
                .id(10L)
                .lostItem(lostItem)
                .foundItem(foundItem)
                .status(MatchStatus.CANDIDATE)
                .score(0.95f)
                .build();
    }

    @Test
    @DisplayName("자동 매칭 생성 - 성공 (알림 발송 포함)")
    void createMatch_success() throws FirebaseMessagingException {
        // given
        SimilarItemProjection projectionMock = mock(SimilarItemProjection.class);
        when(projectionMock.getItemId()).thenReturn(200L);
        when(projectionMock.getScore()).thenReturn(0.95);

        ItemPost lostItemPost = ItemPost.builder().title("분실물 게시글").build();

        when(itemRepository.findByIdOrThrow(100L)).thenReturn(lostItem);
        when(itemMatchRepository.findSimilarItems(any(), anyString(), anyString(), anyString(), eq(1L), eq(0.8f)))
                .thenReturn(List.of(projectionMock));
        when(itemRepository.findByIdOrThrow(200L)).thenReturn(foundItem);
        when(itemMatchRepository.existsByLostItemAndFoundItem(lostItem, foundItem)).thenReturn(false);
        when(itemMatchRepository.save(any(ItemMatch.class))).thenReturn(itemMatch);
        when(itemPostRepository.findByItem(lostItem)).thenReturn(lostItemPost);

        // when
        itemMatchService.createMatch(100L);

        // then
        verify(itemMatchRepository, times(1)).save(any(ItemMatch.class));
        verify(notificationService, times(1)).send(eq(user), any(SendNotificationCommand.class));
        assertEquals(MatchStatus.NOTIFIED, itemMatch.getStatus());
    }

    @Test
    @DisplayName("자동 매칭 생성 - 매칭된 유사 아이템이 없는 경우")
    void createMatch_noSimilarItems() throws FirebaseMessagingException{
        // given
        when(itemRepository.findByIdOrThrow(100L)).thenReturn(lostItem);
        when(itemMatchRepository.findSimilarItems(any(), anyString(), anyString(), anyString(), eq(1L), eq(0.8f)))
                .thenReturn(List.of());

        // when
        itemMatchService.createMatch(100L);

        // then
        verify(itemMatchRepository, never()).save(any());
        verify(notificationService, never()).send(any(), any());
    }

    @Test
    @DisplayName("매칭 확정 - 성공 (아이템 상태 변경 및 타 매칭 거절 처리)")
    void confirmMatch_success() {
        // given
        when(itemMatchRepository.findByIdOrThrow(10L)).thenReturn(itemMatch);
        when(itemMatchRepository.existsByLostItemAndStatus(lostItem, MatchStatus.CONFIRMED)).thenReturn(false);
        when(itemMatchRepository.existsByFoundItemAndStatus(foundItem, MatchStatus.CONFIRMED)).thenReturn(false);

        // when
        itemMatchService.confirmMatch(10L);

        // then
        assertEquals(MatchStatus.CONFIRMED, itemMatch.getStatus());
        assertEquals(ItemStatus.MATCHED, lostItem.getStatus());
        assertEquals(ItemStatus.MATCHED, foundItem.getStatus());
        verify(itemMatchRepository, times(1)).rejectOthersByLostItem(10L, 100L, 200L);
    }

    @Test
    @DisplayName("매칭 확정 - 이미 확정된 매칭이 존재하는 경우 예외 발생")
    void confirmMatch_fail_alreadyConfirmed() {
        // given
        when(itemMatchRepository.findByIdOrThrow(10L)).thenReturn(itemMatch);
        when(itemMatchRepository.existsByLostItemAndStatus(lostItem, MatchStatus.CONFIRMED)).thenReturn(true);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> itemMatchService.confirmMatch(10L));
        assertEquals("이미 주인을 찾은 물품이 있습니다.", exception.getClientMessage());
    }

    @Test
    @DisplayName("수동 매칭 - 보관함(Locker) 아이템인 경우 성공")
    void matchManual_locker_success() {
        // given
        MatchManualRequest request = mock(MatchManualRequest.class);
        when(request.getLostItemId()).thenReturn(100L);
        when(request.getFoundItemId()).thenReturn(200L);

        foundItem.setStatus(ItemStatus.IN_LOCKER); // 보관함 상태
        Locker locker = Locker.builder().id(5L).build();

        when(itemRepository.findByIdOrThrow(100L)).thenReturn(lostItem);
        when(itemRepository.findByIdOrThrow(200L)).thenReturn(foundItem);
        when(itemMatchRepository.existsByLostItemAndFoundItem(lostItem, foundItem)).thenReturn(false);
        when(itemMatchRepository.save(any(ItemMatch.class))).thenReturn(itemMatch);
        when(lockerRepository.findLockerByCurrentItem(foundItem)).thenReturn(locker);

        // when
        MatchManualResponse response = itemMatchService.matchManual(request);

        // then
        assertEquals(MatchManualType.LOCKER, response.getMatchManualType());
        assertEquals(5L, response.getLockerId());
        verify(itemMatchRepository, times(1)).rejectOthersByLostItem(itemMatch.getId(), 100L, 200L);
    }

    @Test
    @DisplayName("수동 매칭 - 채팅(Chat) 아이템인 경우 성공")
    void matchManual_chat_success() {
        // given
        MatchManualRequest request = mock(MatchManualRequest.class);
        when(request.getLostItemId()).thenReturn(100L);
        when(request.getFoundItemId()).thenReturn(200L);

        when(itemRepository.findByIdOrThrow(100L)).thenReturn(lostItem);
        when(itemRepository.findByIdOrThrow(200L)).thenReturn(foundItem);
        when(itemMatchRepository.existsByLostItemAndFoundItem(lostItem, foundItem)).thenReturn(false);
        when(itemMatchRepository.save(any(ItemMatch.class))).thenReturn(itemMatch);

        // when
        MatchManualResponse response = itemMatchService.matchManual(request);

        // then
        assertEquals(MatchManualType.CHAT, response.getMatchManualType());
        assertNull(response.getLockerId());
    }
}