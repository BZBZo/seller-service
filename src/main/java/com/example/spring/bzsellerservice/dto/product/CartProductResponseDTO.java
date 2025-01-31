package com.example.spring.bzsellerservice.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor // 모든 필드를 포함하는 생성자 추가
public class CartProductResponseDTO {
    private Long id;
    private String name;
    private Integer price;
    private String mainPicturePath;
}