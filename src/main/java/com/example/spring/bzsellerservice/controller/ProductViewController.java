package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;

import com.example.spring.bzsellerservice.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductViewController {

    private final SellerService sellerService;

    @GetMapping("/upload")
    public String uploadProduct() {
        //상품 등록 폼으로 이동
        return "product_upload";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        // 상품의 상세 정보를 조회하여 수정 폼에 표시할 수 있도록 모델에 추가
        ProdReadResponseDTO product = sellerService.getProductDetails(id);
        model.addAttribute("product", product);
        return "product_edit"; // 수정 페이지 HTML 파일 이름
    }

}
