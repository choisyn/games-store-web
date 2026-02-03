package com.gamestore.controller;

import com.gamestore.dto.response.ApiResponse;
import com.gamestore.entity.CommunityComment;
import com.gamestore.entity.CommunityPost;
import com.gamestore.service.CommunityService;
import com.gamestore.util.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台社区管理API
 */
@RestController
@RequestMapping("/api/admin/community")
public class AdminCommunityController {

    private final CommunityService communityService;

    public AdminCommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    /**
     * 获取所有帖子（用于审核）
     */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<CommunityPost>>> getAllPosts(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (sectionId != null) {
            Page<CommunityPost> postsPage = communityService.getPostsBySection(sectionId, page, size);
            return ResponseUtil.success("获取成功", postsPage.getContent());
        } else {
            // 获取所有板块的帖子
            return ResponseUtil.success("获取成功", List.of());
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<CommunityPost>> getPost(@PathVariable Long id) {
        CommunityPost post = communityService.getPostById(id);
        return ResponseUtil.success("获取成功", post);
    }

    /**
     * 设置精华帖
     */
    @PutMapping("/posts/{id}/essence")
    public ResponseEntity<ApiResponse<Void>> setEssence(
            @PathVariable Long id,
            @RequestParam boolean isEssence) {
        
        CommunityPost post = communityService.getPostById(id);
        post.setIsEssence(isEssence);
        communityService.updatePost(post);
        return ResponseUtil.success(isEssence ? "设置精华成功" : "取消精华成功", null);
    }

    /**
     * 设置置顶帖
     */
    @PutMapping("/posts/{id}/pin")
    public ResponseEntity<ApiResponse<Void>> setPinned(
            @PathVariable Long id,
            @RequestParam boolean isPinned) {
        
        CommunityPost post = communityService.getPostById(id);
        post.setIsPinned(isPinned);
        communityService.updatePost(post);
        return ResponseUtil.success(isPinned ? "置顶成功" : "取消置顶成功", null);
    }

    /**
     * 关闭/开启帖子评论
     */
    @PutMapping("/posts/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closePost(
            @PathVariable Long id,
            @RequestParam boolean isClosed) {
        
        CommunityPost post = communityService.getPostById(id);
        post.setIsClosed(isClosed);
        communityService.updatePost(post);
        return ResponseUtil.success(isClosed ? "关闭评论成功" : "开启评论成功", null);
    }

    /**
     * 删除帖子（管理员权限）
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        CommunityPost post = communityService.getPostById(id);
        post.setStatus(CommunityPost.PostStatus.DELETED);
        communityService.updatePost(post);
        return ResponseUtil.success("删除成功", null);
    }

    /**
     * 隐藏帖子
     */
    @PutMapping("/posts/{id}/hide")
    public ResponseEntity<ApiResponse<Void>> hidePost(@PathVariable Long id) {
        CommunityPost post = communityService.getPostById(id);
        post.setStatus(CommunityPost.PostStatus.HIDDEN);
        communityService.updatePost(post);
        return ResponseUtil.success("隐藏成功", null);
    }

    /**
     * 获取所有评论
     */
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<CommunityComment>>> getAllComments(
            @RequestParam(required = false) Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        if (postId != null) {
            Page<CommunityComment> commentsPage = communityService.getCommentsByPost(postId, page, size);
            return ResponseUtil.success("获取成功", commentsPage.getContent());
        } else {
            return ResponseUtil.success("获取成功", List.of());
        }
    }

    /**
     * 删除评论（管理员权限）
     */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        communityService.adminDeleteComment(id);
        return ResponseUtil.success("删除成功", null);
    }
}

