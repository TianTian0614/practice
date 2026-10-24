package com.agriassist.controller;

import com.agriassist.common.ErrorCode;
import com.agriassist.common.Result;
import com.agriassist.entity.Certification;
import com.agriassist.exception.BusinessException;
import com.agriassist.service.CertificationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cert")
public class CertificationController {

    private final CertificationService service;
    public CertificationController(CertificationService service) { this.service = service; }

    @PostMapping("/apply")
    public Result<Certification> apply(@RequestBody Map<String, String> body) {
        return Result.success(service.apply(currentUid(), body.get("type"), body.get("material")));
    }

    @GetMapping("/my")
    public Result<List<Certification>> my() {
        return Result.success(service.listMy(currentUid()));
    }

    @GetMapping("/pending")
    public Result<List<Certification>> pending() {
        requireAdmin();
        return Result.success(service.listPending());
    }

    @PostMapping("/review")
    public Result<Certification> review(@RequestParam Long certId,
                                        @RequestParam boolean approve,
                                        @RequestParam(required = false) String rejectReason) {
        requireAdmin();
        return Result.success(service.review(certId, approve, currentUid(), rejectReason));
    }

    private Long currentUid() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void requireAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) throw new BusinessException(ErrorCode.FORBIDDEN, "无权限：仅管理员可操作");
    }
}
