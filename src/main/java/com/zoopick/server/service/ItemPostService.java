package com.zoopick.server.service;

import com.zoopick.server.dto.item.*;
import com.zoopick.server.entity.*;
import com.zoopick.server.exception.DataNotFoundException;
import com.zoopick.server.exception.ForbiddenException;
import com.zoopick.server.mapper.ItemPostMapper;
import com.zoopick.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@NullMarked
public class ItemPostService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemPostRepository itemPostRepository;
    private final BuildingRepository buildingRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final ItemPostMapper itemPostMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateItemPostResult createItemPost(long userId, CreateItemPostRequest request) {
        User user = userRepository.findByIdOrThrow(userId);
        Building building = buildingRepository.findByIdOrThrow(request.getBuildingId());
        Item item = Item.builder()
                .reporter(user)
                .type(request.getType())
                .status(ItemStatus.REPORTED)
                .category(request.getCategory())
                .color(request.getColor())
                .embedding(null)
                .reportedBuilding(building)
                .locationName(request.getDetailAddress())
                .imageUrl(request.getImageUrl())
                .reportedAt(request.getReportedAt() != null ? request.getReportedAt() : LocalDateTime.now())
                .build();

        Item savedItem = itemRepository.save(item);
        eventPublisher.publishEvent(new ItemCreatedEvent(savedItem.getId(), item.getType()));

        ItemPost itemPost = ItemPost.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .item(savedItem)
                .build();
        ItemPost savedItemPost = itemPostRepository.save(itemPost);
        return new CreateItemPostResult(savedItemPost.getId(), savedItem.getStatus(), "등록되었습니다.");
    }

    public ListItemPostResult getItemPosts(@Nullable ItemPostFilter filter, Pageable pageable) {
        Page<ItemPost> page = itemPostRepository.findAll(ItemPostRepository.applyFilter(filter), pageable);
        List<ItemPostRecord> itemPostRecords = page.stream().map(itemPostMapper::toItemPostRecord)
                .toList();

        return new ListItemPostResult(itemPostRecords, itemPostRecords.size(), page.getNumber());
    }

    public ItemPostRecord getItemPost(long id) {
        ItemPost itemPost = itemPostRepository.findByIdOrThrow(id);
        return itemPostMapper.toItemPostRecord(itemPost);
    }

    public List<ItemPostRecord> getMyItemPosts(long userId, ItemType type) {
        return itemPostRepository.findAllByUser_IdAndItem_Type(userId, type).stream()
                .map(itemPostMapper::toItemPostRecord)
                .toList();
    }

    public ItemOwnerInfoResult getOwnerInfo(long userId, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> DataNotFoundException.from("물품", itemId));

        // [QR-2] 인가 체크: 본인이 신고한 물품이거나, CONFIRMED 매칭된 상대방인지 확인
        boolean isReporter = item.getReporter().getId().equals(userId);
        boolean isMatchedFound = itemMatchRepository.existsByFoundItemAndLostItem_Reporter_IdAndStatus(
                item, userId, MatchStatus.CONFIRMED);
        boolean isMatchedLost = itemMatchRepository.existsByLostItemAndFoundItem_Reporter_IdAndStatus(
                item, userId, MatchStatus.CONFIRMED);

        if (!isReporter && !isMatchedFound && !isMatchedLost) {
            throw new ForbiddenException(
                    "물품 소유자 정보를 볼 권한이 없습니다.",
                    "User " + userId + " is not authorized to see owner info of item " + itemId);
        }

        User owner = item.getReporter();
        return new ItemOwnerInfoResult(owner.getNickname(), owner.getDepartment());
    }
}
