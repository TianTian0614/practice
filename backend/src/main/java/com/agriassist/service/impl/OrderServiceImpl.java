package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.entity.TradeOrder;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.SupplyDemandRepository;
import com.agriassist.repository.TradeOrderRepository;
import com.agriassist.service.NotificationService;
import com.agriassist.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final TradeOrderRepository repository;
    private final SupplyDemandRepository supplyRepository;
    private final NotificationService notificationService;

    public OrderServiceImpl(TradeOrderRepository repository, SupplyDemandRepository supplyRepository, NotificationService notificationService) {
        this.repository = repository;
        this.supplyRepository = supplyRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TradeOrder create(Long supplyId, Integer quantity, BigDecimal amount, BigDecimal deposit, Long buyerId) {
        SupplyDemand sd = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "供需信息不存在"));
        if (sd.getStatus() != 1) throw new BusinessException(ErrorCode.BAD_REQUEST, "该供需未通过审核，暂不可下单");
        if (sd.getUserId().equals(buyerId)) throw new BusinessException(ErrorCode.BAD_REQUEST, "不能购买自己发布的信息");
        TradeOrder o = new TradeOrder();
        o.setSupplyId(supplyId);
        o.setBuyerId(buyerId);
        o.setSellerId(sd.getUserId());
        o.setTitle(sd.getTitle());
        o.setAmount(amount == null ? BigDecimal.ZERO : amount);
        o.setDeposit(deposit == null ? BigDecimal.ZERO : deposit);
        o.setQuantity(quantity == null || quantity < 1 ? 1 : quantity);
        o.setStatus(0);
        return repository.save(o);
    }

    @Override
    @Transactional
    public TradeOrder payDeposit(Long id, Long buyerId) {
        TradeOrder o = get(id, buyerId, true);
        if (o.getStatus() != 0) throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不可支付定金");
        o.setStatus(1);
        o.setPayTime(LocalDateTime.now());
        TradeOrder saved = repository.save(o);
        notificationService.create(o.getSellerId(), "TRADE", "订单定金已支付",
                "买家已为《" + o.getTitle() + "》支付定金，请尽快备货发货", "/order/" + o.getId());
        return saved;
    }

    @Override
    @Transactional
    public TradeOrder ship(Long id, String expressNo, Long sellerId) {
        TradeOrder o = get(id, sellerId, false);
        if (o.getStatus() != 1) throw new BusinessException(ErrorCode.BAD_REQUEST, "请先确认买家已付定金");
        o.setStatus(2);
        o.setExpressNo(expressNo);
        o.setShipTime(LocalDateTime.now());
        TradeOrder saved = repository.save(o);
        notificationService.create(o.getBuyerId(), "TRADE", "卖家已发货",
                "您购买的《" + o.getTitle() + "》已发货，运单号：" + (expressNo == null ? "无" : expressNo), "/order/" + o.getId());
        return saved;
    }

    @Override
    @Transactional
    public TradeOrder confirm(Long id, Long buyerId) {
        TradeOrder o = get(id, buyerId, true);
        if (o.getStatus() != 2) throw new BusinessException(ErrorCode.BAD_REQUEST, "卖家尚未发货，无法确认收货");
        o.setStatus(3);
        o.setFinishTime(LocalDateTime.now());
        TradeOrder saved = repository.save(o);
        notificationService.create(o.getSellerId(), "TRADE", "订单已完成",
                "买家已确认收货，订单《" + o.getTitle() + "》完成", "/order/" + o.getId());
        return saved;
    }

    @Override
    @Transactional
    public TradeOrder cancel(Long id, Long uid) {
        TradeOrder o = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        boolean isBuyer = o.getBuyerId().equals(uid);
        boolean isSeller = o.getSellerId().equals(uid);
        if (!isBuyer && !isSeller) throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        if (o.getStatus() != 0 && o.getStatus() != 1)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不可取消");
        o.setStatus(o.getStatus() == 1 ? 5 : 4); // 已付定金取消=退款，未付=取消
        TradeOrder saved = repository.save(o);
        Long other = isBuyer ? o.getSellerId() : o.getBuyerId();
        notificationService.create(other, "TRADE", "订单已取消",
                "订单《" + o.getTitle() + "》已被" + (isBuyer ? "买家" : "卖家") + "取消", "/order/" + o.getId());
        return saved;
    }

    @Override
    public List<TradeOrder> myOrders(Long uid) {
        List<TradeOrder> list = repository.findByBuyerId(uid);
        list.addAll(repository.findBySellerId(uid));
        return list;
    }

    private TradeOrder get(Long id, Long uid, boolean buyerSide) {
        TradeOrder o = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        boolean ok = buyerSide ? o.getBuyerId().equals(uid) : o.getSellerId().equals(uid);
        if (!ok) throw new BusinessException(ErrorCode.FORBIDDEN, buyerSide ? "只能操作自己的订单（买家）" : "只能操作自己的订单（卖家）");
        return o;
    }
}
