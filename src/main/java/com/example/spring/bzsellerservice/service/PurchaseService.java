package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.entity.Purchase;
import com.example.spring.bzsellerservice.entity.Review;
import com.example.spring.bzsellerservice.repository.PurchaseRepository;
import com.example.spring.bzsellerservice.repository.ReviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public List<Purchase> getPurchasesByCustomer(Customer customer) {
        return purchaseRepository.findByCustomer(customer);
    }

    public void enrichPurchasesWithProducts(List<Purchase> purchases) {
        ObjectMapper objectMapper = new ObjectMapper();

        for (Purchase purchase : purchases) {
            try {
                Map<String, Integer> productMap = objectMapper.readValue(purchase.getProductList(), new TypeReference<Map<String, Integer>>() {});
                List<Product> products = new ArrayList<>();
                List<Review> reviews = reviewRepository.findByPurchaseId(purchase.getId());

                // 각 제품 ID에 대한 리뷰 여부를 매핑
                Map<Long, Boolean> reviewedProductIds = new HashMap<>();
                for (Review review : reviews) {
                    reviewedProductIds.put(review.getProduct().getId(), true);
                }

                for (Map.Entry<String, Integer> entry : productMap.entrySet()) {
                    Long productId = Long.valueOf(entry.getKey());
                    Integer quantity = entry.getValue();
                    Product product = productService.getProductById(productId);
                    product.setQuantity(String.valueOf(quantity));

                    // 각 Product 객체에 현재 Purchase의 리뷰 상태 저장
                    boolean isReviewed = reviewedProductIds.getOrDefault(productId, false);
                    product.setReviewedForPurchase(purchase.getId(), isReviewed);

                    products.add(product);
                }
                purchase.setProducts(products);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing product list from purchase", e);
            }
        }
    }


}
