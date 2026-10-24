package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.Negotiation;
import com.agriassist.service.NegotiationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/negotiation")
public class NegotiationController {

    private final NegotiationService service;

    public NegotiationController(NegotiationService service) {
        this.service = service;
    }

    @PostMapping
    public Result<Negotiation> init(@RequestBody Map<String, Object> body) {
        Long sdId = ((Number) body.get("supplyDemandId")).longValue();
        String msg = (String) body.get("message");
        return Result.success(service.init(sdId, msg, currentUid()));
    }

    @PutMapping("/{id}/accept")
    public Result<Negotiation> accept(@PathVariable Long id) {
        return Result.success(service.accept(id, currentUid()));
    }

    @PutMapping("/{id}/reject")
    public Result<Negotiation> reject(@PathVariable Long id) {
        return Result.success(service.reject(id, currentUid()));
    }

    @PutMapping("/{id}/complete")
    public Result<Negotiation> complete(@PathVariable Long id) {
        return Result.success(service.complete(id, currentUid()));
    }

    @PutMapping("/{id}/cancel")
    public Result<Negotiation> cancel(@PathVariable Long id) {
        return Result.success(service.cancel(id, currentUid()));
    }

    @GetMapping("/my")
    public Result<Page<Negotiation>> my(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.myList(currentUid(), PageRequest.of(page, size)));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
