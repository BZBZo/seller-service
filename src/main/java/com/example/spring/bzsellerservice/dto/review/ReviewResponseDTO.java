package com.example.spring.bzsellerservice.dto.review;


import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.entity.Product;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@RequiredArgsConstructor
public class ReviewResponseDTO {
    private final Long id;
    private final String picturePath;
    private final String content;
    private final Product product;
    private final Customer customer;
    private final LocalDateTime date;
    private final String loginId;
}
