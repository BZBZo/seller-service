package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.review.ReviewResponseDTO;
import com.example.spring.bzsellerservice.dto.review.ReviewWriteRequestDTO;
import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.entity.Purchase;
import com.example.spring.bzsellerservice.entity.Review;
import com.example.spring.bzsellerservice.repository.CustomerRepository;
import com.example.spring.bzsellerservice.repository.ProductRepository;
import com.example.spring.bzsellerservice.repository.PurchaseRepository;
import com.example.spring.bzsellerservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;

    public Page<ReviewResponseDTO> findReviewsByProductId(Long productId, Pageable pageable) {
        // productId를 기반으로 리뷰를 찾는 리포지토리 메소드 호출
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        // Review 엔티티를 CustomerReviewResponseDTO로 변환
        return reviewPage.map(this::convertToDto);
    }

    private ReviewResponseDTO convertToDto(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .loginId(review.getCustomer().getLoginId()) // customer 테이블의 loginId 포함
                .picturePath(review.getPicturePath())
                .content(review.getContent())
                .product(review.getProduct())
                .customer(review.getCustomer())
                .date(review.getDate())
                .build();
    }

    public String handleFileUpload(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = "picture_" + System.currentTimeMillis() + fileExtension;
            Path savePath = Paths.get("src/main/resources/static/uploads/", fileName);

            Files.createDirectories(savePath.getParent());
            Files.copy(file.getInputStream(), savePath);

            return "/uploads/" + fileName;
        }
        return null;
    }

    public void saveReview(ReviewWriteRequestDTO requestDTO, String picturePath) {
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
        Customer customer = customerRepository.findById(Long.parseLong(requestDTO.getCustomerId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer ID"));
        Purchase purchase = purchaseRepository.findById(requestDTO.getPurchaseId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid purchase ID"));


        Review review = Review.fromDTO(requestDTO, picturePath, product, customer, purchase);
        reviewRepository.save(review);
    }

    public void updateReview(ReviewWriteRequestDTO requestDTO, String picturePath) {
        // 기존 리뷰를 찾는 로직
        Review existingReview = reviewRepository.findByPurchaseIdAndProductIdAndCustomerId(
                        requestDTO.getPurchaseId(), requestDTO.getProductId(), Long.parseLong(requestDTO.getCustomerId()))
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // 기존 리뷰 데이터 업데이트
        existingReview.updateFromDTO(requestDTO, picturePath);

        // 리뷰 저장
        reviewRepository.save(existingReview);
    }

    public void deleteReview(Long productId, Long purchaseId) {
        Review existingReview = reviewRepository.findByPurchaseIdAndProductId(purchaseId,productId)
                .orElseThrow(()->new IllegalArgumentException("Review not found"));
        reviewRepository.delete(existingReview);
    }

    public Integer countReview(Long productId) {
        return reviewRepository.countByProductId(productId).intValue();
    }
    // customerId로 리뷰 조회 메서드
    public Page<ReviewResponseDTO> findReviewsByCustomerId(Long customerId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByCustomerId(customerId, pageable);
        return reviewPage.map(this::convertToDto);
    }

    public Review findByIds(Long purchaseId, Long productId, Long customerId) {
        return reviewRepository.findByPurchaseIdAndProductIdAndCustomerId(purchaseId, productId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("No review found for the specified ids"));
    }
}
