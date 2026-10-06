package com.example.just5minbackend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理，统一返回 {@link Result}。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        ResultCode resultCode = e.getResultCode();
        return ResponseEntity.status(httpStatus(resultCode))
                .body(Result.error(resultCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = ResultCode.PARAM_ERROR.getMessage();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ResultCode.PARAM_ERROR, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.BIZ_ERROR, "服务器开小差了，请稍后再试"));
    }

    /**
     * 业务码 → HTTP 状态映射。
     * ⚠️ 新增 ResultCode 时必须同步这里，否则会落到 default → 500（前端只对 401 特判，
     * 其余统一弹 message，会出现「提示文案对了但状态码是 500」的怪现象）。
     */
    private HttpStatus httpStatus(ResultCode resultCode) {
        return switch (resultCode) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case PARAM_ERROR, PHONE_EXISTS, LOGIN_FAILED, POINTS_NOT_ENOUGH, STOCK_NOT_ENOUGH,
                 ALREADY_CHECKED_IN, CANNOT_ADD_SELF, ALREADY_FRIENDS, FRIEND_REQUEST_HANDLED,
                 FRIEND_REQUEST_SENT, AVATAR_TOO_LARGE ->
                    HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
