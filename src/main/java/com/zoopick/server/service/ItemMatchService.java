package com.zoopick.server.service;

import com.zoopick.server.dto.match.ItemMatchResultResponse;
import com.zoopick.server.dto.match.MatchManualRequest;
import com.zoopick.server.dto.match.MatchManualResponse;
import com.zoopick.server.dto.match.SimilarItemResult;
import com.zoopick.server.entity.*;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.repository.ItemMatchRepository;
import com.zoopick.server.repository.ItemRepository;
import com.zoopick.server.repository.LockerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Vector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemMatchService {
    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final LockerRepository lockerRepository;
    @Value("${zoopick.similarity.threshold}")
    private float similarityThreshold;
    public void createMatch(Long itemId) {
        log.info("매칭 시작 ID: {}", itemId);
        Item targetItem = itemRepository.findByIdOrThrow(itemId); // 게시글이 올라간 아이템
        Vector embedding = Vector.of(targetItem.getEmbedding());
        List<SimilarItemResult> similarItems = itemMatchRepository.findSimilarItems(
                        embedding,
                        targetItem.getType().name(),
                        targetItem.getCategory().name(),
                        targetItem.getColor().name(),
                        similarityThreshold)
                .stream()
                .map(p -> new SimilarItemResult(p.getItemId(), p.getScore()))
                .toList();
        if (!similarItems.isEmpty()) {
            for (SimilarItemResult similarItemResult : similarItems) {
                Item foundItemInDb = itemRepository.findByIdOrThrow(similarItemResult.getItemId());
                log.info("매칭된 아이템 ID: {}", foundItemInDb.getId());
                // 게시글에 올라온 아이템이 LOST라면 lostItem에, FOUND라면 foundItem에
                Item lostItem = targetItem.getType() == ItemType.LOST ? targetItem : foundItemInDb;
                Item foundItem = targetItem.getType() == ItemType.LOST ? foundItemInDb : targetItem;
                ItemMatch itemMatch = ItemMatch
                        .builder()
                        .score((float) similarItemResult.getScore())
                        .lostItem(lostItem)
                        .foundItem(foundItem)
                        .status(MatchStatus.CANDIDATE)
                        .build();
                // 중복 저장 방지
                if (!itemMatchRepository.existsByLostItemAndFoundItem(lostItem, foundItem))
                    itemMatchRepository.save(itemMatch);
            }
        }
        else log.warn("매칭된 아이템이 없습니다.");
        log.info("매칭 종료 ID: {}", targetItem.getId());
    }

    public List<ItemMatchResultResponse> getItemMatchResult(Long userId) {
        return itemMatchRepository.itemMatchesByLostItem(userId)
                .stream()
                .map(p -> new ItemMatchResultResponse(
                        p.getMatchId(),
                        p.getFoundItemId(),
                        p.getFoundPostId(),
                        p.getFoundPostTitle(),
                        p.getFoundImageUrl(),
                        p.getLocationName(),
                        p.getFoundNickname(),
                        p.getFoundDepartment(),
                        p.getScore(),
                        MatchStatus.valueOf(p.getStatus())
                ))
                .toList();
    }

    public void confirmMatch(Long matchId) {
        ItemMatch itemMatch = itemMatchRepository.findByIdOrThrow(matchId);
        Item lostItem = itemMatch.getLostItem();
        Item foundItem = itemMatch.getFoundItem();

        lostItem.setStatus(ItemStatus.MATCHED);
        foundItem.setStatus(ItemStatus.MATCHED);

        itemMatch.setStatus(MatchStatus.CONFIRMED);
        log.info("매칭 CONFIRMED ID: {}", matchId);
    }

    public void rejectMatch(Long matchId) {
        ItemMatch itemMatch = itemMatchRepository.findByIdOrThrow(matchId);
        itemMatch.setStatus(MatchStatus.REJECTED);
        log.info("매칭 REJECTED ID: {}", matchId);
    }

    public MatchManualResponse matchManual(MatchManualRequest request) {
        log.info("수동 매칭 시작: {} <-> {}", request.getLostItemId(), request.getFoundItemId());
        Item lostItem = itemRepository.findByIdOrThrow(request.getLostItemId());
        Item foundItem = itemRepository.findByIdOrThrow(request.getFoundItemId());

        if (itemMatchRepository.existsByLostItemAndFoundItem(lostItem, foundItem)) {
            throw new BadRequestException("이미 진행중인 매칭입니다.");
        }
        if (itemMatchRepository.existsByLostItemAndStatus(lostItem, MatchStatus.CONFIRMED) ||
                itemMatchRepository.existsByFoundItemAndStatus(foundItem, MatchStatus.CONFIRMED)) {
            throw new BadRequestException("이미 주인을 찾은 물품이 있습니다.");
        }

        ItemMatch itemMatch = ItemMatch.builder() //새로운 매칭 생성
                .lostItem(lostItem)
                .foundItem(foundItem)
                .score(1.0f)
                .status(MatchStatus.CONFIRMED)
                .build();
        itemMatchRepository.save(itemMatch);
        itemMatchRepository.rejectOthersByLostItem(itemMatch.getId(), lostItem.getId(), foundItem.getId());
        log.info("매칭 저장 완료 ID: {}", itemMatch.getId());

        //LOCKER, CHAT 분기
        if (foundItem.getStatus().equals(ItemStatus.IN_LOCKER)) {
            Locker locker = lockerRepository.findLockerByCurrentItem(foundItem);
            log.info("매칭 LOCKER {} <-> {}", request.getLostItemId(), request.getFoundItemId());
            return MatchManualResponse.builder()
                    .matchId(itemMatch.getId())
                    .matchManualType(MatchManualType.LOCKER)
                    .lockerId(locker.getId())
                    .build();
        } else {
            log.info("매칭 CHAT {} <-> {}", request.getLostItemId(), request.getFoundItemId());
            return MatchManualResponse.builder()
                    .matchId(itemMatch.getId())
                    .matchManualType(MatchManualType.CHAT)
                    .build();
        }
    }
}
