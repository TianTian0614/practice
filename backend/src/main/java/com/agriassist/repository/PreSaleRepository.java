package com.agriassist.repository;

import com.agriassist.entity.PreSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreSaleRepository extends JpaRepository<PreSale, Long> {
    List<PreSale> findByUserId(Long userId);

    List<PreSale> findByStatus(Integer status);

    @Query("SELECT p FROM PreSale p WHERE p.status = 1 AND (LOWER(p.productName) LIKE ?1 OR LOWER(p.description) LIKE ?1)")
    List<PreSale> search(String q);
}
