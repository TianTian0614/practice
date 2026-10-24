package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.PriceQuote;
import com.agriassist.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 农产品价格行情 / 比价
 */
@RestController
@RequestMapping("/api/price")
public class PriceController {

    private final PriceService service;

    public PriceController(PriceService service) {
        this.service = service;
    }

    /** 报价列表（?product= 可选过滤） */
    @GetMapping("/quotes")
    public Result<List<PriceQuote>> quotes(@RequestParam(required = false) String product) {
        return Result.success(service.quotes(product));
    }

    /** 已有行情的农产品清单 */
    @GetMapping("/products")
    public Result<List<String>> products() {
        return Result.success(service.products());
    }

    /** 价格趋势（?product= 可选，默认第一个产品） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(@RequestParam(required = false) String product) {
        if (product == null || product.isBlank()) {
            List<String> ps = service.products();
            if (!ps.isEmpty()) product = ps.get(0);
        }
        return Result.success(service.trend(product));
    }

    /** 最新比价表（每个产品各渠道最新价，标注最低价） */
    @GetMapping("/latest")
    public Result<List<Map<String, Object>>> latest() {
        return Result.success(service.latest());
    }

    /** 新增报价（需登录） */
    @PostMapping
    public Result<PriceQuote> add(@RequestBody PriceQuote quote) {
        return Result.success(service.add(quote));
    }
}
