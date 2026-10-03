package com.example.just5minbackend.interceptor;

import com.example.just5minbackend.common.Result;
import com.example.just5minbackend.common.ResultCode;
import com.example.just5minbackend.common.UserContext;
import com.example.just5minbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 鉴权拦截器：解析 {@code Authorization: Bearer <token>}，把 userId 写入 {@link UserContext}。
 * <p><b>游客模式</b>：未携带（或携带失效）token 时，只读的浏览类接口仍然放行——
 * 这样未登录用户能以「游客」身份浏览题库与题目；写操作与个人数据接口照旧返回 401，
 * 由前端弹出登录引导。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    /** 游客可匿名访问的只读接口前缀（仅限 GET）。注意 startsWith 匹配，只放行具体浏览接口 */
    private static final List<String> GUEST_READABLE_PREFIXES = List.of(
            "/api/categories",
            "/api/questions",
            "/api/mall/categories",
            "/api/mall/products"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 有 token 就尝试解析；解析失败（过期/伪造）按未登录处理，不当场报错
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            try {
                UserContext.set(jwtUtil.parseUserId(token));
            } catch (Exception e) {
                log.debug("token 解析失败: {}", e.getMessage());
            }
        }

        // 2. 已登录：放行
        if (UserContext.get() != null) {
            return true;
        }

        // 3. 游客：只读浏览接口放行
        if (isGuestReadable(request)) {
            log.debug("游客访问 {} {}", request.getMethod(), request.getRequestURI());
            return true;
        }

        // 4. 其余（答题提交、错题本、统计等）必须登录
        writeUnauthorized(response);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isGuestReadable(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return GUEST_READABLE_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> body = Result.error(ResultCode.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
