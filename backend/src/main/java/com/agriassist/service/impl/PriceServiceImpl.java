package com.agriassist.service.impl;

import com.agriassist.entity.PriceQuote;
import com.agriassist.repository.PriceQuoteRepository;
import com.agriassist.service.PriceService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {

    private final PriceQuoteRepository repository;

    public PriceServiceImpl(PriceQuoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PriceQuote> quotes(String productName) {
        if (productName == null || productName.isBlank()) {
            return repository.findAllByOrderByQuoteDateDesc();
        }
        return repository.findByProductNameOrderByQuoteDateAsc(productName);
    }

    @Override
    public List<String> products() {
        return repository.findAll().stream()
                .map(PriceQuote::getProductName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> trend(String productName) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<PriceQuote> list = (productName == null || productName.isBlank())
                ? repository.findAllByOrderByQuoteDateDesc()
                : repository.findByProductNameOrderByQuoteDateAsc(productName);
        if (list.isEmpty()) {
            result.put("dates", new ArrayList<>());
            result.put("series", new ArrayList<>());
            return result;
        }
        // 日期轴（升序去重）
        List<String> dates = list.stream()
                .map(q -> q.getQuoteDate().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        // 按市场分组
        Map<String, Map<String, PriceQuote>> byMarket = new LinkedHashMap<>();
        for (PriceQuote q : list) {
            byMarket.computeIfAbsent(q.getMarket(), k -> new LinkedHashMap<>())
                    .put(q.getQuoteDate().toString(), q);
        }
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, PriceQuote>> e : byMarket.entrySet()) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("market", e.getKey());
            s.put("data", dates.stream()
                    .map(d -> {
                        PriceQuote q = e.getValue().get(d);
                        return q == null ? null : q.getPrice();
                    })
                    .collect(Collectors.toList()));
            series.add(s);
        }
        result.put("dates", dates);
        result.put("series", series);
        return result;
    }

    @Override
    public List<Map<String, Object>> latest() {
        // 全部报价按日期升序，逐条覆盖 -> 每个产品每个市场只留最新一条
        List<PriceQuote> all = repository.findAllByOrderByQuoteDateDesc();
        Collections.reverse(all); // 升序
        Map<String, Map<String, PriceQuote>> byProduct = new LinkedHashMap<>();
        for (PriceQuote q : all) {
            byProduct.computeIfAbsent(q.getProductName(), k -> new LinkedHashMap<>())
                    .put(q.getMarket(), q);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, PriceQuote>> e : byProduct.entrySet()) {
            List<PriceQuote> latestList = new ArrayList<>(e.getValue().values());
            latestList.sort(Comparator.comparing(PriceQuote::getPrice));
            List<Map<String, Object>> markets = new ArrayList<>();
            for (int i = 0; i < latestList.size(); i++) {
                PriceQuote q = latestList.get(i);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("market", q.getMarket());
                m.put("price", q.getPrice());
                m.put("unit", q.getUnit());
                m.put("quoteDate", q.getQuoteDate().toString());
                m.put("lowest", i == 0); // 排序后第一个即最低价
                markets.add(m);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", e.getKey());
            row.put("unit", latestList.isEmpty() ? "" : latestList.get(0).getUnit());
            row.put("markets", markets);
            result.add(row);
        }
        return result;
    }

    @Override
    public PriceQuote add(PriceQuote quote) {
        if (quote.getQuoteDate() == null) {
            quote.setQuoteDate(java.time.LocalDate.now());
        }
        return repository.save(quote);
    }
}
