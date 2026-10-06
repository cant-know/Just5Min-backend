package com.example.just5minbackend.common;

import lombok.Getter;

/**
 * 业务状态码。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    NOT_FOUND(404, "资源不存在"),
    BIZ_ERROR(500, "业务处理失败"),
    WX_ERROR(501, "微信登录失败"),
    PHONE_EXISTS(1001, "该手机号已注册，请直接登录"),
    LOGIN_FAILED(1002, "手机号或密码错误"),
    POINTS_NOT_ENOUGH(1003, "积分不足"),
    STOCK_NOT_ENOUGH(1004, "库存不足"),
    ALREADY_CHECKED_IN(1005, "今天已经打过卡啦，明天再来"),
    CANNOT_ADD_SELF(1006, "不能添加自己为好友"),
    ALREADY_FRIENDS(1007, "你们已经是好友了"),
    FRIEND_REQUEST_HANDLED(1008, "好友请求不存在或已处理"),
    FRIEND_REQUEST_SENT(1009, "已发送过好友请求，请等待对方处理"),
    AVATAR_TOO_LARGE(1010, "头像图片过大，请重新选择");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
