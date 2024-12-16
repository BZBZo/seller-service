package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.service.ProductService;
import com.example.spring.bzsellerservice.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerViewController {

    private final ProductService productService;
    private final SellerService sellerService;

    // 판매자가 판매하는 상품들
    @GetMapping("/product/list")
    public String productList(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());

        Page<ProdReadResponseDTO> productPage = productService.findAll(pageable);
        List<ProdReadResponseDTO> products = productPage.getContent();

        int totalPages = productPage.getTotalPages();
        int pageBlock = 10; // 페이지 블록 크기
        int startPage = (page / pageBlock) * pageBlock;
        int endPage = Math.min(startPage + pageBlock - 1, totalPages - 1);

        model.addAttribute("products", products);
        model.addAttribute("productPage", productPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("showPrevious", startPage > 0);
        model.addAttribute("showNext", endPage < totalPages - 1);

        return "product_list";
    }

    @GetMapping("/product/upload")
    public String uploadProduct() {
        //상품 등록 폼으로 이동
        return "upload_product";
    }

    // admin 상품 상세 정보 매핑 추가
    @GetMapping("/product/detail/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProdReadResponseDTO product = sellerService.getProductDetails(id);
        model.addAttribute("product", product);
        return "detail_product"; // 상세 페이지 HTML 파일 이름 반환
    }

    @GetMapping("/product/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        // 상품의 상세 정보를 조회하여 수정 폼에 표시할 수 있도록 모델에 추가
        ProdReadResponseDTO product = sellerService.getProductDetails(id);
        model.addAttribute("product", product);
        return "edit_product"; // 수정 페이지 HTML 파일 이름
    }

}
