package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.repository.CongdongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CongdongService {

    private final CongdongRepository congdongRepository;

    // 상품 ID에 해당하는 모든 조건을 반환
    public List<Congdong> findConditionsByProductId(Long productId) {
        return congdongRepository.findAllByProductId(productId);
    }

}
