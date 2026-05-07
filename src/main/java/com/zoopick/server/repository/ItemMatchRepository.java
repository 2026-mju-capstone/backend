package com.zoopick.server.repository;

import com.zoopick.server.dto.item.SimilarItemResult;
import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.ItemMatch;
import org.springframework.data.domain.Vector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    @Query(value = """
    SELECT i.id as itemId, (1 - (i.embedding <=> CAST(:embedding AS vector))) as score
    FROM zoopick.items i
    WHERE i.type <> CAST(:excludeType AS item_type)
      AND i.returned_at IS NULL
      AND i.category = CAST(:category AS item_category)
      AND i.color = CAST(:color AS item_color)
      AND (1 - (i.embedding <=> CAST(:embedding AS vector))) >= :threshold
    """, nativeQuery = true)
    List<SimilarItemResult> findSimilarItems(@Param("embedding") Vector embedding,
                                             @Param("excludeType") String excludeType,
                                             @Param("category") String category,
                                             @Param("color") String color,
                                             @Param("threshold") float threshold);

    // 중복 체크
    boolean existsByLostItemAndFoundItem(Item lostItem, Item foundItem);
}
