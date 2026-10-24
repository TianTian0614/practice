package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.TradeOrder;
import com.agriassist.service.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping("/create")
    public Result<TradeOrder> create(@RequestBody Map<String, Object> body) {
        Long supplyId = ((Number) body.get("supplyId")).longValue();
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : 1;
        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : null;
        BigDecimal deposit = body.get("deposit") != null ? new BigDecimal(body.get("deposit").toString()) : null;
        return Result.success(service.create(supplyId, quantity, amount, deposit, currentUid()));
    }

    @PostMapping("/{id}/pay")
    public Result<TradeOrder> pay(@PathVariable Long id) {
        return Result.success(service.payDeposit(id, currentUid()));
    }

    @PostMapping("/{id}/ship")
    public Result<TradeOrder> ship(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String expressNo = body != null ? body.get("expressNo") : null;
        return Result.success(service.ship(id, expressNo, currentUid()));
    }

    @PostMapping("/{id}/confirm")
    public Result<TradeOrder> confirm(@PathVariable Long id) {
        return Result.success(service.confirm(id, currentUid()));
    }

    @PostMapping("/{id}/cancel")
    public Result<TradeOrder> cancel(@PathVariable Long id) {
        return Result.success(service.cancel(id, currentUid()));
    }

    @GetMapping("/my")
    public Result<List<TradeOrder>> my() {
        return Result.success(service.myOrders(currentUid()));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
