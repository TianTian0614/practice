package com.agriassist.service;

import com.agriassist.entity.PreSale;
import com.agriassist.entity.Reservation;

import java.util.List;

public interface PreSaleService {
    PreSale publish(PreSale ps, Long userId);
    List<PreSale> list();
    List<PreSale> listMine(Long userId);
    Reservation reserve(Long preSaleId, Long userId, int quantity, String name, String phone);
    List<Reservation> myReservations(Long userId);
    List<Reservation> reservationsOf(Long preSaleId, Long ownerUid);
    void ship(Long reservationId, Long ownerUid);
    void confirm(Long reservationId, Long uid);
    void end(Long id, Long userId);
    void cancel(Long id, Long userId);
    void cancelReservation(Long reservationId, Long userId);
}
