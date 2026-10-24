package com.agriassist.repository;

import com.agriassist.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByToUserIdAndStatus(Long toUserId, Integer status, Pageable pageable);
    List<Rating> findByToUserIdAndStatus(Long toUserId, Integer status);
    List<Rating> findByFromUserId(Long fromUserId);
}
