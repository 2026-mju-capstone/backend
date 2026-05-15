package com.zoopick.server.repository;

import com.zoopick.server.dto.match.SimilarItemProjection;
import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.CctvDetectionMatch;
import com.zoopick.server.entity.Item;
import org.springframework.data.domain.Vector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CctvDetectionMatchRepository extends JpaRepository<CctvDetectionMatch, Long> {
    @Query(value = """
    SELECT *
    FROM (
        SELECT
            i.id AS itemId,
            1 - (i.embedding <=> CAST(:embedding AS vector)) AS score
        FROM zoopick.items i
        WHERE i.type = CAST('LOST' AS item_type)
          AND i.returned_at IS NULL
          AND i.category = CAST(:category AS item_category)
        ORDER BY i.embedding <=> CAST(:embedding AS vector)
        LIMIT 100
    ) t
    WHERE t.score >= :threshold
        LIMIT 30
    """, nativeQuery = true)
    List<SimilarItemProjection> findLostItems(@Param("embedding") Vector embedding,
                                                 @Param("category") String category,
                                                 @Param("threshold") float threshold);

    @Query(value = """
    SELECT *
    FROM (
        SELECT
            d.id AS itemId,
            1 - (d.embedding <=> CAST(:embedding AS vector)) AS score
        FROM zoopick.cctv_detections d
        WHERE d.detected_category = CAST(:category AS item_category)
        ORDER BY d.embedding <=> CAST(:embedding AS vector)
        LIMIT 100
    ) t
    WHERE t.score >= :threshold
        LIMIT 30
    """, nativeQuery = true)
    List<SimilarItemProjection> findDetections(@Param("embedding") Vector embedding,
                                              @Param("category") String category,
                                              @Param("threshold") float threshold);

    // 중복 체크
    boolean existsByCctvDetectionAndItem(CctvDetection detection, Item lostItem);
}
