package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.SupplyDemandRepository;
import com.agriassist.service.SupplyDemandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplyDemandServiceImpl implements SupplyDemandService {

    private final SupplyDemandRepository repository;
    private final com.agriassist.service.NotificationService notificationService;
    private final com.agriassist.service.SubscriptionService subscriptionService;

    public SupplyDemandServiceImpl(SupplyDemandRepository repository, com.agriassist.service.NotificationService notificationService, com.agriassist.service.SubscriptionService subscriptionService) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    @Transactional
    public SupplyDemand publish(SupplyDemand sd, Long userId) {
        sd.setUserId(userId);
        sd.setStatus(0); // 待审核
        return repository.save(sd);
    }

    @Override
    public Page<SupplyDemand> list(String type, String category, String region, Pageable pageable) {
        if (type != null && !type.isBlank()) {
            return repository.findByTypeAndStatus(type, 1, pageable);
        }
        return repository.findByStatus(1, pageable);
    }

    @Override
    public List<SupplyDemand> match(Long supplyDemandId) {
        SupplyDemand target = repository.findById(supplyDemandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        String opposite = "SUPPLY".equals(target.getType()) ? "DEMAND" : "SUPPLY";
        List<SupplyDemand> candidates = repository.findByTypeAndStatus(opposite, 1);
        return candidates.stream()
                .filter(o -> o.getCategory() != null && o.getCategory().equals(target.getCategory()))
                .sorted((a, b) -> Integer.compare(score(target, b), score(target, a)))
                .limit(10)
                .collect(Collectors.toList());
    }

    // 匹配打分：同地区 +30，价格接近(±20%) + 价格相似度
    private int score(SupplyDemand t, SupplyDemand o) {
        int s = 0;
        if (t.getRegion() != null && t.getRegion().equals(o.getRegion())) s += 30;
        if (t.getPrice() != null && o.getPrice() != null && t.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = t.getPrice().subtract(o.getPrice()).abs();
            BigDecimal ratio = diff.divide(t.getPrice(), 4, BigDecimal.ROUND_HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.2")) <= 0) s += 20;
        }
        return s;
    }

    // ===== 后台审核：待审核列表 + 通过/驳回 =====
    @Override
    public List<SupplyDemand> listPending() {
        return repository.findByStatus(0);
    }

    @Override
    @Transactional
    public void approve(Long id) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        sd.setStatus(1); // 已通过
        repository.save(sd);
        notificationService.create(sd.getUserId(), "SYSTEM", "供需审核通过",
                "您发布的《" + (sd.getTitle() != null ? sd.getTitle() : "") + "》已通过审核，现已公开展示。", "/supply/" + sd.getId());
        subscriptionService.notifyOnNewSupply(sd);
    }

    @Override
    @Transactional
    public void reject(Long id) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        sd.setStatus(2); // 已驳回
        repository.save(sd);
    }

    @Override
    @Transactional
    public SupplyDemand update(Long id, SupplyDemand patch, Long userId) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        if (!sd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己发布的信息");
        }
        if (patch.getCategory() != null) sd.setCategory(patch.getCategory());
        if (patch.getTitle() != null) sd.setTitle(patch.getTitle());
        if (patch.getDescription() != null) sd.setDescription(patch.getDescription());
        if (patch.getRegion() != null) sd.setRegion(patch.getRegion());
        if (patch.getPrice() != null) sd.setPrice(patch.getPrice());
        if (patch.getBrandStory() != null) sd.setBrandStory(patch.getBrandStory());
        sd.setStatus(0); // 内容变更，重置为待审核
        return repository.save(sd);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        if (!sd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己发布的信息");
        }
        repository.delete(sd);
    }

    @Override
    public Page<SupplyDemand> myList(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void off(Long id, Long userId) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        if (!sd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能操作自己发布的信息");
        }
        sd.setStatus(3); // 下架
        repository.save(sd);
    }

    @Override
    @Transactional
    public void on(Long id, Long userId) {
        SupplyDemand sd = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        if (!sd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能操作自己发布的信息");
        }
        sd.setStatus(1); // 上架
        repository.save(sd);
    }
}
