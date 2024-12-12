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

        // Product 저장
        String mainPicturePath = saveFile(dto.getMainPicture());
        Product product = dto.toProduct();
        product.setMainPicturePath(mainPicturePath);
        product.setIsCong(dto.isCong());
        Product savedProduct = productRepository.save(product);

        log.info("AdminService - condition: {}", dto.getCondition());
        log.info("Product ID: {}", savedProduct.getId()); // Product 저장 결과 로그

        // Congdong 저장
        if (dto.isCong() && dto.getCondition() != null) {
            Congdong congdong = Congdong.create(savedProduct, dto.getCondition());
            congdongRepository.save(congdong); // Congdong만 저장
            log.info("Congdong saved for Product ID: " + savedProduct.getId());
            log.info("Congdong condition: {}", congdong.getConditon()); // Congdong 조건 로그
            log.info("Product-Congdong mapping: {}", congdong.getProduct().getId()); // 매핑 확인 로그
        }

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

    public void updateProduct(Long id, ProdUploadRequestDTO dto) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        updateProductDetails(existingProduct, dto);
        productRepository.save(existingProduct);
        log.info("Product updated with ID: " + existingProduct.getId());
        log.info("Product updated with ID: " + existingProduct);
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