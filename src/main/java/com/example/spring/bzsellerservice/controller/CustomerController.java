package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.product.CartProductResponseDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class CustomerController {
    private final SellerService sellerService;

    @GetMapping("/detail/po/{id}")
    public ResponseEntity<ProdReadResponseDTO> getProductDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        // 토큰 확인을 위한 로그
        System.out.println("Token received in Seller Controller: " + token);

        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Authorization token is missing");
        }

        ProdReadResponseDTO product = sellerService.getProductDetails(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    @GetMapping("/myShop")
    Page<ProdReadResponseDTO> findAllBySellerId(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam Long sellerId){

        // 페이징 처리
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // 서비스 호출
        Page<ProdReadResponseDTO> productPage = sellerService.findAllBySellerId(pageable, sellerId);

        return productPage;

    }

    @PostMapping("/cart/list")
    public ResponseEntity<?> getProductsByIds(@RequestBody List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Product IDs cannot be empty"
            ));
        }
        try {
            List<CartProductResponseDTO> products = sellerService.getProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid product IDs: {}", productIds, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching products by IDs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred. Please try again."
            ));
        }
    }
}
