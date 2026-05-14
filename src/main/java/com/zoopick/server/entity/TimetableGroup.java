package com.zoopick.server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "timetable_groups", schema = "zoopick")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TimetableGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
