package com.zoopick.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private T data;
    private String error;

    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>(false, null, message); // 첫 번째 인자를 false로!
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null); // 성공은 true
    }
}