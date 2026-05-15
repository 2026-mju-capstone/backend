package com.zoopick.server.service;

import com.zoopick.server.dto.match.CreateCctvMatchEvent;
import com.zoopick.server.dto.match.SimilarItemResult;
import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.CctvDetectionMatch;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemPost;
import com.zoopick.server.repository.CctvDetectionMatchRepository;
import com.zoopick.server.repository.CctvDetectionRepository;
import com.zoopick.server.repository.ItemPostRepository;
import com.zoopick.server.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Vector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CctvMatchService {
    private final CctvDetectionRepository cctvDetectionRepository;
    private final CctvDetectionMatchRepository cctvDetectionMatchRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ItemPostRepository itemPostRepository;
    @Value("${zoopick.similarity.threshold}")
    private float similarityThreshold;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void matchCctvToLostItems(Long detectionId) {
        log.info("[CCTV] 매칭 시작 ID: {}", detectionId);
        CctvDetection cctvDetection = cctvDetectionRepository.findById(detectionId).orElse(null);
        Vector embedding = Vector.of(cctvDetection.getEmbedding());
        List<SimilarItemResult> similarItems = cctvDetectionMatchRepository.findLostItems(
                        embedding,
                        cctvDetection.getDetectedCategory().name(),
                        similarityThreshold)
                .stream()
                .map(p -> new SimilarItemResult(p.getItemId(), p.getScore()))
                .toList();

        if (similarItems.isEmpty()) {
            log.warn("[CCTV] 매칭된 아이템이 없습니다.");
            return;
        }
        List<Long> itemIds = similarItems.stream()
                .map(SimilarItemResult::getItemId)
                .toList();

        Map<Long, ItemPost> itemPostMap = itemPostRepository.findAllByItemIdsWithItem(itemIds)
                .stream()
                .collect(Collectors.toMap(
                        post -> post.getItem().getId(), // Key: Item의 ID
                        post -> post                   // Value: ItemPost 객체 (안에 Item이 들어있음)
                ));

        for (SimilarItemResult s : similarItems) {
            // 중복 저장 방지
            ItemPost itemPost = itemPostMap.get(s.getItemId());
            Item foundItemInDb = itemPost.getItem();
            if (!cctvDetectionMatchRepository.existsByCctvDetectionAndItem(cctvDetection, foundItemInDb)) {
                CctvDetectionMatch savedMatch = cctvDetectionMatchRepository.save(CctvDetectionMatch.builder()
                        .score((float) s.getScore())
                        .item(foundItemInDb)
                        .cctvDetection(cctvDetection)
                        .build());
                log.info("CCTV 매칭된 아이템 ID: {}", foundItemInDb.getId());
                //TODO: 이벤트 퍼블리싱으로 FCM 보내기
                eventPublisher.publishEvent(new CreateCctvMatchEvent(foundItemInDb, savedMatch, cctvDetection, itemPost));
            }
        }
        log.info("[CCTV] 매칭 종료 ID: {}", detectionId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void matchLostItemsToCctv(Long lostItemId) {
        log.info("[CCTV] 매칭 시작 ID: {}", lostItemId);
        Item lostItem = itemRepository.findByIdOrThrow(lostItemId);
        ItemPost lostItemPost = itemPostRepository.findByItem(lostItem);

        Vector embedding = Vector.of(lostItem.getEmbedding());
        List<SimilarItemResult> similarItems = cctvDetectionMatchRepository.findDetections(
                        embedding,
                        lostItem.getCategory().name(),
                        similarityThreshold)
                .stream()
                .map(p -> new SimilarItemResult(p.getItemId(), p.getScore()))
                .toList();

        if (similarItems.isEmpty()) {
            log.warn("[CCTV] 매칭된 Detection이 없습니다.");
            return;
        }
        Map<Long, CctvDetection> detectionMap = cctvDetectionRepository.findAllById(
                        similarItems.stream().map(SimilarItemResult::getItemId).toList())
                .stream()
                .collect(Collectors.toMap(CctvDetection::getId, i -> i));

        for (SimilarItemResult s : similarItems) {
            // 중복 저장 방지
            CctvDetection foundItemInDb = detectionMap.get(s.getItemId());
            if (foundItemInDb == null) {
                log.error("검색 결과에는 있으나 DB 조회에 실패한 Detection ID: {}", s.getItemId());
                continue;
            }
            if (!cctvDetectionMatchRepository.existsByCctvDetectionAndItem(foundItemInDb, lostItem)) {
                CctvDetectionMatch savedMatch = cctvDetectionMatchRepository.save(CctvDetectionMatch.builder()
                        .score((float) s.getScore())
                        .item(lostItem)
                        .cctvDetection(foundItemInDb)
                        .build());
                log.info("[CCTV] 매칭된 Detection ID: {}", foundItemInDb.getId());
                //TODO: 이벤트 퍼블리싱으로 FCM 보내기
                eventPublisher.publishEvent(new CreateCctvMatchEvent(lostItem, savedMatch, foundItemInDb, lostItemPost));
            }
        }
        log.info("[CCTV] 매칭 종료 ID: {}", lostItemId);
    }
}