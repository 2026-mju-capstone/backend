package com.zoopick.server.repository;

import com.zoopick.server.entity.ChatRoom;
import com.zoopick.server.entity.ChatRoomStatus;
import com.zoopick.server.exception.DataNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    default ChatRoom findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> DataNotFoundException.from("채팅방", id));
    }

    List<ChatRoom> findByOwnerIdOrFinderId(long ownerId, long finderId);

    default List<ChatRoom> findByParticipant(long userId) {
        return findByOwnerIdOrFinderId(userId, userId);
    }

    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.owner.id = :userId OR cr.finder.id = :userId) AND cr.item.id = :itemId AND cr.status = 'OPEN'")
    Optional<ChatRoom> findOpenByParticipantAndItem(@Param("userId") long userId, @Param("itemId") long itemId);

    long countByOwnerIdOrFinderId(Long ownerId, Long finderId);

    Optional<ChatRoom> findByOwnerIdAndFinderIdIs(long ownerId, long finderId);

    Optional<ChatRoom> findByOwnerIdAndFinderIdIsAndStatus(long ownerId, long finderId, ChatRoomStatus status);
}
