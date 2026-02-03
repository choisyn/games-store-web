package com.gamestore.controller;

import com.gamestore.dto.request.CreateCommentRequest;
import com.gamestore.dto.request.CreatePostRequest;
import com.gamestore.dto.response.ApiResponse;
import com.gamestore.dto.response.CommentResponse;
import com.gamestore.dto.response.PostResponse;
import com.gamestore.entity.User;
import com.gamestore.service.AuthService;
import com.gamestore.service.PostService;
import com.gamestore.util.ResponseUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 获取帖子列表
     * @param gameId 游戏ID（可选），为空或0表示所有游戏
     * @param page 页码（从0开始）
     * @param size 每页大小
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @RequestParam(required = false) Long gameId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<PostResponse> posts = postService.getPosts(gameId, page, size);
        return ResponseUtil.success("获取成功", posts);
    }
    
    /**
     * 根据ID获取帖子详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseUtil.success("获取成功", post);
    }
    
    /**
     * 创建帖子（需要登录）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestBody CreatePostRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseUtil.error(401, "请先登录");
        }
        
        PostResponse post = postService.createPost(request, userId);
        return ResponseUtil.success("发布成功", post);
    }
    
    /**
     * 删除帖子（需要登录）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseUtil.error(401, "请先登录");
        }
        
        postService.deletePost(id, userId);
        return ResponseUtil.success("删除成功");
    }
    
    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Page<CommentResponse> comments = postService.getComments(postId, page, size);
        return ResponseUtil.success("获取成功", comments);
    }
    
    /**
     * 创建评论（需要登录）
     */
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @RequestBody CreateCommentRequest request,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseUtil.error(401, "请先登录");
        }
        
        CommentResponse comment = postService.createComment(request, userId);
        return ResponseUtil.success("评论成功", comment);
    }
    
    /**
     * 删除评论（需要登录）
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseUtil.error(401, "请先登录");
        }
        
        postService.deleteComment(id, userId);
        return ResponseUtil.success("删除成功");
    }
    
    /**
     * 搜索帖子
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseUtil.success("搜索成功", posts);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        User user = authService.getUserByToken(token);
        return user != null ? user.getId() : null;
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 优先从Header获取
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        
        // 从Cookie获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("SESSION_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}

