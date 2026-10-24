package com.agriassist.service.impl;

import com.agriassist.entity.TraceRecord;
import com.agriassist.repository.TraceRecordRepository;
import com.agriassist.service.TraceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TraceServiceImpl implements TraceService {

    private final TraceRecordRepository repository;

    public TraceServiceImpl(TraceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> batches() {
        List<TraceRecord> all = repository.findAllByOrderByOccurredAtDesc();
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (TraceRecord r : all) {
            Map<String, Object> b = grouped.computeIfAbsent(r.getBatchNo(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("batchNo", r.getBatchNo());
                m.put("productName", r.getProductName());
                m.put("stepCount", 0);
                m.put("firstTime", r.getOccurredAt());
                m.put("lastTime", r.getOccurredAt());
                m.put("isDemo", r.getIsDemo());
                return m;
            });
            b.put("stepCount", ((int) b.get("stepCount")) + 1);
            if (r.getOccurredAt().isBefore((java.time.LocalDateTime) b.get("firstTime"))) {
                b.put("firstTime", r.getOccurredAt());
            }
            if (r.getOccurredAt().isAfter((java.time.LocalDateTime) b.get("lastTime"))) {
                b.put("lastTime", r.getOccurredAt());
            }
        }
        return new ArrayList<>(grouped.values());
    }

    @Override
    public List<TraceRecord> query(String batchNo) {
        return repository.findByBatchNoOrderByOccurredAtAsc(batchNo);
    }

    @Override
    public TraceRecord add(TraceRecord record) {
        if (record.getOccurredAt() == null) {
            record.setOccurredAt(java.time.LocalDateTime.now());
        }
        return repository.save(record);
    }
}
