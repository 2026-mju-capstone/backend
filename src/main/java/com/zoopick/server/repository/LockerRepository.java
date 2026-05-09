package com.zoopick.server.repository;

import com.zoopick.server.entity.Item;
import com.zoopick.server.entity.Locker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerRepository extends JpaRepository<Locker, Long> {
    Locker findLockerByCurrentItem(Item currentItem);
}
