package com.zoopick.server.repository;

import com.zoopick.server.entity.Building;
import com.zoopick.server.entity.Room;
import com.zoopick.server.exception.DataNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    default Room findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> DataNotFoundException.from("공간", id));
    }

    Optional<Room> findByNameAndBuilding(String name, Building building);

    List<Room> findAllByBuilding(Building building);
}
