package com.agriassist.service.impl;

import com.agriassist.entity.Certification;
import com.agriassist.exception.BusinessException;
import com.agriassist.mq.NotificationEvent;
import com.agriassist.mq.NotificationEventPublisher;
import com.agriassist.repository.CertificationRepository;
import com.agriassist.service.CertificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository repository;
    private final NotificationEventPublisher notificationPublisher;

    public CertificationServiceImpl(CertificationRepository repository, NotificationEventPublisher notificationPublisher) {
        this.repository = repository;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    @Transactional
    public Certification apply(Long userId, String type, String material) {
        if (type == null || type.isBlank()) throw new BusinessException(400, "认证类型不能为空");
        // 已通过则不可重复申请
        if (repository.findByUserIdAndStatus(userId, "APPROVED").stream()
                .anyMatch(c -> c.getType().equals(type))) {
            throw new BusinessException(400, "该认证已通过，无需重复申请");
        }
        // 撤回同类型未处理的旧申请，允许重新提交
        repository.findByUserIdAndStatus(userId, "PENDING").stream()
                .filter(c -> c.getType().equals(type))
                .forEach(repository::delete);

        Certification c = new Certification();
        c.setUserId(userId);
        c.setType(type);
        c.setMaterial(material);
        c.setStatus("PENDING");
        c.setApplyTime(LocalDateTime.now());
        return repository.save(c);
    }

    @Override
    public List<Certification> listMy(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Certification> listPending() {
        return repository.findByStatus("PENDING");
    }

    @Override
    @Transactional
    public Certification review(Long certId, boolean approve, Long reviewerId, String rejectReason) {
        Certification c = repository.findById(certId)
                .orElseThrow(() -> new BusinessException(404, "认证记录不存在"));
        if (!"PENDING".equals(c.getStatus())) {
            throw new BusinessException(400, "该申请已处理");
        }
        c.setStatus(approve ? "APPROVED" : "REJECTED");
        c.setReviewTime(LocalDateTime.now());
        c.setReviewerId(reviewerId);
        if (!approve) c.setRejectReason(rejectReason);
        Certification saved = repository.save(c);

        String label = CertificationService.labelOf(c.getType());
        NotificationEvent event;
        if (approve) {
            event = new NotificationEvent(c.getUserId(), "SYSTEM",
                    "认证通过：" + label,
                    "您申请的【" + label + "】已通过审核，徽章已展示在个人主页。", "/mine");
        } else {
            event = new NotificationEvent(c.getUserId(), "SYSTEM",
                    "认证未通过：" + label,
                    "您申请的【" + label + "】未通过审核，原因：" + (rejectReason == null ? "未说明" : rejectReason),
                    "/mine");
        }
        // 异步解耦：通过 MQ（或本地 @Async 降级）投递通知，不阻塞审核主事务
        notificationPublisher.publish(event);
        return saved;
    }
}
