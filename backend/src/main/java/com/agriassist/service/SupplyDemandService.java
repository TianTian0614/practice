package com.agriassist.service;

import com.agriassist.entity.SupplyDemand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface SupplyDemandService {
    SupplyDemand publish(SupplyDemand sd, Long userId);
    Page<SupplyDemand> list(String type, String category, String region, Pageable pageable);
    List<SupplyDemand> match(Long supplyDemandId); // 智能匹配：同类/同地区/价格±20%

    // ===== 后台审核 =====
    List<SupplyDemand> listPending(); // 待审核(status=0)
    void approve(Long id);            // 通过 → status=1
    void reject(Long id);             // 驳回 → status=2

    // ===== 用户侧 CRUD（owner 校验）=====
    SupplyDemand update(Long id, SupplyDemand patch, Long userId); // 修改后重置为待审核
    void delete(Long id, Long userId);
    Page<SupplyDemand> myList(Long userId, Pageable pageable);
    void off(Long id, Long userId);   // 下架 → status=3
    void on(Long id, Long userId);    // 上架 → status=1
}
