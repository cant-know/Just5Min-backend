package com.example.just5minbackend.common;

/**
 * 当前请求用户的上下文（基于 ThreadLocal）。
 * 由 {@code AuthInterceptor} 在请求进入时写入、请求结束时清除。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    public static Long get() {
        return USER_ID.get();
    }

    /**
     * 获取当前用户 ID，未登录时抛出 401 业务异常。
     */
    public static Long require() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
