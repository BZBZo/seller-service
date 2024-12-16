package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.congdong.CongdongDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.dto.product.ProdUploadRequestDTO;
import com.example.spring.bzsellerservice.entity.Congdong;
import com.example.spring.bzsellerservice.entity.Product;
import com.example.spring.bzsellerservice.repository.CongdongRepository;
import com.example.spring.bzsellerservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final ProductRepository productRepository;
    private final CongdongRepository congdongRepository; // Congdong Repository 추가

    public Page<ProdReadResponseDTO> findAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(Product::toProdReadResponseDTO);
    }

    public Long save(ProdUploadRequestDTO dto) throws IOException {

        // 설명 필드 필터링 추가
        String description = filterDescription(dto.getDescription());

        String mainPicturePath = saveFile(dto.getMainPicture());

        Product product = dto.toProduct();
        product.setMainPicturePath(mainPicturePath);
        product.setDescription(description);
        product.setIsCong(dto.isCong());
        Product savedProduct = productRepository.save(product);

        log.info("Product saved with ID: {}", savedProduct.getId());

        handleCongdong(savedProduct, false, dto.isCong(), dto.getCondition());

        return savedProduct.getId();
    }

    public void saveCongdong(CongdongDTO dto) {
        Optional<Congdong> existingCongdong = congdongRepository.findByProductId(dto.getProductId());

        if (existingCongdong.isPresent()) {
            // 중복된 경우 처리: 로그만 남기고 저장하지 않음
            log.warn("Congdong already exists for product ID: {}", dto.getProductId());
            return;
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + dto.getProductId()));

        Congdong congdong = Congdong.create(product, dto.getCondition());
        congdongRepository.save(congdong);

        log.info("Congdong saved for product ID: {}", dto.getProductId());
        log.info("Congdong condition: {}", dto.getCondition());
    }

    // description에 있는 html 태그 필터링
    public String filterDescription(String description) {
        if (description == null || description.isEmpty()) {
            return ""; // 빈 값 처리
        }
        // 허용할 태그를 정의 (예: <p>, <img>, <a>)
        Safelist safelist = Safelist.basicWithImages()
                .addTags("p", "a") // 필요한 태그 추가
                .addAttributes("a", "href", "target") // 링크 태그의 허용 속성 추가
                .addAttributes("img", "src", "alt", "title"); // 이미지 태그의 허용 속성 추가

        // HTML 파싱 및 클린 처리
        return Jsoup.clean(description, safelist);
    }


    public void updateProduct(Long id, ProdUploadRequestDTO dto) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        boolean wasCong = existingProduct.isCong();
        boolean isCong = dto.isCong();

        updateProductDetails(existingProduct, dto);
        productRepository.save(existingProduct);

        handleCongdong(existingProduct, wasCong, isCong, dto.getCondition());

        log.info("Product updated with ID: {}", existingProduct.getId());
    }

    public ProdReadResponseDTO getProductDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Congdong 데이터 가져오기
        Optional<Congdong> congdong = congdongRepository.findByProductId(id);
        String condition = congdong.map(Congdong::getConditon).orElse("{1:0}"); // 기본값 설정

        return ProdReadResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .description(product.getDescription())
                .mainPicturePath(product.getMainPicturePath())
                .isCong(product.isCong())
                .condition(condition) // 기본값 포함
                .build();
    }

    // save와 updateProduct의 congdong 중복으로 인한 메서드
    private void handleCongdong(Product product, boolean wasCong, boolean isCong, String condition) {
        if (!wasCong && isCong) {
            if (condition == null || condition.isEmpty()) {
                throw new IllegalArgumentException("Condition must be provided when enabling Congdong.");
            }
            validateCondition(condition);
            createCongdongIfNotExists(product.getId(), condition);
        } else if (wasCong && !isCong) {
            deleteCongdongByProductId(product.getId());
        } else if (wasCong && isCong) {
            if (condition != null && !condition.isEmpty()) {
                validateCondition(condition);
                updateCongdongCondition(product.getId(), condition);
            } else {
                log.info("Condition not provided for existing Congdong. No update performed.");
            }
        }
    }

    // 불가에서 가능으로 바꿀 때 congdong 생성
    public void createCongdongIfNotExists(Long productId, String condition) {
        Optional<Congdong> existingCongdong = congdongRepository.findByProductId(productId);
        if (existingCongdong.isEmpty()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productId));
            Congdong congdong = Congdong.create(product, condition);
            congdongRepository.save(congdong);
            log.info("Congdong created for Product ID: {}, Condition: {}", productId, condition);
        }
    }

    // product id 따라 congdong 삭제
    public void deleteCongdongByProductId(Long productId) {
        congdongRepository.findByProductId(productId)
                .ifPresent(congdong -> {
                    congdongRepository.delete(congdong);
                    log.info("Congdong deleted for Product ID: {}", productId);
                });
    }

    // 모집인원:할인율 업데이트
    public void updateCongdongCondition(Long productId, String condition) {
        Congdong congdong = congdongRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No Congdong found for Product ID: " + productId));
        congdong.setConditon(condition); // 새로운 조건으로 업데이트
        congdongRepository.save(congdong);
        log.info("Congdong updated for Product ID: {}, New Condition: {}", productId, condition);
    }

    private void validateCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            throw new IllegalArgumentException("Condition cannot be null or empty.");
        }

        // 조건 파싱
        String[] parts = condition.replace("{", "").replace("}", "").split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Condition must follow the format: {people:discount}");
        }

        try {
            int people = Integer.parseInt(parts[0].trim());
            int discount = Integer.parseInt(parts[1].trim());

            if (people < 0) {
                throw new IllegalArgumentException("The 'people' value in condition must be 0 or greater.");
            }
            if (discount < 0) {
                throw new IllegalArgumentException("The 'discount' value in condition must be 0 or greater.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Condition values must be valid integers.");
        }
    }

    private void updateProductDetails(Product product, ProdUploadRequestDTO dto) throws IOException {
        // 기본 필드 업데이트
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setDescription(dto.getDescription());
        product.setIsCong(dto.isCong());

        // 새 메인 이미지가 있는 경우 처리
        if (dto.getMainPicture() != null && !dto.getMainPicture().isEmpty()) {
            // 기존 이미지가 있다면 삭제
            if (product.getMainPicturePath() != null) {
                Path previousImagePath = Paths.get("src/main/resources/static" + product.getMainPicturePath());
                Files.deleteIfExists(previousImagePath);
            }

            // 새로운 이미지 저장 후 경로 설정
            String mainPicturePath = saveFile(dto.getMainPicture());
            product.setMainPicturePath(mainPicturePath); // 새 이미지 경로를 mainPicturePath에 저장
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        String uploadDir = "src/main/resources/static/uploads/";
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path filePath = Paths.get(uploadDir + fileName);

        if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 클라이언트에서 접근할 수 있는 상대 경로를 반환
        return "/uploads/" + fileName;
    }

    private String generateUniqueFileName(String originalName) {
        String baseName = StringUtils.stripFilenameExtension(originalName);
        String extension = StringUtils.getFilenameExtension(originalName);
        String fileName = baseName + "_" + System.currentTimeMillis() + "." + extension;
        return fileName;
    }

    public void deleteProductsByIds(List<Long> productIds) {
        productRepository.deleteAllById(productIds);
        log.info("Deleted products with IDs: " + productIds);
    }

}