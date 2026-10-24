package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.service.SupplyDemandService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supply")
public class SupplyDemandController {
    private final SupplyDemandService service;
    public SupplyDemandController(SupplyDemandService service) { this.service = service; }

    @PostMapping("/publish")
    public Result<SupplyDemand> publish(@RequestBody SupplyDemand sd) {
        Long uid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.publish(sd, uid));
    }

    @GetMapping("/list")
    public Result<Page<SupplyDemand>> list(@RequestParam(required = false) String type,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String region,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.list(type, category, region, PageRequest.of(page, size)));
    }

    @GetMapping("/match/{id}")
    public Result<?> match(@PathVariable Long id) {
        return Result.success(service.match(id));
    }

    @GetMapping("/my")
    public Result<Page<SupplyDemand>> myList(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.myList(currentUid(), PageRequest.of(page, size)));
    }

    @PutMapping("/{id}")
    public Result<SupplyDemand> update(@PathVariable Long id, @RequestBody SupplyDemand patch) {
        return Result.success(service.update(id, patch, currentUid()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id, currentUid());
        return Result.success();
    }

    @PostMapping("/{id}/off")
    public Result<Void> off(@PathVariable Long id) {
        service.off(id, currentUid());
        return Result.success();
    }

    @PostMapping("/{id}/on")
    public Result<Void> on(@PathVariable Long id) {
        service.on(id, currentUid());
        return Result.success();
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
