package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.dto.product.ProdUploadRequestDTO;
import com.example.spring.bzsellerservice.dto.product.ProdUploadResponseDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductApiController {

    private final SellerService sellerService;

    // 판매자가 판매하는 상품들
    @GetMapping("/list")
    Page<ProdReadResponseDTO> getProductList(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestHeader("Accept") String acceptHeader){
        System.out.println("client 도착");
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProdReadResponseDTO> productPage = sellerService.findAll(pageable);
        log.info("Response: {}", productPage);
        return productPage;
    }

    // 판매자가 판매하는 상품들
    @GetMapping("/myMarket")
    Page<ProdReadResponseDTO> loadMyProduct(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestHeader("Authorization") String token
    ){
        // 토큰 로그 확인
        System.out.println("Token received in Seller Controller: " + token);
        System.out.println("client 도착");

        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Authorization token is missing");
        }

        // 페이징 처리
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        // 서비스 호출
        Page<ProdReadResponseDTO> productPage = sellerService.findAll(pageable);

        return productPage;
    }

    @GetMapping("/edit/{id}")
    public ResponseEntity<ProdReadResponseDTO> editProduct(@PathVariable Long id) {
        // 상품의 상세 정보를 조회
        ProdReadResponseDTO product = sellerService.getProductDetails(id);
        return ResponseEntity.ok(product); // JSON 형식으로 반환
    }


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

    @PostMapping
    public ResponseEntity<ProdUploadResponseDTO> addProduct(
            @ModelAttribute ProdUploadRequestDTO dto,
            @RequestHeader("Authorization") String token
    ) {
        try {
            log.info("Authorization Header: {}", token);

            // 서비스 호출
            String redirectUrl = sellerService.processProductUpload(dto);

            log.info("Product upload successful. Redirecting to: {}", redirectUrl);

            return ResponseEntity.ok(
                    ProdUploadResponseDTO.builder()
                            .url(redirectUrl)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error during product upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ProdUploadResponseDTO.builder()
                            .url("/product/upload") // 에러 발생 시 리다이렉트 URL
                            .build()
            );
        }
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filenameExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            String standardizedFilename = "detail_" + System.currentTimeMillis() + filenameExtension;

            Path filePath = Paths.get("src/main/resources/static/uploads/", standardizedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/" + standardizedFilename;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editProduct(
            @PathVariable Long id,
            @ModelAttribute ProdUploadRequestDTO dto) {
        try {
            sellerService.updateProduct(id, dto);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 제품 ID 입력 {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생 {}: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "상품 수정 중 서버 오류가 발생했습니다. 다시 시도해 주세요."
            ));
        }
    }

    @DeleteMapping("/detail/{id}")
    public ResponseEntity<?> removeProduct(@PathVariable Long id) {
        try {
            sellerService.deleteProductsByIds(Collections.singletonList(id));  // id를 리스트로 감싸서 전달
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상품이 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "상품 삭제에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
    }

}