package com.agriassist.repository;

import com.agriassist.entity.TraceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, Long> {

    /** 按批次号查溯源链（时间升序 = 流转顺序） */
    List<TraceRecord> findByBatchNoOrderByOccurredAtAsc(String batchNo);

    /** 所有溯源记录（用于分组出批次列表） */
    List<TraceRecord> findAllByOrderByOccurredAtDesc();
}
