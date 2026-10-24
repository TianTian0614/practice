package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据可视化大屏统计接口：一次返回全部大屏所需数据
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final SupplyDemandRepository supplyRepo;
    private final PreSaleRepository preSaleRepo;
    private final UserRepository userRepo;
    private final ConsultationRepository consultRepo;
    private final CommunityPostRepository postRepo;
    private final ReservationRepository reservationRepo;

    public StatsController(SupplyDemandRepository supplyRepo, PreSaleRepository preSaleRepo,
                           UserRepository userRepo, ConsultationRepository consultRepo,
                           CommunityPostRepository postRepo, ReservationRepository reservationRepo) {
        this.supplyRepo = supplyRepo;
        this.preSaleRepo = preSaleRepo;
        this.userRepo = userRepo;
        this.consultRepo = consultRepo;
        this.postRepo = postRepo;
        this.reservationRepo = reservationRepo;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1) 顶部统计卡
        Map<String, Object> totals = new LinkedHashMap<>();
        var supplies = supplyRepo.findAll();
        totals.put("supply", supplies.stream().filter(s -> "SUPPLY".equalsIgnoreCase(s.getType())).count());
        totals.put("demand", supplies.stream().filter(s -> "DEMAND".equalsIgnoreCase(s.getType())).count());
        totals.put("presale", preSaleRepo.count());
        totals.put("user", userRepo.count());
        totals.put("post", postRepo.count());
        totals.put("consult", consultRepo.count());
        totals.put("reservation", reservationRepo.count());
        data.put("totals", totals);

        // 2) 供需按品类分布（饼图）
        Map<String, Long> byCategory = supplies.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() == null ? "未分类" : s.getCategory(), TreeMap::new, Collectors.counting()));
        data.put("supplyByCategory", toNameValue(byCategory));

        // 3) 供需按地区分布 Top8（柱图）
        Map<String, Long> byRegion = supplies.stream()
                .filter(s -> s.getRegion() != null && !s.getRegion().isBlank())
                .collect(Collectors.groupingBy(s -> s.getRegion(), Collectors.counting()));
        List<Map<String, Object>> regionList = toNameValue(byRegion);
        regionList.sort((a, b) -> Long.compare((long) b.get("value"), (long) a.get("value")));
        data.put("supplyByRegion", regionList.subList(0, Math.min(8, regionList.size())));

        // 4) 近 7 日 AI 咨询趋势（折线）
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> trendMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            trendMap.put(LocalDate.now().minusDays(i).format(fmt), 0L);
        }
        consultRepo.findAll().forEach(c -> {
            if (c.getCreateTime() != null) {
                String key = c.getCreateTime().toLocalDate().format(fmt);
                trendMap.computeIfPresent(key, (k, v) -> v + 1);
            }
        });
        data.put("consultTrend", toNameValue(trendMap));

        // 5) 最新供需列表 Top6（大屏滚动列表）
        List<Map<String, Object>> recent = supplies.stream()
                .sorted(Comparator.comparing(
                        (com.agriassist.entity.SupplyDemand s) -> s.getCreateTime(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", s.getTitle());
                    m.put("type", s.getType());
                    m.put("category", s.getCategory());
                    m.put("region", s.getRegion());
                    m.put("createTime", s.getCreateTime() == null ? "" : s.getCreateTime().toLocalDate().toString());
                    return m;
                })
                .collect(Collectors.toList());
        data.put("recentSupply", recent);

        return Result.success(data);
    }

    private List<Map<String, Object>> toNameValue(Map<String, Long> map) {
        return map.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            return m;
        }).collect(Collectors.toList());
    }
}
