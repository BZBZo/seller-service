package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.repository.CongdongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongdongService {

    private final CongdongRepository congdongRepository;

    /**
     * 상품 ID에 해당하는 모든 조건 반환
     *
     * @param productId 상품 ID
     * @return List<Congdong>
     */
    public List<Congdong> findConditionsByProductId(Long productId) {
        log.info("Finding conditions for product ID: {}", productId); // 로그 추가
        List<Congdong> conditions = congdongRepository.findAllByProductId(productId);
        log.info("Conditions found: {}", conditions); // 로그 추가
        return conditions;
    }

    /**
     * 상품 ID에 해당하는 조건 정보와 상품 정보를 통합하여 반환
     *
     * @param productId 상품 ID
     * @return DTO 형태의 조건 정보와 상품 정보
     */
    public ProdReadResponseDTO getProductWithCondition(Long productId) {
        log.info("Fetching product with condition for product ID: {}", productId); // 로그 추가

        Optional<Congdong> congdong = congdongRepository.findByProductId(productId);

        if (congdong.isEmpty()) {
            log.warn("No condition information found for product ID: {}", productId); // 로그 추가
            throw new IllegalArgumentException("해당 상품에 대한 조건 정보가 없습니다. 상품 ID: " + productId);
        }

        Product product = congdong.get().getProduct();

        ProdReadResponseDTO responseDTO = ProdReadResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .mainPicturePath(product.getMainPicturePath())
                .description(product.getDescription())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .isCong(product.isCong())
                .condition(congdong.get().getConditions())
                .sellerId(product.getSellerId())
                .build();

        log.info("Product with condition retrieved: {}", responseDTO); // 로그 추가
        return responseDTO;
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
}

