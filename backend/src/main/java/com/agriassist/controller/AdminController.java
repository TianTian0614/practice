package com.agriassist.controller;

import com.agriassist.common.ErrorCode;
import com.agriassist.common.Result;
import com.agriassist.entity.AuditLog;
import com.agriassist.entity.SupplyDemand;
import com.agriassist.exception.BusinessException;
import com.agriassist.service.ContentService;
import com.agriassist.service.SupplyDemandService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ContentService service;
    private final SupplyDemandService supplyDemandService;
    public AdminController(ContentService service, SupplyDemandService supplyDemandService) {
        this.service = service;
        this.supplyDemandService = supplyDemandService;
    }

    // 服务端管理员校验：必须由 ADMIN 角色调用，否则拒绝（不再仅靠前端拦截）
    private void requireAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) throw new BusinessException(ErrorCode.FORBIDDEN, "无权限：仅管理员可操作");
    }

    @PostMapping("/audit")
    public Result<AuditLog> audit(@RequestParam String type, @RequestParam Long targetId,
                                  @RequestParam String action, @RequestParam(required = false) String note) {
        requireAdmin();
        Long aid = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Result.success(service.audit(type, targetId, action, aid, note));
    }

    @GetMapping("/audit-logs")
    public Result<List<AuditLog>> logs() { requireAdmin(); return Result.success(service.listAuditLogs()); }

    @GetMapping("/supply-pending")
    public Result<List<SupplyDemand>> supplyPending() {
        requireAdmin();
        return Result.success(supplyDemandService.listPending());
    }
}
