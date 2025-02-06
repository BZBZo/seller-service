package com.example.spring.bzsellerservice.dto.congdong;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CongDongIngDTO {
    private Long id;             // 공동구매 ID
    private Long productId;      // 상품 ID
    private String condition;    // 조건 (JSON 형태)
    private String congs;        // 참여자 목록 (JSON 형태)
    private LocalDateTime startAt; // 시작 시간

    private String name;            // 상품명
    private String mainPicturePath; // 상품 이미지 경로
    private Integer price;          // 상품 가격
}
