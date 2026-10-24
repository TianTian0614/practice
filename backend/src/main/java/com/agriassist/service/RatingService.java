package com.agriassist.service;

import com.agriassist.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RatingService {
    Rating create(Rating rating, Long fromUserId);
    Page<Rating> listByUser(Long toUserId, Pageable pageable);
    List<Rating> listByUserAll(Long toUserId);
    Rating update(Long id, Integer score, String comment, Long fromUserId);
    void delete(Long id, Long fromUserId);
}
