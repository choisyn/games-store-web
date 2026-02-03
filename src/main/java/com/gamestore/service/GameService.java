package com.gamestore.service;

import com.gamestore.entity.Category;
import com.gamestore.entity.Game;
import com.gamestore.exception.CustomException;
import com.gamestore.repository.CategoryRepository;
import com.gamestore.repository.GameRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {
    
    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    
    public GameService(GameRepository gameRepository, CategoryRepository categoryRepository) {
        this.gameRepository = gameRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public Page<Game> getGames(Long categoryId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Game.GameStatus activeStatus = Game.GameStatus.ACTIVE;
        
        if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
            return gameRepository.searchGamesByCategory(categoryId, keyword, activeStatus, pageable);
        } else if (categoryId != null) {
            return gameRepository.findByCategoryIdAndStatus(categoryId, activeStatus, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            return gameRepository.searchGames(keyword, activeStatus, pageable);
        } else {
            return gameRepository.findByStatus(activeStatus, pageable);
        }
    }
    
    public Game getGameById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new CustomException("游戏不存在"));
    }
    
    public List<Game> getFeaturedGames(int page, int size) {
        List<Game> featuredGames = gameRepository.findByIsFeaturedTrueAndStatus(Game.GameStatus.ACTIVE);
        
        // 简单分页
        int start = page * size;
        int end = Math.min(start + size, featuredGames.size());
        
        if (start >= featuredGames.size()) {
            return List.of();
        }
        
        return featuredGames.subList(start, end);
    }
    
    public Page<Game> searchGames(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return gameRepository.searchGames(keyword, Game.GameStatus.ACTIVE, pageable);
    }
    
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
    
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }
    
    /**
     * 创建游戏(支持多分类)
     */
    @Transactional
    public Game createGame(Game game, List<Long> categoryIds) {
        // 保存游戏基本信息
        Game savedGame = gameRepository.save(game);
        
        // 如果提供了分类ID列表,设置分类关联
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new CustomException("分类不存在: " + categoryId));
                categories.add(category);
            }
            savedGame.setCategories(categories);
            savedGame = gameRepository.save(savedGame);
        }
        
        return savedGame;
    }
    
    /**
     * 更新游戏(支持多分类)
     */
    @Transactional
    public Game updateGame(Long id, Game gameDetails, List<Long> categoryIds) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new CustomException("游戏不存在"));
        
        // 更新基本信息
        game.setName(gameDetails.getName());
        game.setDescription(gameDetails.getDescription());
        game.setDeveloper(gameDetails.getDeveloper());
        game.setPublisher(gameDetails.getPublisher());
        game.setReleaseDate(gameDetails.getReleaseDate());
        game.setPrice(gameDetails.getPrice());
        game.setDiscountPrice(gameDetails.getDiscountPrice());
        game.setImageUrl(gameDetails.getImageUrl());
        game.setGallery(gameDetails.getGallery());
        game.setSystemRequirements(gameDetails.getSystemRequirements());
        game.setTags(gameDetails.getTags());
        game.setIsFeatured(gameDetails.getIsFeatured());
        game.setStatus(gameDetails.getStatus());
        
        // 更新分类关联
        if (categoryIds != null) {
            game.clearCategories();  // 清空现有分类
            if (!categoryIds.isEmpty()) {
                Set<Category> categories = new HashSet<>();
                for (Long categoryId : categoryIds) {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new CustomException("分类不存在: " + categoryId));
                    categories.add(category);
                }
                game.setCategories(categories);
            }
        }
        
        return gameRepository.save(game);
    }
    
    /**
     * 删除游戏
     */
    @Transactional
    public void deleteGame(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new CustomException("游戏不存在"));
        gameRepository.delete(game);
    }
    
    /**
     * 根据分类查询游戏(支持多分类)
     */
    public List<Game> getGamesByCategory(Long categoryId) {
        // 使用JPQL查询包含该分类的所有游戏
        return gameRepository.findGamesByCategory(categoryId);
    }
    
    /**
     * 根据游戏名称查找(用于检查重复)
     */
    public Game getGameByName(String name) {
        return gameRepository.findByName(name);
    }
    
    /**
     * 根据名称查找(排除指定ID,用于更新时检查)
     */
    public Game getGameByNameExcludingId(String name, Long id) {
        return gameRepository.findByNameAndIdNot(name, id);
    }
}