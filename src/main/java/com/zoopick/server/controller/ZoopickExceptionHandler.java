package com.zoopick.server.controller;

import com.zoopick.server.dto.CommonResponse;
import com.zoopick.server.exception.ZoopickException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 예외처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class ZoopickExceptionHandler {
    /**
     * 서버의 오류로 발생한 예외 처리
     *
     * @param exception 발생한 예외
     * @return 상태 코트 500의 {@link CommonResponse#error(String)}
     */
    @ExceptionHandler(Exception.class)
    public <T> ResponseEntity<CommonResponse<T>> handleInternalServerException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("Internal Server Error"));
    }

    // @Valid 검증 실패 예외 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error(errorMessage));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<CommonResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponse.error("잘못된 요청입니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponse.error("존재하지 않는 경로입니다."));
    }

    /**
     * @param exception 발생한 예외
     * @param request   Http 요청 정보
     * @return {@link ZoopickException#createResponseEntity()}로 만들어진 상태 코드와 응답
     * @see ZoopickException
     */
    @ExceptionHandler(ZoopickException.class)
    public <T> ResponseEntity<CommonResponse<T>> handleZoopickException(ZoopickException exception, HttpServletRequest request) {
        if (exception.getStatusCode().is5xxServerError()) {
            log.error(exception.getMessage(), exception);
        } else {
            log.info("({}) {}", request.getRemoteAddr(), exception.getClientMessage());
            log.debug("Detail: ", exception);
        }

        return exception.createResponseEntity();
    }
}
