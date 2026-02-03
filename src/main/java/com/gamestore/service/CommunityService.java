package com.gamestore.service;

import com.gamestore.dto.request.CreateCommentRequest;
import com.gamestore.dto.request.CreatePostRequest;
import com.gamestore.entity.CommunityComment;
import com.gamestore.entity.CommunityPost;
import com.gamestore.entity.CommunitySection;
import com.gamestore.exception.CustomException;
import com.gamestore.repository.CommunityCommentRepository;
import com.gamestore.repository.CommunityPostRepository;
import com.gamestore.repository.CommunitySectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunityService {

    private final CommunitySectionRepository sectionRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;

    public CommunityService(CommunitySectionRepository sectionRepository,
                           CommunityPostRepository postRepository,
                           CommunityCommentRepository commentRepository) {
        this.sectionRepository = sectionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // ========== 板块管理 ==========

    /**
     * 获取所有启用的板块
     */
    public List<CommunitySection> getAllSections() {
        return sectionRepository.findByStatusOrderBySortOrderAsc(CommunitySection.SectionStatus.ACTIVE);
    }

    /**
     * 获取板块详情
     */
    public CommunitySection getSectionById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new CustomException("板块不存在"));
    }

    // ========== 帖子管理 ==========

    /**
     * 获取板块下的帖子列表（分页）
     */
    public Page<CommunityPost> getPostsBySection(Long sectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findBySectionIdAndStatus(sectionId, CommunityPost.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 获取精华帖子
     */
    public Page<CommunityPost> getEssencePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByIsEssenceTrueAndStatusOrderByCreatedAtDesc(
                CommunityPost.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 搜索帖子
     */
    public Page<CommunityPost> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.searchPosts(keyword, CommunityPost.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 获取帖子详情（增加浏览次数）
     */
    @Transactional
    public CommunityPost getPostById(Long id) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException("帖子不存在"));
        
        // 增加浏览次数
        postRepository.incrementViewCount(id);
        
        return post;
    }

    /**
     * 创建帖子
     */
    @Transactional
    public CommunityPost createPost(CreatePostRequest request, Long userId) {
        // 验证输入
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new CustomException("标题不能为空");
        }
        if (request.getTitle().length() > 200) {
            throw new CustomException("标题长度不能超过200个字符");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CustomException("内容不能为空");
        }
        if (request.getSectionId() == null) {
            throw new CustomException("板块ID不能为空");
        }
        
        // 验证板块是否存在
        CommunitySection section = getSectionById(request.getSectionId());
        
        CommunityPost post = new CommunityPost();
        post.setSectionId(request.getSectionId());
        post.setUserId(userId);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setImages(request.getImages());
        post.setStatus(CommunityPost.PostStatus.PUBLISHED);
        
        CommunityPost savedPost = postRepository.save(post);
        
        // 更新板块帖子数
        section.setPostCount(section.getPostCount() + 1);
        sectionRepository.save(section);
        
        return savedPost;
    }

    /**
     * 删除帖子
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = getPostById(postId);
        
        // 验证权限（只能删除自己的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new CustomException("无权删除该帖子");
        }
        
        post.setStatus(CommunityPost.PostStatus.DELETED);
        postRepository.save(post);
        
        // 更新板块帖子数
        CommunitySection section = getSectionById(post.getSectionId());
        section.setPostCount(Math.max(0, section.getPostCount() - 1));
        sectionRepository.save(section);
    }

    // ========== 评论管理 ==========

    /**
     * 获取帖子的评论列表
     */
    public Page<CommunityComment> getCommentsByPost(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByPostIdAndStatusOrderByCreatedAtAsc(
                postId, CommunityComment.CommentStatus.PUBLISHED, pageable);
    }

    /**
     * 获取评论的回复列表
     */
    public List<CommunityComment> getRepliesByComment(Long commentId) {
        return commentRepository.findByParentIdAndStatusOrderByCreatedAtAsc(
                commentId, CommunityComment.CommentStatus.PUBLISHED);
    }

    /**
     * 创建评论
     */
    @Transactional
    public CommunityComment createComment(CreateCommentRequest request, Long userId) {
        // 验证输入
        if (request.getPostId() == null) {
            throw new CustomException("帖子ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CustomException("评论内容不能为空");
        }
        
        // 验证帖子是否存在
        CommunityPost post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new CustomException("帖子不存在"));
        
        // 验证帖子是否已关闭评论
        if (post.getIsClosed()) {
            throw new CustomException("该帖子已关闭评论");
        }
        
        CommunityComment comment = new CommunityComment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent().trim());
        comment.setStatus(CommunityComment.CommentStatus.PUBLISHED);
        
        CommunityComment savedComment = commentRepository.save(comment);
        
        // 更新帖子评论数和最后评论时间
        postRepository.updateCommentCount(request.getPostId(), 1);
        
        return savedComment;
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException("评论不存在"));
        
        // 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new CustomException("无权删除该评论");
        }
        
        comment.setStatus(CommunityComment.CommentStatus.DELETED);
        commentRepository.save(comment);
        
        // 更新帖子评论数
        postRepository.updateCommentCount(comment.getPostId(), -1);
    }

    /**
     * 获取用户发布的帖子
     */
    public Page<CommunityPost> getUserPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId, CommunityPost.PostStatus.PUBLISHED, pageable);
    }

    /**
     * 获取用户发布的评论
     */
    public Page<CommunityComment> getUserComments(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId, CommunityComment.CommentStatus.PUBLISHED, pageable);
    }

    // ========== 管理员专用方法 ==========

    /**
     * 更新帖子（管理员）
     */
    @Transactional
    public CommunityPost updatePost(CommunityPost post) {
        return postRepository.save(post);
    }

    /**
     * 管理员删除评论
     */
    @Transactional
    public void adminDeleteComment(Long commentId) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException("评论不存在"));
        
        comment.setStatus(CommunityComment.CommentStatus.DELETED);
        commentRepository.save(comment);
        
        // 更新帖子评论数
        postRepository.updateCommentCount(comment.getPostId(), -1);
    }
}

