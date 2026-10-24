package com.agriassist.repository;

import com.agriassist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u ORDER BY u.points DESC")
    java.util.List<User> findTopUsers(org.springframework.data.domain.Pageable pageable);
}
