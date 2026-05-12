package com.zoopick.server.repository;

import com.zoopick.server.entity.LockerCommand;
import com.zoopick.server.entity.LockerCommandStatus;
import com.zoopick.server.entity.LockerCommandType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface LockerCommandRepository extends JpaRepository<LockerCommand, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LockerCommand> findFirstByLocker_IdAndStatusOrderByCreatedAtAsc(
            Long lockerId, LockerCommandStatus status);

    // 해당 사물함에 내려진 가장 최신 명령을 조회 (주로 OPEN 명령을 내린 사람을 확인하기 위함)
    Optional<LockerCommand> findFirstByLocker_IdAndCommandOrderByCreatedAtDesc(
            Long lockerId, LockerCommandType command);
}