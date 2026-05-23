package com.zoopick.server.locker.entity;

import com.zoopick.server.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lockers", schema = "zoopick")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locker {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "locker_status")
    private LockerStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_item_id")
    private Item currentItem;   // 보관 중인 아이템 없으면 null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_item_id")
    private Item pendingItem;   // unlock 후 아직 lock 안 된 아이템
}
