package com.agriassist.service;

import com.agriassist.entity.TraceRecord;

import java.util.List;
import java.util.Map;

public interface TraceService {

    /** 溯源批次列表（按批次分组摘要） */
    List<Map<String, Object>> batches();

    /** 按溯源码查询完整溯源链 */
    List<TraceRecord> query(String batchNo);

    /** 新增溯源环节 */
    TraceRecord add(TraceRecord record);
}
