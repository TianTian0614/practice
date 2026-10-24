package com.agriassist.service;

import com.agriassist.entity.Certification;

import java.util.List;

public interface CertificationService {
    Certification apply(Long userId, String type, String material);
    List<Certification> listMy(Long userId);
    List<Certification> listPending();
    Certification review(Long certId, boolean approve, Long reviewerId, String rejectReason);

    static String labelOf(String type) {
        return switch (type) {
            case "REALNAME" -> "实名认证";
            case "ORIGIN"   -> "产地认证";
            case "COOP"     -> "合作社认证";
            case "EXPERT"   -> "农技专家";
            case "QUALITY"  -> "品质认证";
            default         -> type;
        };
    }
}
