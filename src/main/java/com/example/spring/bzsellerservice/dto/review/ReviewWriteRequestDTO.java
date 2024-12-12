package com.example.spring.bzsellerservice.dto.review;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
public class ReviewWriteRequestDTO {
    private Long productId;
    private String customerId;
    private Long purchaseId;
    private MultipartFile picture;
    private String content;
}
