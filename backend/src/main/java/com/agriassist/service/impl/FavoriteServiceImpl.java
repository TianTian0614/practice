package com.agriassist.service.impl;

import com.agriassist.entity.*;
import com.agriassist.repository.*;
import com.agriassist.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final SupplyDemandRepository supplyRepository;
    private final ArticleRepository articleRepository;
    private final PreSaleRepository preSaleRepository;
    private final CommunityPostRepository postRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, SupplyDemandRepository supplyRepository,
                               ArticleRepository articleRepository, PreSaleRepository preSaleRepository,
                               CommunityPostRepository postRepository) {
        this.favoriteRepository = favoriteRepository;
        this.supplyRepository = supplyRepository;
        this.articleRepository = articleRepository;
        this.preSaleRepository = preSaleRepository;
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public boolean toggle(Long userId, String targetType, Long targetId) {
        Optional<Favorite> exist = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (exist.isPresent()) {
            favoriteRepository.delete(exist.get());
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        favoriteRepository.save(f);
        return true;
    }

    @Override
    public boolean isFav(Long userId, String targetType, Long targetId) {
        return favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
    }

    @Override
    public List<Map<String, Object>> list(Long userId, String targetType) {
        List<Favorite> favs = (targetType == null || targetType.isBlank())
                ? favoriteRepository.findAll().stream().filter(f -> f.getUserId().equals(userId)).toList()
                : favoriteRepository.findByUserIdAndTargetType(userId, targetType);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : favs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("targetType", f.getTargetType());
            m.put("targetId", f.getTargetId());
            m.put("createTime", f.getCreateTime());
            enrich(m, f);
            result.add(m);
        }
        return result;
    }

    private void enrich(Map<String, Object> m, Favorite f) {
        String type = f.getTargetType();
        Long tid = f.getTargetId();
        if ("SUPPLY".equals(type)) {
            supplyRepository.findById(tid).ifPresent(s -> {
                m.put("title", s.getTitle());
                m.put("summary", s.getDescription());
                m.put("link", "/supply/" + tid);
            });
        } else if ("ARTICLE".equals(type)) {
            articleRepository.findById(tid).ifPresent(a -> {
                m.put("title", a.getTitle());
                m.put("link", "/article/" + tid);
            });
        } else if ("PRESALE".equals(type)) {
            preSaleRepository.findById(tid).ifPresent(p -> {
                m.put("title", p.getProductName());
                m.put("summary", p.getDescription());
                m.put("link", "/presale/" + tid);
            });
        } else if ("COMMUNITY".equals(type)) {
            postRepository.findById(tid).ifPresent(c -> {
                m.put("title", c.getTitle());
                m.put("summary", c.getContent());
                m.put("link", "/community/" + tid);
            });
        }
    }
}
