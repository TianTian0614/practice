package com.agriassist.service;

import com.agriassist.entity.Negotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NegotiationService {
    Negotiation init(Long supplyDemandId, String message, Long buyerId); // 买家发起协商
    Negotiation accept(Long id, Long userId);   // 卖家接受
    Negotiation reject(Long id, Long userId);   // 拒绝
    Negotiation complete(Long id, Long userId); // 完成（已接受后）
    Negotiation cancel(Long id, Long userId);   // 取消（任一方）
    Page<Negotiation> myList(Long userId, Pageable pageable);
}
