package com.example.spring.bzsellerservice.repository;


import com.example.spring.bzsellerservice.entity.Congdong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CongdongRepository extends JpaRepository<Congdong, Long> {
    Optional<Congdong> findByProductId(Long productId);
    // 상품 ID에 해당하는 모든 조건을 조회할 때
    List<Congdong> findAllByProductId(Long productId);

}
