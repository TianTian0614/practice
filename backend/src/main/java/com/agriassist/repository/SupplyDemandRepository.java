package com.agriassist.repository;

import com.agriassist.entity.SupplyDemand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplyDemandRepository extends JpaRepository<SupplyDemand, Long> {
    Page<SupplyDemand> findByTypeAndStatus(String type, Integer status, Pageable pageable);
    Page<SupplyDemand> findByStatus(Integer status, Pageable pageable);
    List<SupplyDemand> findByStatus(Integer status);
    List<SupplyDemand> findByTypeAndStatus(String type, Integer status);
    Page<SupplyDemand> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT s FROM SupplyDemand s WHERE s.status = 1 AND (LOWER(s.title) LIKE ?1 OR LOWER(s.description) LIKE ?1 OR LOWER(s.category) LIKE ?1)")
    List<SupplyDemand> search(String q);
}
