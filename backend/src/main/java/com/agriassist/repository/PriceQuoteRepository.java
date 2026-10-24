package com.agriassist.repository;

import com.agriassist.entity.PriceQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceQuoteRepository extends JpaRepository<PriceQuote, Long> {

    /** 某农产品的全部报价（日期升序，用于画趋势线） */
    List<PriceQuote> findByProductNameOrderByQuoteDateAsc(String productName);

    /** 全部报价（日期升序，用于产品清单与比价） */
    List<PriceQuote> findAllByOrderByQuoteDateDesc();
}
