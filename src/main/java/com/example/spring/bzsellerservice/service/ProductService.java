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
public class ProductService {

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

        // 허용할 태그 정의
        Safelist safelist = Safelist.basicWithImages()
                .addTags("a") // 필요한 태그 추가
                .addAttributes("a", "href", "target") // 링크 태그의 허용 속성 추가
                .addAttributes("img", "src", "alt", "title"); // 이미지 태그의 허용 속성 추가

        // HTML 파싱 및 클린 처리
        String cleanedDescription = Jsoup.clean(description, safelist);

        // 불필요한 태그 제거: <p>, <br> 등만 남은 경우
        cleanedDescription = cleanedDescription.replaceAll("(?i)<(br|p|/p|\\s)*?>", "").trim();

        // 결과가 빈 문자열이면 빈 값 반환
        return cleanedDescription.isEmpty() ? "" : cleanedDescription;
    }


    public void updateProduct(Long id, ProdUploadRequestDTO dto) throws IOException {
        // ID 유효성 검증
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상품 ID입니다: " + id));

        // 설명 필드 필터링
        String filteredDescription = filterDescription(dto.getDescription());

        // 필수 입력값 검증
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수 입력 항목입니다.");
        }
        if (dto.getPrice() <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        if (Integer.parseInt(dto.getQuantity()) < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다.");
        }
        if (dto.isCong()) { // 공구 가능 상태일 때만 검증
            if (dto.getCondition() == null || dto.getCondition().trim().isEmpty()) {
                throw new IllegalArgumentException("조건 값이 비어 있습니다.");
            }
            validateConditions(dto.getCondition());
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("설명은 필수 입력 항목입니다.");
        }

        boolean wasCong = existingProduct.isCong();
        boolean isCong = dto.isCong();

        // 상품 상세 정보 업데이트
        updateProductDetails(existingProduct, dto, filteredDescription);

        // 상품 저장
        productRepository.save(existingProduct);

        // 공구 조건 처리
        handleCongdong(existingProduct, wasCong, isCong, dto.getCondition());

        log.info("상품이 성공적으로 업데이트되었습니다. ID: {}", existingProduct.getId());
    }

    public ProdReadResponseDTO getProductDetails(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Congdong 데이터 가져오기
        Optional<Congdong> congdong = congdongRepository.findByProductId(id);
        String condition = congdong.map(Congdong::getConditions).orElse("{1:0}"); // 기본값 설정

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
        // 공구 활성화로 변경되었을 때 조건 값 검증
        if (!wasCong && isCong) {
            if (condition == null || condition.trim().isEmpty()) {
                throw new IllegalArgumentException("공구를 활성화하려면 조건이 필요합니다.");
            }
            validateConditions(condition);

            // 기존 공구 데이터 확인 후 처리
            Optional<Congdong> existingCongdong = congdongRepository.findByProductId(product.getId());
            if (existingCongdong.isPresent()) {
                updateCongdongCondition(product.getId(), condition);
            } else {
                createCongdongIfNotExists(product.getId(), condition);
            }
        }
        // 공구 비활성화로 변경되었을 때
        else if (wasCong && !isCong) {
            deleteCongdongByProductId(product.getId());
            return; // 공구가 아닌 경우 추가 로직 생략
        }
        // 공구 상태 유지하면서 조건 업데이트
        else if (wasCong && isCong) {
            if (condition != null && !condition.trim().isEmpty()) {
                validateConditions(condition);
                updateCongdongCondition(product.getId(), condition);
            } else {
                log.info("공구 조건이 제공되지 않아 업데이트가 수행되지 않았습니다.");
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
        congdong.setConditions(condition); // 새로운 조건으로 업데이트
        congdongRepository.save(congdong);
        log.info("Congdong updated for Product ID: {}, New Condition: {}", productId, condition);
    }

    private void validateConditions(String conditions) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("Condition cannot be null or empty.");
        }

        String[] conditionArray = conditions.split(","); // 콤마로 나눠서 각 조건 검증
        for (String condition : conditionArray) {
            // 각 조건을 검증
            if (!condition.matches("\\{\\d+:\\d+\\}")) {
                throw new IllegalArgumentException("Condition must follow the format: {people:discount}");
            }

            String[] parts = condition.replace("{", "").replace("}", "").split(":");
            int people = Integer.parseInt(parts[0].trim());
            int discount = Integer.parseInt(parts[1].trim());

            if (people < 1) {
                throw new IllegalArgumentException("The 'people' value must be greater than 0.");
            }
            if (discount < 0) {
                throw new IllegalArgumentException("The 'discount' value must be 0 or greater.");
            }
        }
    }

    private void updateProductDetails(Product product, ProdUploadRequestDTO dto, String filteredDescription) throws IOException {
        // 기본 필드 업데이트
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setDescription(filteredDescription); // 필터링된 설명 적용
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