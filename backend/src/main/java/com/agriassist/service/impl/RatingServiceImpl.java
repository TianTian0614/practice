package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.Rating;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.RatingRepository;
import com.agriassist.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repository;
    private final com.agriassist.service.NotificationService notificationService;

    public RatingServiceImpl(RatingRepository repository, com.agriassist.service.NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Rating create(Rating rating, Long fromUserId) {
        rating.setFromUserId(fromUserId);
        rating.setStatus(1);
        Rating saved = repository.save(rating);
        notificationService.create(rating.getToUserId(), "INTERACTION", "收到新评价",
                "您收到一条" + (rating.getScore() != null ? rating.getScore() + "星" : "") + "评价", "/user/" + rating.getFromUserId());
        return saved;
    }

    @Override
    public Page<Rating> listByUser(Long toUserId, Pageable pageable) {
        return repository.findByToUserIdAndStatus(toUserId, 1, pageable);
    }

    @Override
    public List<Rating> listByUserAll(Long toUserId) {
        return repository.findByToUserIdAndStatus(toUserId, 1);
    }

    @Override
    @Transactional
    public Rating update(Long id, Integer score, String comment, Long fromUserId) {
        Rating r = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));
        if (!r.getFromUserId().equals(fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己的评价");
        }
        if (score != null) r.setScore(score);
        if (comment != null) r.setComment(comment);
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long fromUserId) {
        Rating r = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));
        if (!r.getFromUserId().equals(fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己的评价");
        }
        r.setStatus(0);
        repository.save(r);
    }
}
