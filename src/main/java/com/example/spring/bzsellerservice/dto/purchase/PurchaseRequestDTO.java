package com.example.spring.bzsellerservice.dto.purchase;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class PurchaseRequestDTO {
    private List<PurchaseProductDTO> products;
    private final Long customerId;
    private Integer grandTotal;
    private LocalDateTime localDateTime;
}
