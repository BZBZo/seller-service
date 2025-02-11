package com.example.spring.bzsellerservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SaleHistoryDTO {
    Long id;
    String orderId;
    String approvedAt;
    Long memberNo;
    Long sellerId;
    Long productId;
    int quantity;
    String imgUrl;
    String productName;
    Integer price;
}
