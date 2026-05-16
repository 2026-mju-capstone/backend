package com.zoopick.server.repository;

import com.zoopick.server.dto.cctv.CctvDetectionDetail;
import com.zoopick.server.dto.cctv.MatchedLostItems;
import com.zoopick.server.entity.CctvDetection;
import com.zoopick.server.entity.DetectionReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CctvDetectionRepository extends JpaRepository<CctvDetection, Long> {
    List<CctvDetection> findAllByOrderByDetectedAtAsc();

    @Query("""
     SELECT new com.zoopick.server.dto.cctv.CctvDetectionDetail(
         m.id,
         m.score,
         d.detectedAt,
         b.name,
         r.name,
         d.itemSnapshotUrl,
         d.momentSnapshotUrl
     )
     FROM CctvDetectionMatch m
     JOIN m.item i
     JOIN m.cctvDetection d
     JOIN d.cctvVideo v
     JOIN v.room r
     JOIN r.building b
     WHERE i.id = :itemId
       AND i.reporter.id = :userId
       AND m.reviewStatus = :status
""")
    List<CctvDetectionDetail> findCctvDetectionDetail(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("status") DetectionReviewStatus status);

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
     JOIN CctvDetectionMatch m ON m.item.id = i.id
     WHERE p.user.id = :userId
       AND m.reviewStatus = :status
     GROUP BY i.id, p.title, i.category, i.reportedAt, i.imageUrl
     """)
    List<MatchedLostItems> findCctvDetectionByUserId(@Param("userId") Long userId, @Param("status") DetectionReviewStatus status);
}
