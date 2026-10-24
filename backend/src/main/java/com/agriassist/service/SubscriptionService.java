package com.agriassist.service;

import com.agriassist.entity.SupplyDemand;
import com.agriassist.entity.Subscription;

import java.util.List;

public interface SubscriptionService {
    Subscription subscribe(Long userId, String keyword, String region);
    void unsubscribe(Long id, Long userId);
    List<Subscription> list(Long userId);
    void notifyOnNewSupply(SupplyDemand sd);
}
