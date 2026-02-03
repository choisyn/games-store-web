package com.gamestore.controller;

import com.gamestore.dto.response.ApiResponse;
import com.gamestore.entity.User;
import com.gamestore.service.UserService;
import com.gamestore.util.ResponseUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final UserService userService;
    
    public TestController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> hello() {
        return ResponseUtil.success("Hello from Game Store API!", "API is working!");
    }
    
    @GetMapping("/memory")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testMemoryData() {
        try {
            // 测试内存数据存储
            User user = userService.findByUsername("admin");
            
            Map<String, Object> result = new HashMap<>();
            result.put("memory_storage", true);
            result.put("user_found", user != null);
            result.put("user_info", user != null ? 
                Map.of("id", user.getId(), "username", user.getUsername(), "email", user.getEmail()) : 
                "No user found");
            result.put("total_users", userService.getAllUsers().size());
            
            return ResponseUtil.success("内存数据测试成功", result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("memory_storage", false);
            result.put("error", e.getMessage());
            
            return ResponseUtil.error("内存数据测试失败: " + e.getMessage());
        }
    }
}
