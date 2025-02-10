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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongdongService {

    private final CongdongRepository congdongRepository;
    private final CongdongIngRepository congdongIngRepository;
    private final ProductRepository productRepository;
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

        // 공동구매 찾기
        CongDongIng congdong = congdongIngRepository.findByProductIdAndCondition(productId, condition)
                .orElseThrow(() -> new IllegalArgumentException("❌ 해당 조건의 공동구매가 존재하지 않습니다. productId=" + productId + ", condition=" + condition));
        log.info("✅ 공동구매 정보 찾음: {}", congdong);

        try {
            // 1️⃣ JSON 문자열을 List<Long>으로 변환
            List<Long> congsList = new ObjectMapper().readValue(congsJson, new TypeReference<List<Long>>() {});

            // 2️⃣ List<Long>을 JSON 배열 형태의 String으로 변환 (🔥 불필요한 따옴표 제거)
            String cleanedCongsJson = new ObjectMapper().writeValueAsString(congsList)
                    .replace("\"[", "[") // 앞쪽 따옴표 제거
                    .replace("]\"", "]") // 뒷쪽 따옴표 제거
                    .replaceAll("\\\\\"", ""); // 이스케이프된 따옴표 제거

            congdong.setCongs(cleanedCongsJson);
            congdongIngRepository.save(congdong);
            log.info("✅ 공동구매 참여자 목록 업데이트 완료: {}", cleanedCongsJson);

            // 3️⃣ DTO 변환 후 반환 (🔥 congs를 JSON String 그대로 유지)
            CongDongIngDTO responseDTO = CongDongIngDTO.builder()
                    .id(congdong.getId())
                    .productId(congdong.getProductId())
                    .condition(congdong.getCondition())
                    .congs(cleanedCongsJson)  // 🚀 JSON String 그대로 전달
                    .startAt(congdong.getStartAt())
                    .build();

            log.info("🚀 최종 반환 DTO: {}", responseDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (JsonProcessingException e) {
            log.error("❌ 공동구매 참여자 목록 JSON 변환 실패", e);
            throw new RuntimeException("공동구매 참여자 목록 JSON 변환 실패", e);
        }
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

    public List<CongDongIngDTO> getAllCongDongingProducts() {
        log.info("Fetching all active CongDonging products...");

        List<CongDongIng> congdongings = congdongIngRepository.findAll();
        log.info("Active CongDongIng records retrieved: {}", congdongings);

        List<CongDongIngDTO> responseDTOs = congdongings.stream()
                .map(congdongIng -> {
                    // productId를 통해 상품 정보 조회
                    Optional<Product> optionalProduct = productRepository.findById(congdongIng.getProductId());

                    if (optionalProduct.isEmpty()) {
                        log.warn("Product not found for productId: {}", congdongIng.getProductId());
                        return null; // 상품이 없으면 해당 공동구매는 건너뜀
                    }

                    Product product = optionalProduct.get();

                    // DTO 변환 (상품 정보 추가 ✅)
                    CongDongIngDTO dto = CongDongIngDTO.builder()
                            .id(congdongIng.getId())
                            .productId(congdongIng.getProductId())
                            .condition(congdongIng.getCondition()) // JSON 형태 그대로 전달
                            .congs(congdongIng.getCongs()) // JSON 형태 그대로 전달
                            .startAt(congdongIng.getStartAt()) // 시작 시간 그대로 전달
                            .name(product.getName()) // ✅ 상품명 추가
                            .mainPicturePath(product.getMainPicturePath()) // ✅ 상품 이미지 추가
                            .price(product.getPrice()) // ✅ 상품 가격 추가
                            .build();

                    log.info("Mapped CongDongIng DTO: {}", dto);
                    return dto;
                })
                .filter(Objects::nonNull) // null 값 제외
                .collect(Collectors.toList());

        log.info("All active CongDonging products retrieved: {}", responseDTOs);
        return responseDTOs;
    }

    public List<CongDongIng> getCongs(Long memberNo) {
        log.info("📢 [Service] 공동구매 참여 목록 조회 요청 - memberNo: {}", memberNo);

        // JSON_CONTAINS 방식을 사용하여 정확한 검색을 진행
        List<CongDongIng> result = congdongIngRepository.findByMemberNo(String.valueOf(memberNo));

        log.info("✅ 조회된 공동구매 목록 ({}건): {}", result.size(), result);

        return result;
    }

}
