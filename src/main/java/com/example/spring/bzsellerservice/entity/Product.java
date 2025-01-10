package com.example.spring.bzsellerservice.entity;

import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer price;
    private Long sellerId;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private String quantity;
    private String category;
    private String mainPicturePath;
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isCong;

    // 리뷰 여부를 관리하는 Map
    @Transient // 이 필드는 데이터베이스에 저장되지 않음
    private Map<Long, Boolean> reviewedPurchases = new HashMap<>();

    @Builder
    public Product(Long id, String name, Integer price, Long sellerId, String mainPicturePath, String description, String quantity, String category, boolean isCong) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sellerId = sellerId;
        this.mainPicturePath = mainPicturePath;
        this.description = description;
        this.quantity = quantity;
        this.category = category;
        this.isCong = isCong;
    }

    // 특정 구매 ID에 대한 리뷰 상태를 반환
    public boolean isReviewedForPurchase(Long purchaseId) {
        return reviewedPurchases.getOrDefault(purchaseId, false);
    }

    // 특정 구매 ID의 리뷰 상태를 설정
    public void setReviewedForPurchase(Long purchaseId, boolean reviewed) {
        reviewedPurchases.put(purchaseId, reviewed);
    }
    public void setIsCong(boolean isCong) {
        this.isCong = isCong;
    }

    public ProdReadResponseDTO toProdReadResponseDTO() {
        return ProdReadResponseDTO.builder()
                .id(id)
                .name(name)
                .price(price)
                .mainPicturePath(mainPicturePath)
                .description(description)
                .quantity(quantity)
                .category(category)
                .isCong(isCong)
                .build();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", mainPicturePath='" + mainPicturePath + '\'' +
                ", description='" + description + '\'' +
                ", quantity='" + quantity + '\'' +
                ", category='" + category + '\'' +
                ", isCong='" + isCong + '\'' +
                '}';
    }
}
