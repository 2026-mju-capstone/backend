package com.zoopick.server.exception;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;

@NullMarked
public class ForbiddenException extends ZoopickException {
    public ForbiddenException(String clientMessage, String exceptionMessage) {
        super(HttpStatus.FORBIDDEN, clientMessage, exceptionMessage);
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
