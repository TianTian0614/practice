package com.agriassist.service.impl;

import com.agriassist.entity.Article;
import com.agriassist.entity.AuditLog;
import com.agriassist.entity.Comment;
import com.agriassist.entity.CommunityPost;
import com.agriassist.exception.BusinessException;
import com.agriassist.mq.NotificationEvent;
import com.agriassist.mq.NotificationEventPublisher;
import com.agriassist.repository.*;
import com.agriassist.service.ContentService;
import com.agriassist.service.PointService;
import com.agriassist.service.SupplyDemandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    private final ArticleRepository articleRepository;
    private final CommunityPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AuditLogRepository auditLogRepository;
    private final SupplyDemandService supplyDemandService;
    private final PointService pointService;
    private final NotificationEventPublisher notificationPublisher;

    public ContentServiceImpl(ArticleRepository articleRepository, CommunityPostRepository postRepository,
                              CommentRepository commentRepository, AuditLogRepository auditLogRepository,
                              SupplyDemandService supplyDemandService, PointService pointService,
                              NotificationEventPublisher notificationPublisher) {
        this.articleRepository = articleRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.auditLogRepository = auditLogRepository;
        this.supplyDemandService = supplyDemandService;
        this.pointService = pointService;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public Article addArticle(Article a) {
        return articleRepository.save(a);
    }

    @Override
    public List<Article> listArticles(String category) {
        return category == null || category.isBlank()
                ? articleRepository.findAll()
                : articleRepository.findByCategory(category);
    }

    @Override
    public CommunityPost addPost(CommunityPost p, Long userId) {
        p.setUserId(userId);
        p.setStatus("OPEN");
        CommunityPost saved = postRepository.save(p);
        pointService.award(userId, 5, "发布互助帖", "POST", saved.getId());
        return saved;
    }

    @Override
    public List<CommunityPost> listPosts() {
        return postRepository.findAll();
    }

    @Override
    public List<Comment> listComments(Long postId) {
        return commentRepository.findAll().stream()
                .filter(c -> postId.equals(c.getPostId()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Comment addComment(Long postId, Long userId, String content) {
        if (postRepository.findById(postId).isEmpty())
            throw new BusinessException(400, "帖子不存在");
        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setContent(content);
        Comment saved = commentRepository.save(c);
        pointService.award(userId, 2, "参与评论", "COMMENT", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void adoptComment(Long postId, Long commentId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "帖子不存在"));
        Comment best = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(404, "评论不存在"));
        post.setBestCommentId(commentId);
        post.setStatus("RESOLVED");
        postRepository.save(post);
        pointService.award(best.getUserId(), 10, "最佳答案", "COMMENT", commentId);
        notificationPublisher.publish(new NotificationEvent(best.getUserId(), "INTERACTION",
                "你的回答被选为最佳",
                "你在帖子《" + post.getTitle() + "》中的回答被选为最佳答案，+10 积分", "/community/" + postId));
    }

    @Override
    public AuditLog audit(String type, Long targetId, String action, Long auditorId, String note) {
        AuditLog log = new AuditLog();
        log.setTargetType(type);
        log.setTargetId(targetId);
        log.setAction(action);
        log.setAuditorId(auditorId);
        log.setNote(note);
        AuditLog saved = auditLogRepository.save(log);
        // 供需信息审核：通过/驳回直接改写 supply_demand.status，使其真正上/下架
        if ("SUPPLY".equals(type)) {
            if ("APPROVE".equals(action)) supplyDemandService.approve(targetId);
            else if ("REJECT".equals(action)) supplyDemandService.reject(targetId);
        }
        return saved;
    }

    @Override
    public List<AuditLog> listAuditLogs() {
        return auditLogRepository.findAll();
    }
}
