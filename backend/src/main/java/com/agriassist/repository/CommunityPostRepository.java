package com.agriassist.repository;

import com.agriassist.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("SELECT c FROM CommunityPost c WHERE c.status = 'OPEN' AND (LOWER(c.title) LIKE ?1 OR LOWER(c.content) LIKE ?1)")
    List<CommunityPost> search(String q);
}
