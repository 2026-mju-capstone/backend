package com.zoopick.server.service;

import com.zoopick.server.entity.*;
import com.zoopick.server.exception.BadRequestException;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.exception.ForbiddenException;
import com.zoopick.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockerService {

    private final LockerRepository lockerRepository;
    private final LockerCommandRepository commandRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public LockerCommand requestUnlock(Long userId, Long lockerId, Long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> DataNotFoundException.from("사용자", userId));
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> DataNotFoundException.from("사물함", lockerId));

        if (locker.getStatus() == LockerStatus.MAINTENANCE) {
            throw new BadRequestException(
                    "사물함이 점검 중입니다.",
                    "Locker " + lockerId + " is under maintenance");
        }

        boolean isStorage = (locker.getCurrentItem() == null);

        if (isStorage) {
            handleStorage(userId, locker, itemId);
        } else {
            handleRetrieval(userId, locker);
        }

        return enqueue(user, locker, LockerCommandType.OPEN);
    }

    private void handleStorage(Long userId, Locker locker, Long itemId) {
        if (itemId == null) {
            throw new BadRequestException(
                    "보관할 물품 정보가 필요합니다.",
                    "itemId is required for storage flow");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> DataNotFoundException.from("물품", itemId));

        //본인이 신고한 습득물인지 확인
        if (!item.getReporter().getId().equals(userId)) {
            throw new ForbiddenException(
                    "본인이 신고한 물품만 보관할 수 있습니다.",
                    "User " + userId + " is not the reporter of item " + itemId);
        }

        if (item.getType() != ItemType.FOUND) {
            throw new BadRequestException(
                    "습득물만 사물함에 보관할 수 있습니다.",
                    "Item " + itemId + " type is " + item.getType());
        }
        if (item.getStatus() != ItemStatus.REPORTED) {
            throw new BadRequestException(
                    "보관 가능한 상태의 물품이 아닙니다.",
                    "Item " + itemId + " status is " + item.getStatus());
        }

        locker.setStatus(LockerStatus.IN_USE);
        locker.setCurrentItem(item);
        item.setStatus(ItemStatus.IN_LOCKER);

        log.info("[STORE] locker_id={} item_id={} user_id={} 보관 요청",
                locker.getId(), itemId, userId);
    }

    private void handleRetrieval(Long userId, Locker locker) {
        Item stored = locker.getCurrentItem();

        //권한 확인: AI 매칭 CONFIRMED 소유자이거나, 채팅 RESOLVED_RETURNED된 분실자여야 함
        boolean isMatchedOwner = itemMatchRepository.existsByFoundItemAndLostItem_Reporter_IdAndStatus(
                stored, userId, MatchStatus.CONFIRMED);
        boolean isChatOwner = chatRoomRepository.existsByItemAndOwnerIdAndStatus(stored, userId, ChatRoomStatus.RESOLVED_RETURNED);

        if (!isMatchedOwner && !isChatOwner) {
            throw new ForbiddenException(
                    "물품을 회수할 권한이 없습니다.",
                    "User " + userId + " has no permission to retrieve item " + stored.getId());
        }

        LocalDateTime now = LocalDateTime.now();

        locker.setStatus(LockerStatus.EMPTY);
        locker.setCurrentItem(null);
        stored.setStatus(ItemStatus.RETURNED);
        stored.setReturnedAt(now);

        log.info("[RETRIEVE] locker_id={} item_id={} user_id={} 회수 요청",
                locker.getId(), stored.getId(), userId);
    }

    @Transactional
    public LockerCommand requestLock(Long userId, Long lockerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> DataNotFoundException.from("사용자", userId));
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> DataNotFoundException.from("사물함", lockerId));

        //잠금 권한 확인: 가장 최근에 이 사물함을 연(OPEN) 사람만 잠글 수 있음
        commandRepository.findFirstByLocker_IdAndCommandOrderByCreatedAtDesc(lockerId, LockerCommandType.OPEN)
                .ifPresent(lastOpenCmd -> {
                    if (!lastOpenCmd.getIssuedBy().getId().equals(userId)) {
                        throw new ForbiddenException(
                                "사물함을 잠글 권한이 없습니다 (열었던 사용자만 가능).",
                                "User " + userId + " is not the issuer of the last OPEN command");
                    }
                });

        log.info("[CLOSE] locker_id={} user_id={} 잠금 요청", lockerId, userId);
        return enqueue(user, locker, LockerCommandType.CLOSE);
    }

    private LockerCommand enqueue(User user, Locker locker, LockerCommandType type) {
        return commandRepository.save(LockerCommand.builder()
                .locker(locker)
                .command(type)
                .issuedBy(user)
                .status(LockerCommandStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public Optional<LockerCommand> pollNextCommand(Long lockerId) {
        Optional<LockerCommand> next = commandRepository
                .findFirstByLocker_IdAndStatusOrderByCreatedAtAsc(
                        lockerId, LockerCommandStatus.PENDING);

        next.ifPresent(cmd -> {
            cmd.setStatus(LockerCommandStatus.CONSUMED);
            cmd.setConsumedAt(LocalDateTime.now());
            log.info("[POLL] locker_id={} command_id={} type={} consumed",
                    lockerId, cmd.getId(), cmd.getCommand());
        });
        return next;
    }

    @Transactional
    public void ackCommand(Long lockerId, Long commandId) {
        LockerCommand cmd = commandRepository.findById(commandId)
                .orElseThrow(() -> DataNotFoundException.from("명령", commandId));

        if (!cmd.getLocker().getId().equals(lockerId)) {
            throw new BadRequestException(
                    "잘못된 요청입니다.",
                    "command " + commandId + " does not belong to locker " + lockerId);
        }

        cmd.setStatus(LockerCommandStatus.COMPLETED);
        cmd.setCompletedAt(LocalDateTime.now());
        log.info("[ACK] locker_id={} command_id={} type={} completed",
                lockerId, commandId, cmd.getCommand());
    }
}