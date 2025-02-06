package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.congdong.CongDongIngDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.CongDongIng;
import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.entity.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.spring.bzsellerservice.repository.CongdongIngRepository;
import com.example.spring.bzsellerservice.repository.CongdongRepository;
import com.example.spring.bzsellerservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongdongService {

    private final CongdongRepository congdongRepository;
    private final CongdongIngRepository congdongIngRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CongDongIngDTO startCongdong(Long productId, String condition, List<Long> congs) {
        log.info("Starting new CongDong for product ID: {} with condition: {}, congs={}", productId, condition, congs);

        // 새로운 공동구매 엔티티 생성 및 저장
        CongDongIng newCongdongIng = CongDongIng.builder()
                .productId(productId)
                .condition(condition)
                .congs(CongDongIng.toJson(congs))
                .build();

        CongDongIng savedCongdongIng = congdongIngRepository.save(newCongdongIng);
        log.info("New CongDongIng saved: {}", savedCongdongIng);

        // 엔티티를 DTO로 변환하여 반환
        return CongDongIngDTO.builder()
                .id(savedCongdongIng.getId())
                .productId(savedCongdongIng.getProductId())
                .condition(savedCongdongIng.getCondition())
                .congs(savedCongdongIng.getCongs())
                .startAt(savedCongdongIng.getStartAt())
                .build();
    }

    @Transactional
    public ResponseEntity<CongDongIngDTO> joinCongdong(String token, Long productId, String condition, String congsJson) {
        log.info("🔎 공동구매 참여 요청 (Seller Service) → productId={}, condition={}, congs={}", productId, condition, congsJson);

        // 1️⃣ 공동구매 찾기 (없으면 예외 발생)
        CongDongIng congdong = congdongIngRepository.findByProductIdAndCondition(productId, condition)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 조건의 공동구매가 존재하지 않습니다. productId=" + productId + ", condition=" + condition));
        log.info("✅ 공동구매 정보 찾음: {}", congdong);

        // 2️⃣ Front에서 받은 `congsJson`을 그대로 저장
        // 2️⃣ 따옴표(") 제거 후 저장 (JSON 형태 유지)
        String cleanedCongsJson = congsJson.replaceAll("\"", "");  // 🔥 따옴표 제거
        congdong.setCongs(cleanedCongsJson);
        congdongIngRepository.save(congdong);
        log.info("✅ 공동구매 참여자 목록 업데이트 완료: {}", cleanedCongsJson);

        // 3️⃣ DTO 변환 후 반환
        CongDongIngDTO responseDTO = CongDongIngDTO.builder()
                .id(congdong.getId())
                .productId(congdong.getProductId())
                .condition(congdong.getCondition())
                .congs(congdong.getCongs())
                .startAt(congdong.getStartAt())
                .build();

        log.info("🚀 최종 반환 DTO: {}", responseDTO);

        return ResponseEntity.ok(responseDTO);
    }

    // **상품 ID로 congdongIng 테이블의 전체 데이터 가져오기**
    public List<CongDongIng> getCongdongIngByProductId(Long productId) {
        log.info("공동구매 전체 목록 조회 - productId: {}", productId);

        // DB에서 해당 productId에 대한 공동구매 목록 조회
        List<CongDongIng> congdongIngList = congdongIngRepository.findAllByProductId(productId);

        // 공동구매 정보가 없으면 빈 리스트 반환
        if (congdongIngList.isEmpty()) {
            log.warn("해당 상품의 공동구매 정보 없음 - productId: {}", productId);
            return Collections.emptyList();
        }

        log.info("조회된 공동구매 목록: {}", congdongIngList);
        return congdongIngList;
    }


    public List<ProdReadResponseDTO> getAllCongDongProducts() {
        log.info("Fetching all CongDong products..."); // 로그 추가

        List<Congdong> congdongs = congdongRepository.findAll();
        log.info("CongDong records retrieved: {}", congdongs); // 로그 추가

        List<ProdReadResponseDTO> responseDTOs = congdongs.stream()
                .map(congdong -> {
                    Product product = congdong.getProduct();
                    ProdReadResponseDTO dto = ProdReadResponseDTO.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .price(product.getPrice())
                            .mainPicturePath(product.getMainPicturePath())
                            .description(product.getDescription())
                            .quantity(product.getQuantity())
                            .category(product.getCategory())
                            .isCong(product.isCong())
                            .condition(congdong.getConditions())
                            .sellerId(product.getSellerId())
                            .build();
                    log.info("Mapped DTO: {}", dto); // 로그 추가
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("All CongDong products retrieved: {}", responseDTOs); // 로그 추가
        return responseDTOs;
    }
}
