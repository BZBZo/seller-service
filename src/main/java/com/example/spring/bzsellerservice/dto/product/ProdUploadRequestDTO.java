package com.example.spring.bzsellerservice.dto.product;

import com.example.spring.bzsellerservice.entity.Product;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ProdUploadRequestDTO {
    private String name;
    private Integer price;
    private String quantity;
    private String category;
    private String description;
    private MultipartFile mainPicture; // 메인 이미지 파일
    private String mainPicturePath; // 메인 이미지 경로 추가
    private boolean isCong;
    private String condition; // 모집인원과 할인율 정보를 JSON 형식으로 저장

    public Product toProduct() {
        System.out.println("엔티티의 isCong 값: " + isCong);
        return Product.builder()
                .name(name)
                .price(price)
                .quantity(quantity)
                .category(category)
                .description(description)
                .mainPicturePath(mainPicturePath) // Product 객체에 mainPicturePath 설정
                .isCong(isCong)
                .build();
    }

}