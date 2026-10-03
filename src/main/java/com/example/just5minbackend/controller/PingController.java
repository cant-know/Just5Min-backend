package com.example.just5minbackend.controller;

import com.example.just5minbackend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查（免鉴权），用于验证服务启动与连通性。
 */
@RestController
@RequestMapping("/api/ping")
public class PingController {

    @GetMapping
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of(
                "service", "Just5Min-backend",
                "time", LocalDateTime.now().toString()
        ));
    }
}
