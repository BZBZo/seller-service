package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.congdong.CongDongIngDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.CongDongIng;
import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.repository.CongdongIngRepository;
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
    private final CongdongIngRepository congdongIngRepository;

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
}
