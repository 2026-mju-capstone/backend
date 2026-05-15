package com.zoopick.server.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "cctv_detection_matches", schema = "zoopick")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CctvDetectionMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detection_id", nullable = false)
    private CctvDetection cctvDetection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    private float score;

    @PrePersist
    @PreUpdate
    public void formatScore() {
        // 저장 및 수정 직전에 0.85714...를 0.857로 변환
        this.score = Math.round(this.score * 1000f) / 1000f;
    }
}