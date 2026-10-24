package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.PreSale;
import com.agriassist.entity.Reservation;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.PreSaleRepository;
import com.agriassist.repository.ReservationRepository;
import com.agriassist.service.NotificationService;
import com.agriassist.service.PreSaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreSaleServiceImpl implements PreSaleService {

    private final PreSaleRepository preSaleRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public PreSaleServiceImpl(PreSaleRepository preSaleRepository, ReservationRepository reservationRepository,
                              NotificationService notificationService) {
        this.preSaleRepository = preSaleRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public PreSale publish(PreSale ps, Long userId) {
        ps.setUserId(userId);
        ps.setReservedCount(0);
        if (ps.getStatus() == null) ps.setStatus(1);
        return preSaleRepository.save(ps);
    }

    @Override
    public List<PreSale> list() {
        return preSaleRepository.findAll();
    }

    @Override
    public List<PreSale> listMine(Long userId) {
        return preSaleRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Reservation reserve(Long preSaleId, Long userId, int quantity, String name, String phone) {
        PreSale ps = preSaleRepository.findById(preSaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (ps.getStatus() != null && ps.getStatus() != 1) throw new BusinessException(ErrorCode.BAD_REQUEST, "该预售已结束或取消");
        int left = (ps.getTotalQuota() == null ? 0 : ps.getTotalQuota()) - (ps.getReservedCount() == null ? 0 : ps.getReservedCount());
        if (quantity > left) throw new BusinessException(ErrorCode.BAD_REQUEST, "库存不足");
        ps.setReservedCount((ps.getReservedCount() == null ? 0 : ps.getReservedCount()) + quantity);
        preSaleRepository.save(ps);
        Reservation r = new Reservation();
        r.setPreSaleId(preSaleId);
        r.setUserId(userId);
        r.setQuantity(quantity);
        r.setName(name);
        r.setPhone(phone);
        r.setStatus("RESERVED");
        return reservationRepository.save(r);
    }

    @Override
    public List<Reservation> myReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    public List<Reservation> reservationsOf(Long preSaleId, Long ownerUid) {
        PreSale ps = preSaleRepository.findById(preSaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (!ps.getUserId().equals(ownerUid)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看自己发布的预售预约");
        return reservationRepository.findByPreSaleId(preSaleId);
    }

    @Override
    @Transactional
    public void ship(Long reservationId, Long ownerUid) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约记录不存在"));
        PreSale ps = preSaleRepository.findById(r.getPreSaleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (!ps.getUserId().equals(ownerUid)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能操作自己发布的预售");
        if (!"RESERVED".equals(r.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "仅已预约的订单可发货");
        r.setStatus("SHIPPED");
        reservationRepository.save(r);
        notificationService.create(r.getUserId(), "TRADE", "预售已发货",
                "您预约的《" + (ps.getProductName() != null ? ps.getProductName() : "") + "》已发货，请留意收货。", "/presale");
    }

    @Override
    @Transactional
    public void confirm(Long reservationId, Long uid) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约记录不存在"));
        PreSale ps = preSaleRepository.findById(r.getPreSaleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (!r.getUserId().equals(uid)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能确认自己的预约");
        if (!"SHIPPED".equals(r.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "请等待卖家发货后再确认收货");
        r.setStatus("CONFIRMED");
        reservationRepository.save(r);
        notificationService.create(ps.getUserId(), "TRADE", "买家已确认收货",
                "《" + (ps.getProductName() != null ? ps.getProductName() : "") + "》的买家已确认收货。", "/presale");
    }

    @Override
    @Transactional
    public void end(Long id, Long userId) {
        PreSale ps = preSaleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (!ps.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能结束自己发布的预售");
        ps.setStatus(2);
        preSaleRepository.save(ps);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        PreSale ps = preSaleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        if (!ps.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能取消自己发布的预售");
        ps.setStatus(3);
        preSaleRepository.save(ps);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预约记录不存在"));
        if (!r.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN, "只能取消自己的预约");
        if (!"RESERVED".equals(r.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST, "该预约不可取消");
        r.setStatus("CANCELLED");
        reservationRepository.save(r);
        PreSale ps = preSaleRepository.findById(r.getPreSaleId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "预售不存在"));
        int left = (ps.getReservedCount() == null ? 0 : ps.getReservedCount()) - (r.getQuantity() == null ? 0 : r.getQuantity());
        ps.setReservedCount(Math.max(0, left));
        preSaleRepository.save(ps);
    }
}
