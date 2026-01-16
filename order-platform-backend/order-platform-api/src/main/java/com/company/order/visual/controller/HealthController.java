package com.company.order.visual.controller;

import com.company.order.visual.common.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author Order Platform Team
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("name", "order-platform");
        data.put("time", LocalDateTime.now());
        return Result.ok(data);
    }
}
