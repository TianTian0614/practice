package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.PreSale;
import com.agriassist.entity.Reservation;
import com.agriassist.service.PreSaleService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presales")
public class PreSaleController {
    private final PreSaleService service;
    public PreSaleController(PreSaleService service) { this.service = service; }

    @PostMapping("/publish")
    public Result<PreSale> publish(@RequestBody PreSale ps) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.publish(ps, uid));
    }

    @GetMapping
    public Result<List<PreSale>> list() {
        return Result.success(service.list());
    }

    @GetMapping("/my")
    public Result<List<PreSale>> my() {
        return Result.success(service.listMine(currentUid()));
    }

    @GetMapping("/my-reservations")
    public Result<List<Reservation>> myReservations() {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.myReservations(uid));
    }

    @GetMapping("/{id}/reservations")
    public Result<List<Reservation>> reservationsOf(@PathVariable Long id) {
        return Result.success(service.reservationsOf(id, currentUid()));
    }

    @PostMapping("/{id}/reserve")
    public Result<Reservation> reserve(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") int quantity,
                                       @RequestParam(required = false) String name,
                                       @RequestParam(required = false) String phone) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.reserve(id, uid, quantity, name, phone));
    }

    @PostMapping("/{id}/end")
    public Result<Void> end(@PathVariable Long id) {
        service.end(id, currentUid());
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id, currentUid());
        return Result.success();
    }

    @PostMapping("/reservations/{rid}/ship")
    public Result<Void> ship(@PathVariable Long rid) {
        service.ship(rid, currentUid());
        return Result.success();
    }

    @PostMapping("/reservations/{rid}/confirm")
    public Result<Void> confirm(@PathVariable Long rid) {
        service.confirm(rid, currentUid());
        return Result.success();
    }

    @PostMapping("/reservations/{rid}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long rid) {
        service.cancelReservation(rid, currentUid());
        return Result.success();
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
