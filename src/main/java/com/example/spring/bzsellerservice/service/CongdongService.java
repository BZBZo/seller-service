package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.congdong.CongDongIngDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.CongDongIng;
import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.repository.CongdongIngRepository;
import com.example.spring.bzsellerservice.repository.CongdongRepository;
import com.example.spring.bzsellerservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    /**
     * 공구 가능한 상품 리스트 반환
     *
     * @return List<ProdReadResponseDTO>
     */
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


}
