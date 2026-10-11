package com.example.just5minbackend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    /**
     * 缺少必填查询参数（如 {@code GET /api/messages} 没带 friendId）。
     * 不拦的话会被下面的 catch-all 兜成 500，前端看到「服务器开小差」但其实只是没传参。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ResultCode.PARAM_ERROR, "缺少参数：" + e.getParameterName()));
    }

    /** 参数类型不匹配（如 friendId 传了非数字）同样属于客户端错误 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ResultCode.PARAM_ERROR, "参数格式错误：" + e.getName()));
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
            case NOT_FOUND, ARTICLE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case NOT_FRIEND -> HttpStatus.FORBIDDEN;
            case CANNOT_MESSAGE_SELF -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
