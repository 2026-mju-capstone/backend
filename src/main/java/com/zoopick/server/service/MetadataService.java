package com.zoopick.server.service;

import com.zoopick.server.entity.Building;
import com.zoopick.server.entity.ItemCategory;
import com.zoopick.server.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@NullMarked
public class MetadataService {
    private final BuildingRepository buildingRepository;

    public List<String> getBuildings() {
        List<Building> buildings = buildingRepository.findAll();
        return buildings.stream()
                .map(Building::getName)
                .toList();
    }

    public List<String> getItemCategories() {
        return Arrays.stream(ItemCategory.values())
                .map(ItemCategory::name)
                .toList();
    }
}
