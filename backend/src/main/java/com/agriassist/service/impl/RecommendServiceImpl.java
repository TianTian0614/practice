package com.agriassist.service.impl;

import com.agriassist.entity.*;
import com.agriassist.repository.*;
import com.agriassist.service.RecommendService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class RecommendServiceImpl implements RecommendService {

    private static final long DAY_MS = 86_400_000L;
    // 新鲜度窗口：14 天内的内容按发布时间远近给分（当天最高 +14，逐日衰减，14 天后为 0）
    private static final int FRESH_DAYS = 14;

    private final SupplyDemandRepository supplyRepo;
    private final ArticleRepository articleRepo;
    private final PreSaleRepository preSaleRepo;
    private final FavoriteRepository favoriteRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final UserRepository userRepo;

    public RecommendServiceImpl(SupplyDemandRepository supplyRepo, ArticleRepository articleRepo,
                                PreSaleRepository preSaleRepo, FavoriteRepository favoriteRepo,
                                SubscriptionRepository subscriptionRepo, UserRepository userRepo) {
        this.supplyRepo = supplyRepo;
        this.articleRepo = articleRepo;
        this.preSaleRepo = preSaleRepo;
        this.favoriteRepo = favoriteRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Map<String, Object> recommend(Long userId, int limit) {
        User u = userId != null ? userRepo.findById(userId).orElse(null) : null;

        Set<String> subCats = new HashSet<>();
        Set<String> subRegions = new HashSet<>();
        if (u != null) {
            subscriptionRepo.findByUserId(u.getId()).forEach(s -> {
                if (s.getKeyword() != null && !s.getKeyword().isBlank()) subCats.add(s.getKeyword());
                if (s.getRegion() != null && !s.getRegion().isBlank()) subRegions.add(s.getRegion());
            });
        }
        String userRegion = u != null ? u.getRegion() : null;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("supply", topSupply(userRegion, subCats, limit));
        result.put("articles", topArticles(subCats, Math.max(4, limit / 2)));
        result.put("presales", topPresales(userRegion, subCats, Math.max(4, limit / 2)));
        result.put("personalized", u != null);
        return result;
    }

    private List<Map<String, Object>> topSupply(String region, Set<String> cats, int limit) {
        List<SupplyDemand> all = supplyRepo.findByStatus(1);
        List<Scored> scored = new ArrayList<>();
        for (SupplyDemand s : all) {
            double sc = 0;
            if (region != null && region.equals(s.getRegion())) sc += 3;
            if (s.getCategory() != null && cats.contains(s.getCategory())) sc += 2;
            sc += favoriteRepo.countByTargetTypeAndTargetId("SUPPLY", s.getId()) * 0.5;
            sc += recency(toEpoch(s.getCreateTime()));
            scored.add(new Scored(s.getId(), sc, s));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            SupplyDemand s = (SupplyDemand) scored.get(i).item;
            out.add(Map.of(
                    "id", s.getId(), "type", s.getType(), "category", s.getCategory(),
                    "title", s.getTitle(), "region", s.getRegion(), "price", s.getPrice(),
                    "createTime", toEpoch(s.getCreateTime()),
                    "score", round(scored.get(i).score), "link", "/supply/" + s.getId()));
        }
        return out;
    }

    private List<Map<String, Object>> topArticles(Set<String> cats, int limit) {
        List<Article> all = articleRepo.findAll();
        List<Scored> scored = new ArrayList<>();
        for (Article a : all) {
            double sc = 0;
            if (a.getCategory() != null && cats.contains(a.getCategory())) sc += 3;
            sc += favoriteRepo.countByTargetTypeAndTargetId("ARTICLE", a.getId()) * 0.5;
            // 新鲜度：新发布的资讯优先展示，避免旧内容一直置顶
            sc += recency(a.getCreateTime() == null ? 0L : a.getCreateTime());
            scored.add(new Scored(a.getId(), sc, a));
        }
        scored.sort((x, y) -> Double.compare(y.score, x.score));
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            Article a = (Article) scored.get(i).item;
            out.add(Map.of(
                    "id", a.getId(), "category", a.getCategory(), "title", a.getTitle(),
                    "createTime", a.getCreateTime(),
                    "score", round(scored.get(i).score), "link", "/article/" + a.getId()));
        }
        return out;
    }

    private List<Map<String, Object>> topPresales(String region, Set<String> cats, int limit) {
        List<PreSale> all = preSaleRepo.findByStatus(1);
        List<Scored> scored = new ArrayList<>();
        for (PreSale p : all) {
            double sc = 0;
            if (region != null && region.equals(p.getRegion())) sc += 3;
            if (p.getCategory() != null && cats.contains(p.getCategory())) sc += 2;
            sc += (p.getReservedCount() == null ? 0 : p.getReservedCount()) * 0.5;
            sc += recency(toEpoch(p.getCreateTime()));
            scored.add(new Scored(p.getId(), sc, p));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            PreSale p = (PreSale) scored.get(i).item;
            out.add(Map.of(
                    "id", p.getId(), "category", p.getCategory(), "title", p.getProductName(),
                    "region", p.getRegion(), "price", p.getPrice(),
                    "createTime", toEpoch(p.getCreateTime()),
                    "score", round(scored.get(i).score), "link", "/presale/" + p.getId()));
        }
        return out;
    }

    // 新鲜度评分：发布时间距今越近分越高（14 天内有效，当天 +14，逐日 -1）
    private double recency(long epochMillis) {
        if (epochMillis <= 0) return 0;
        long days = (System.currentTimeMillis() - epochMillis) / DAY_MS;
        if (days < 0) days = 0;
        return Math.max(0, FRESH_DAYS - days);
    }

    private long toEpoch(LocalDateTime t) {
        if (t == null) return 0L;
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }

    static class Scored {
        double score;
        Object item;
        Long id;
        Scored(Long id, double score, Object item) { this.id = id; this.score = score; this.item = item; }
    }
}
