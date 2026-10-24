package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.Negotiation;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.NegotiationRepository;
import com.agriassist.repository.SupplyDemandRepository;
import com.agriassist.service.NotificationService;
import com.agriassist.service.NegotiationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegotiationServiceImpl implements NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final SupplyDemandRepository supplyDemandRepository;
    private final NotificationService notificationService;

    public NegotiationServiceImpl(NegotiationRepository negotiationRepository,
                                  SupplyDemandRepository supplyDemandRepository,
                                  NotificationService notificationService) {
        this.negotiationRepository = negotiationRepository;
        this.supplyDemandRepository = supplyDemandRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Negotiation init(Long supplyDemandId, String message, Long buyerId) {
        SupplyDemand sd = supplyDemandRepository.findById(supplyDemandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        Negotiation n = new Negotiation();
        n.setSupplyDemandId(supplyDemandId);
        n.setBuyerId(buyerId);
        n.setSellerId(sd.getUserId());
        n.setMessage(message);
        n.setStatus("PENDING");
        Negotiation saved = negotiationRepository.save(n);
        notificationService.create(sd.getUserId(), "TRADE", "新的协商请求",
                "有买家就您的供需《" + (sd.getTitle() != null ? sd.getTitle() : "") + "》发起了协商", "/supply/" + supplyDemandId);
        return saved;
    }

    @Override
    @Transactional
    public Negotiation accept(Long id, Long userId) {
        Negotiation n = getAndVerify(id, userId);
        if (!"PENDING".equals(n.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不可接受");
        n.setStatus("ACCEPTED");
        Negotiation saved = negotiationRepository.save(n);
        notifyOther(n, userId, "协商已被接受", "您发起的协商已被对方接受");
        return saved;
    }

    @Override
    @Transactional
    public Negotiation reject(Long id, Long userId) {
        Negotiation n = getAndVerify(id, userId);
        n.setStatus("REJECTED");
        Negotiation saved = negotiationRepository.save(n);
        notifyOther(n, userId, "协商被拒绝", "您发起的协商被对方拒绝");
        return saved;
    }

    @Override
    @Transactional
    public Negotiation complete(Long id, Long userId) {
        Negotiation n = getAndVerify(id, userId);
        if (!"ACCEPTED".equals(n.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "仅已接受的协商可标记完成");
        n.setStatus("DONE");
        Negotiation saved = negotiationRepository.save(n);
        notifyOther(n, userId, "协商已完成", "您参与的协商已标记为完成");
        return saved;
    }

    @Override
    @Transactional
    public Negotiation cancel(Long id, Long userId) {
        Negotiation n = getAndVerify(id, userId);
        n.setStatus("CANCELLED");
        Negotiation saved = negotiationRepository.save(n);
        notifyOther(n, userId, "协商已取消", "您参与的协商已被取消");
        return saved;
    }

    @Override
    public Page<Negotiation> myList(Long userId, Pageable pageable) {
        List<Negotiation> mine = negotiationRepository.findAll().stream()
                .filter(n -> n.getBuyerId().equals(userId) || n.getSellerId().equals(userId))
                .sorted(Comparator.comparing(Negotiation::getCreateTime).reversed())
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), mine.size());
        List<Negotiation> content = start >= mine.size() ? List.of() : mine.subList(start, end);
        return new PageImpl<>(content, pageable, mine.size());
    }

    private Negotiation getAndVerify(Long id, Long userId) {
        Negotiation n = negotiationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "协商不存在"));
        if (!n.getBuyerId().equals(userId) && !n.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该协商");
        }
        return n;
    }

    private void notifyOther(Negotiation n, Long actorId, String title, String content) {
        Long other = n.getBuyerId().equals(actorId) ? n.getSellerId() : n.getBuyerId();
        notificationService.create(other, "TRADE", title, content, "/negotiation/" + n.getId());
    }
}
