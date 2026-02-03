package com.gamestore.controller;

import com.gamestore.dto.request.CreateCommentRequest;
import com.gamestore.dto.request.CreatePostRequest;
import com.gamestore.dto.response.ApiResponse;
import com.gamestore.entity.CommunityComment;
import com.gamestore.entity.CommunityPost;
import com.gamestore.entity.CommunitySection;
import com.gamestore.service.CommunityService;
import com.gamestore.util.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区API控制器
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    // ========== 板块相关 ==========

    /**
     * 获取所有板块
     */
    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<CommunitySection>>> getAllSections() {
        List<CommunitySection> sections = communityService.getAllSections();
        return ResponseUtil.success("获取板块列表成功", sections);
    }

    /**
     * 获取板块详情
     */
    @GetMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<CommunitySection>> getSectionById(@PathVariable Long id) {
        CommunitySection section = communityService.getSectionById(id);
        return ResponseUtil.success("获取板块详情成功", section);
    }

    // ========== 帖子相关 ==========

    /**
     * 获取板块下的帖子列表
     */
    @GetMapping("/sections/{sectionId}/posts")
    public ResponseEntity<ApiResponse<List<CommunityPost>>> getPostsBySection(
            @PathVariable Long sectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommunityPost> postsPage = communityService.getPostsBySection(sectionId, page, size);
        return ResponseUtil.success("获取帖子列表成功", postsPage.getContent());
    }

    /**
     * 获取精华帖子
     */
    @GetMapping("/posts/essence")
    public ResponseEntity<ApiResponse<List<CommunityPost>>> getEssencePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommunityPost> postsPage = communityService.getEssencePosts(page, size);
        return ResponseUtil.success("获取精华帖子成功", postsPage.getContent());
    }

    /**
     * 搜索帖子
     */
    @GetMapping("/posts/search")
    public ResponseEntity<ApiResponse<List<CommunityPost>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommunityPost> postsPage = communityService.searchPosts(keyword, page, size);
        return ResponseUtil.success("搜索成功", postsPage.getContent());
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<CommunityPost>> getPostById(@PathVariable Long id) {
        CommunityPost post = communityService.getPostById(id);
        return ResponseUtil.success("获取帖子详情成功", post);
    }

    /**
     * 创建帖子（需要登录）
     */
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<CommunityPost>> createPost(
            @RequestBody CreatePostRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) { // 临时方案，实际应从JWT获取
        
        CommunityPost post = communityService.createPost(request, userId);
        return ResponseUtil.success("发帖成功", post);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        
        communityService.deletePost(id, userId);
        return ResponseUtil.success("删除成功", null);
    }

    // ========== 评论相关 ==========

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommunityComment>>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Page<CommunityComment> commentsPage = communityService.getCommentsByPost(postId, page, size);
        return ResponseUtil.success("获取评论列表成功", commentsPage.getContent());
    }

    /**
     * 获取评论的回复
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommunityComment>>> getRepliesByComment(@PathVariable Long commentId) {
        List<CommunityComment> replies = communityService.getRepliesByComment(commentId);
        return ResponseUtil.success("获取回复列表成功", replies);
    }

    /**
     * 创建评论（需要登录）
     */
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommunityComment>> createComment(
            @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        
        CommunityComment comment = communityService.createComment(request, userId);
        return ResponseUtil.success("评论成功", comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        
        communityService.deleteComment(id, userId);
        return ResponseUtil.success("删除成功", null);
    }

    // ========== 用户相关 ==========

    /**
     * 获取用户发布的帖子
     */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<ApiResponse<List<CommunityPost>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommunityPost> postsPage = communityService.getUserPosts(userId, page, size);
        return ResponseUtil.success("获取用户帖子成功", postsPage.getContent());
    }

    /**
     * 获取用户发布的评论
     */
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<ApiResponse<List<CommunityComment>>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<CommunityComment> commentsPage = communityService.getUserComments(userId, page, size);
        return ResponseUtil.success("获取用户评论成功", commentsPage.getContent());
    }
}

