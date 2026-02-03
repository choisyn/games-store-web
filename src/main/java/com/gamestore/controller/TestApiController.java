package com.gamestore.controller;

import com.gamestore.dto.response.ApiResponse;
import com.gamestore.entity.Game;
import com.gamestore.repository.GameRepository;
import com.gamestore.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestApiController {

    private final GameRepository gameRepository;

    public TestApiController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getGameCount() {
        try {
            long count = gameRepository.count();
            return ResponseUtil.success("游戏数量：" + count, count);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "错误：" + e.getMessage());
        }
    }

    @GetMapping("/games")
    public ResponseEntity<ApiResponse<List<Game>>> getAllGames() {
        try {
            List<Game> games = gameRepository.findAll();
            return ResponseUtil.success("获取成功", games);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "错误：" + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getStatus() {
        try {
            long count = gameRepository.count();
            List<Game> activeGames = gameRepository.findByStatus(Game.GameStatus.ACTIVE, 
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
            
            String message = String.format("数据库连接正常。游戏总数：%d，活跃游戏：%d", 
                count, activeGames.size());
            return ResponseUtil.success(message, message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.error(500, "错误：" + e.getMessage());
        }
    }
}

