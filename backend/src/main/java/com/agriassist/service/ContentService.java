package com.agriassist.service;

import com.agriassist.entity.Article;
import com.agriassist.entity.CommunityPost;
import com.agriassist.entity.Comment;
import com.agriassist.entity.AuditLog;

import java.util.List;

public interface ContentService {
    Article addArticle(Article a);
    List<Article> listArticles(String category);
    CommunityPost addPost(CommunityPost p, Long userId);
    List<CommunityPost> listPosts();
    List<Comment> listComments(Long postId);
    Comment addComment(Long postId, Long userId, String content);
    void adoptComment(Long postId, Long commentId);
    AuditLog audit(String type, Long targetId, String action, Long auditorId, String note);
    List<AuditLog> listAuditLogs();
}
