package com.example.spring.bzsellerservice.repository;


import com.example.spring.bzsellerservice.entity.Congdong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CongdongRepository extends JpaRepository<Congdong, Long> {
    Optional<Congdong> findByProductId(Long productId);
}
