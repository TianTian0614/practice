package com.agriassist.repository;

import com.agriassist.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findByUserIdOrderByCreateTimeDesc(Long userId);
}
