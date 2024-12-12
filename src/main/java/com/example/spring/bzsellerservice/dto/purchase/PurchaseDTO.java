package com.example.spring.bzsellerservice.dto.purchase;

import com.example.spring.bzsellerservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PurchaseDTO {
    private Long purchaseId;              // 구매 ID
    private LocalDateTime date;           // 주문 날짜
    private List<Product> products;
    private String customerLoginId;
    private Integer grandTotal;
}
