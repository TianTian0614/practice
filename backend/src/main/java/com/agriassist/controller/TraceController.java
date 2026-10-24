package com.agriassist.controller;

import com.agriassist.common.Result;
import com.agriassist.entity.TraceRecord;
import com.agriassist.service.TraceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 可信溯源：批次列表 / 溯源码查询 / 录入新环节
 */
@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceService service;

    public TraceController(TraceService service) {
        this.service = service;
    }

    /** 溯源批次列表（摘要） */
    @GetMapping("/batches")
    public Result<List<Map<String, Object>>> batches() {
        return Result.success(service.batches());
    }

    /** 按溯源码查询完整溯源链 */
    @GetMapping("/{batchNo}")
    public Result<List<TraceRecord>> query(@PathVariable String batchNo) {
        return Result.success(service.query(batchNo));
    }

    /** 录入溯源环节（需登录） */
    @PostMapping
    public Result<TraceRecord> add(@RequestBody TraceRecord record) {
        return Result.success(service.add(record));
    }
}
