package com.agriassist.service;

import com.agriassist.repository.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private final SupplyDemandRepository supplyRepository;
    private final ArticleRepository articleRepository;
    private final PreSaleRepository preSaleRepository;
    private final CommunityPostRepository postRepository;

    public SearchService(SupplyDemandRepository supplyRepository, ArticleRepository articleRepository,
                         PreSaleRepository preSaleRepository, CommunityPostRepository postRepository) {
        this.supplyRepository = supplyRepository;
        this.articleRepository = articleRepository;
        this.preSaleRepository = preSaleRepository;
        this.postRepository = postRepository;
    }

    public Map<String, Object> search(String q) {
        String like = "%" + (q == null ? "" : q).toLowerCase() + "%";
        List<?> supplies = supplyRepository.search(like);
        List<?> articles = articleRepository.search(like);
        List<?> presales = preSaleRepository.search(like);
        List<?> posts = postRepository.search(like);
        int total = supplies.size() + articles.size() + presales.size() + posts.size();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("supplies", supplies);
        res.put("articles", articles);
        res.put("presales", presales);
        res.put("posts", posts);
        res.put("total", total);
        return res;
    }
}
