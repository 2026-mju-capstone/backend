package com.zoopick.server.dto.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimilarItemResult {

    private Long itemId;
    private double score;
}