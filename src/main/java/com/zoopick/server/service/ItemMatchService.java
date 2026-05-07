package com.zoopick.server.service;

import com.zoopick.server.dto.item.SimilarItemResult;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemMatch;
import com.zoopick.server.entity.ItemType;
import com.zoopick.server.entity.MatchStatus;
import com.zoopick.server.repository.ItemMatchRepository;
import com.zoopick.server.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemMatchService {
    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    @Value("${zoopick.similarity.threshold}")
    private float similarityThreshold;
    public void createMatch(Long itemId) {
        Item targetItem = itemRepository.findByIdOrThrow(itemId); // 게시글이 올라간 아이템
        String embedding = Arrays.toString(targetItem.getEmbedding()).replace(", ", ",");
        List<SimilarItemResult> similarItems = itemMatchRepository.findSimilarItems(embedding,
                                                                               targetItem.getType().name(),
                                                                               targetItem.getCategory().name(),
                                                                               targetItem.getColor().name(),
                                                                               similarityThreshold);
        if (!similarItems.isEmpty()) {
            for (SimilarItemResult similarItemResult : similarItems) {
                Item foundItemInDb = itemRepository.findByIdOrThrow(similarItemResult.getItemId());
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
    }
}
