package com.zoopick.server.entity;

import lombok.Getter;

@Getter
public enum ItemCategory {
    SMARTPHONE("스마트폰"),
    EARPHONES("이어폰"),
    BAG("가방"),
    WALLET("지갑"),
    CREDIT_CARD("신용카드"),
    STUDENT_ID_CARD("학생증"),
    TEXTBOOK("책"),
    NOTEBOOK("노트"),
    UMBRELLA("우산"),
    WATER_BOTTLE("물병"),
    PENCIL_CASE("필통"),
    PLUSH_TOY("봉제인형");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }
}
