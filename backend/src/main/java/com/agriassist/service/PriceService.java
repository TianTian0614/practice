package com.agriassist.service;

import com.agriassist.entity.PriceQuote;

import java.util.List;
import java.util.Map;

public interface PriceService {

    /** 全部报价（可按产品过滤） */
    List<PriceQuote> quotes(String productName);

    /** 已有报价的农产品清单 */
    List<String> products();

    /**
     * 价格趋势（用于折线图）
     * 返回：{ dates: [...], series: [ { market, data: [...] } ] }
     */
    Map<String, Object> trend(String productName);

    /**
     * 最新比价表：每个产品在每个渠道的最新价格
     * 返回：[ { product, unit, markets: [ {market, price, quoteDate, lowest} ] } ]
     */
    List<Map<String, Object>> latest();

    /** 新增报价 */
    PriceQuote add(PriceQuote quote);
}
