package com.example.spring.bzsellerservice.repository;

import com.example.spring.bzsellerservice.entity.CongDongIng;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CongdongIngRepository extends JpaRepository<CongDongIng, Long> {
    // 특정 상품 ID에 해당하는 모든 공동구매 정보 조회
    List<CongDongIng> findAllByProductId(Long productId);
    // ✅ 상품 ID와 조건을 기준으로 공동구매 찾기
    Optional<CongDongIng> findByProductIdAndCondition(Long productId, String condition);
}