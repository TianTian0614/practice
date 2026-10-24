package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.entity.Subscription;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.SubscriptionRepository;
import com.agriassist.service.NotificationService;
import com.agriassist.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository repository;
    private final NotificationService notificationService;

    public SubscriptionServiceImpl(SubscriptionRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Subscription subscribe(Long userId, String keyword, String region) {
        Subscription s = new Subscription();
        s.setUserId(userId);
        s.setKeyword(keyword);
        s.setRegion(region);
        return repository.save(s);
    }

    @Override
    @Transactional
    public void unsubscribe(Long id, Long userId) {
        Subscription s = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订阅不存在"));
        if (!s.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能取消自己的订阅");
        repository.delete(s);
    }

    @Override
    public List<Subscription> list(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void notifyOnNewSupply(SupplyDemand sd) {
        if (sd.getStatus() != 1) return;
        List<Subscription> all = repository.findAll();
        String kw = sd.getCategory() == null ? "" : sd.getCategory();
        String title = sd.getTitle() == null ? "" : sd.getTitle();
        String region = sd.getRegion() == null ? "" : sd.getRegion();
        for (Subscription sub : all) {
            boolean kwMatch = sub.getKeyword() == null || sub.getKeyword().isBlank()
                    || kw.contains(sub.getKeyword()) || title.contains(sub.getKeyword());
            boolean regionMatch = sub.getRegion() == null || sub.getRegion().isBlank() || sub.getRegion().equals(region);
            if (kwMatch && regionMatch) {
                notificationService.create(sub.getUserId(), "SYSTEM", "您订阅的供需有更新",
                        "您订阅的【" + (sub.getKeyword() == null ? "全部" : sub.getKeyword()) + "】有新的供需：《" + title + "》",
                        "/supply/" + sd.getId());
            }
        }
    }
}
