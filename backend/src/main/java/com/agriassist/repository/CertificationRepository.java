package com.agriassist.repository;

import com.agriassist.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUserId(Long userId);
    List<Certification> findByUserIdAndStatus(Long userId, String status);
    List<Certification> findByStatus(String status);
}
