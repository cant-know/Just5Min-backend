package com.example.just5minbackend.common;

import lombok.Getter;

/**
 * 业务异常，携带 {@link ResultCode} 与自定义提示。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.BIZ_ERROR;
    }
}
