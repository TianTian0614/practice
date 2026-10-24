package com.agriassist.repository;

import com.agriassist.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByCategory(String category);

    @Query("SELECT a FROM Article a WHERE LOWER(a.title) LIKE ?1 OR LOWER(a.content) LIKE ?1")
    List<Article> search(String q);
}
