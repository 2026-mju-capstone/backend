package com.zoopick.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "cctv_detections", schema = "zoopick")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CctvDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private CctvVideo cctvVideo;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "detected_category", columnDefinition = "item_category")
    private ItemCategory detectedCategory;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "detected_color", columnDefinition = "item_color")
    private ItemColor detectedColor;

    @Column(columnDefinition = "vector(512)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 512)
    private float[] embedding;

    @Column(name = "item_snapshot_url", length = 500)
    private String itemSnapshotUrl;

    @Column(name = "moment_snapshot_url", length = 500)
    private String momentSnapshotUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}