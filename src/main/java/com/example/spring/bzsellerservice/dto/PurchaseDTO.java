package com.example.spring.bzsellerservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PurchaseDTO {
    Long purchaseId;
    String orderId;
    String paymentKey;
    Double totalAmount;
    String approvedAt;
    String method;
    Long memberNo;
    String productList;
}
