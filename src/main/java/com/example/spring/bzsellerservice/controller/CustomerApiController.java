package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.UrlResponseDTO;
import com.example.spring.bzsellerservice.dto.customer.SignUpRequestDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.service.CustomerService;
import com.example.spring.bzsellerservice.service.ProductttService;
import com.example.spring.bzsellerservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CustomerApiController {

    private final CustomerService customerService;
    private final ProductttService productttService;
    private final ProductService productService;

    @PostMapping("/join")
    public ResponseEntity<UrlResponseDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        customerService.save(signUpRequestDTO); // 회원가입 진행 (DB 저장)
        return ResponseEntity.ok(
                UrlResponseDTO.builder()
                        .url("/user/login") // 회원 가입이 완료된 후 로그인 페이지로 이동
                        .build()
        );
    }

    @GetMapping("/product/api/list")
    @ResponseBody
    public Map<String, Object> getProductListJson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<ProdReadResponseDTO> productPage = productttService.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("last", productPage.isLast());
        response.put("totalPages", productPage.getTotalPages());
        response.put("currentPage", page);

        return response;
    }

    // 상품 목록을 JSON 형태로 반환하는 API, 이름 필터 추가
    @GetMapping("/product/search")
    @ResponseBody
    public Page<ProdReadResponseDTO> searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name) {
        Pageable pageable = PageRequest.of(page, size);

        if (name != null && !name.isEmpty()) {
            return productttService.searchProductsByName(name, pageable);
        } else {
            return productService.findAll(pageable);
        }
    }

}