package com.zoopick.server.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cctv_video_progress", schema = "zoopick")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CctvVideoProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private CctvVideo cctvVideo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "video_analysis_status")
    @Builder.Default
    private VideoAnalysisStatus status = VideoAnalysisStatus.PENDING;

    @Column(name = "total_duration_seconds", nullable = false)
    private Integer totalDurationSeconds;

    @Column(name = "analyzed_seconds", nullable = false)
    @Builder.Default
    private Integer analyzedSeconds = 0;

    @Column(name = "estimated_completion_at")
    private LocalDateTime estimatedCompletionAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "last_updated_at")
    @LastModifiedDate
    private LocalDateTime lastUpdatedAt;
}
