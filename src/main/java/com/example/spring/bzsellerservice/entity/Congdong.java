package com.example.spring.bzsellerservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "congdong")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
public class Congdong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE"
            )
    )
    private Product product;

    @Column(columnDefinition = "TEXT")
    private String conditions; // 모집인원 및 할인율 정보 JSON으로 저장

    public static Congdong create(Product product, String condition) {
        Congdong congdong = new Congdong();
        congdong.setProduct(product);
        congdong.setConditions(condition);
        return congdong;
    }

}