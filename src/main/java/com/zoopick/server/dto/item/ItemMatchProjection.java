package com.zoopick.server.dto.item;

public interface ItemMatchProjection {
    String getMatchId();
    Long getFoundItemId();
    Long getFoundPostId();
    String getFoundPostTitle();
    String getFoundImageUrl();
    String getLocationName();
    String getFoundNickname();
    String getFoundDepartment();
    Double getScore();
    String getStatus();
}