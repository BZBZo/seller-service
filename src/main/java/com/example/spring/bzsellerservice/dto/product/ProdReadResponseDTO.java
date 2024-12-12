package com.example.spring.bzsellerservice.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProdReadResponseDTO {
    private Long id;
    private String name;
    private Integer price;
    private String mainPicture;
    private String mainPicturePath; // mainPicturePath 필드 추가
    private String description;
    private String quantity;
    private String category;
    private boolean isCong;
}
