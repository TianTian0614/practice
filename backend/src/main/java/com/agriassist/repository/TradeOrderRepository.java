package com.agriassist.repository;

import com.agriassist.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    List<TradeOrder> findByBuyerId(Long uid);
    List<TradeOrder> findBySellerId(Long uid);
}
