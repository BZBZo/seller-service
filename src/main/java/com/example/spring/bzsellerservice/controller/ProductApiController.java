package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.congdong.CongdongDTO;
import com.example.spring.bzsellerservice.dto.product.ProdUploadRequestDTO;
import com.example.spring.bzsellerservice.dto.product.ProdUploadResponseDTO;
import com.example.spring.bzsellerservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/seller/product")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProdUploadResponseDTO> addProduct(@ModelAttribute ProdUploadRequestDTO dto) {
        log.info("DTO condition: {}", dto.getCondition()); // 값이 제대로 넘어오는지 확인
        try {
            // 전달된 DTO 값 확인
            log.info("Description received: {}", dto.getDescription());
            log.info("Condition received: {}", dto.getCondition());
            // 상품 정보 로깅
            System.out.println("상품명 : " + dto.getName() + '\n'
                    + "가격 : " + dto.getPrice() + '\n'
                    + "수량 : " + dto.getQuantity() + '\n'
                    + "카테고리 : " + dto.getCategory() + '\n'
                    + "설명 : " + dto.getDescription() + '\n'
                    + "공동구매 : " + dto.isCong() + '\n'
                    + "모집인원 and 할인율 : " + (dto.getCondition() != null ? dto.getCondition() : "조건 없음") + '\n');

            MultipartFile mainPicture = dto.getMainPicture();
            String mainPicturePath = null;

            // 설명 텍스트에서 절대 경로를 상대 경로로 변경
            String description = dto.getDescription();
            String formatDescription = description.replace("http://localhost:8088/uploads/", "/uploads/");
            dto.setDescription(formatDescription);

            System.out.println("메인 이미지: " + (mainPicture != null ? mainPicture.getOriginalFilename() : "없음"));

            if (mainPicture != null && !mainPicture.isEmpty()) {
                log.info("초기 mainPicturePath: {}", mainPicturePath);

                try {
                    // 파일 저장 로직
                    String fileNameExtension = mainPicture.getOriginalFilename().substring(mainPicture.getOriginalFilename().lastIndexOf("."));
                    String standardizedFileName = "main_" + System.currentTimeMillis() + fileNameExtension;
                    Path savePath = Paths.get("src/main/resources/static/uploads/", standardizedFileName);
                    Files.createDirectories(savePath.getParent());

                    int counter = 1;
                    while (Files.exists(savePath)) {
                        standardizedFileName = "main_" + System.currentTimeMillis() + "_" + counter + fileNameExtension;
                        savePath = Paths.get("src/main/resources/static/uploads/", standardizedFileName);
                        counter++;
                    }

                    Files.copy(mainPicture.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
                    mainPicturePath = "/uploads/" + standardizedFileName;
                    dto.setMainPicturePath(mainPicturePath);

                    log.info("저장된 메인 이미지 경로: {}", mainPicturePath);
                    log.info("최종 저장된 메인 이미지 경로: {}", mainPicturePath);
                } catch (IOException e) {
                    log.error("메인 이미지 저장 중 오류 발생", e);
                    return ResponseEntity.status(500).body(
                            ProdUploadResponseDTO.builder()
                                    .url("/seller/product/upload")
                                    .build()
                    );
                }
            } else {
                log.warn("메인 이미지가 업로드되지 않았습니다.");
            }

            // Product 저장
            Long productId = productService.save(dto);

            // Congdong 저장 처리
            if (dto.isCong() && dto.getCondition() != null) {
                CongdongDTO congdongDTO = CongdongDTO.builder()
                        .productId(productId)
                        .condition(dto.getCondition())
                        .build();
                productService.saveCongdong(congdongDTO);
                System.out.println("Congdong 저장 완료: " + congdongDTO);
            }

            return ResponseEntity.ok(
                    ProdUploadResponseDTO.builder()
                            .url("/seller/product/list")
                            .mainPicturePath(mainPicturePath)
                            .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ProdUploadResponseDTO.builder()
                            .url("/seller/product/upload")
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

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> editProduct(
            @PathVariable Long id,
            @ModelAttribute ProdUploadRequestDTO dto) {
        try {
            productService.updateProduct(id, dto);
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
            productService.deleteProductsByIds(Collections.singletonList(id));  // id를 리스트로 감싸서 전달
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