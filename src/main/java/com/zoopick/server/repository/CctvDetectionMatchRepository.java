package com.zoopick.server.repository;

import com.zoopick.server.dto.cctv.MatchedLostItems;
import com.zoopick.server.dto.match.SimilarItemProjection;
import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.CctvDetectionMatch;
import com.zoopick.server.entity.Item;
import org.springframework.data.domain.Vector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
          AND i.theft_suspected_at IS NULL
          AND i.category = CAST(:category AS item_category)
          AND i.reported_at <= (CAST(:detectionTime AS timestamp) + interval '6 hours')
        ORDER BY i.embedding <=> CAST(:embedding AS vector)
        LIMIT 100
    ) t
    WHERE t.score >= :threshold
        LIMIT 30
    """, nativeQuery = true)
    List<SimilarItemProjection> findLostItems(@Param("embedding") Vector embedding,
                                                 @Param("category") String category,
                                                 @Param("detectionTime") LocalDateTime detectionTime,
                                                 @Param("threshold") float threshold);

    @Query(value = """
    SELECT *
    FROM (
        SELECT
            d.id AS itemId,
            1 - (d.embedding <=> CAST(:embedding AS vector)) AS score
        FROM zoopick.cctv_detections d
        JOIN zoopick.cctv_videos v ON d.video_id = v.id
        WHERE d.detected_category = CAST(:category AS item_category)
          AND v.room_id IN (:roomIds)
          AND v.recorded_at >= :lostTime
        ORDER BY d.embedding <=> CAST(:embedding AS vector)
        LIMIT 100
    ) t
    WHERE t.score >= :threshold
        LIMIT 30
    """, nativeQuery = true)
    List<SimilarItemProjection> findDetections(@Param("embedding") Vector embedding,
                                              @Param("category") String category,
                                              @Param("roomIds") List<Long> roomIds,
                                              @Param("lostTime") LocalDateTime lostTime,
                                              @Param("threshold") float threshold);

    // 중복 체크
    boolean existsByCctvDetectionAndItem(CctvDetection detection, Item lostItem);


    @Query("""
     SELECT new com.zoopick.server.dto.cctv.MatchedLostItems(
          i.id,
          p.title,
          i.category,
          CAST(COUNT(m.id) AS int),
          i.reportedAt,
          i.imageUrl
          )
     FROM ItemPost p
     JOIN p.item i
     LEFT JOIN CctvDetectionMatch m ON m.item.id = i.id
     WHERE p.user.id = :userId
     GROUP BY i.id, p.title, i.category, i.reportedAt, i.imageUrl
     HAVING COUNT(m.id) > 0
     """)
    List<MatchedLostItems> findCctvDetectionByUserId(@Param("userId") Long userId);
}
