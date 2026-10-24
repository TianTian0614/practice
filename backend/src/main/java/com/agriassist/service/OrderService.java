package com.agriassist.service;

import com.agriassist.entity.TradeOrder;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    TradeOrder create(Long supplyId, Integer quantity, BigDecimal amount, BigDecimal deposit, Long buyerId);
    TradeOrder payDeposit(Long id, Long buyerId);
    TradeOrder ship(Long id, String expressNo, Long sellerId);
    TradeOrder confirm(Long id, Long buyerId);
    TradeOrder cancel(Long id, Long uid);
    List<TradeOrder> myOrders(Long uid);
}
