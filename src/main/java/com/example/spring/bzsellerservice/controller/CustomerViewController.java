//package com.example.spring.bzsellerservice.controller;
//
//import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
//import com.example.spring.bzsellerservice.entity.Congdong;
//import com.example.spring.bzsellerservice.entity.Customer;
//import com.example.spring.bzsellerservice.service.CongdongService;
////import com.example.spring.bzsellerservice.service.CustomerService;
//import com.example.spring.bzsellerservice.service.ProductttService;
//import jakarta.servlet.http.HttpSession;
//import lombok.AllArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Controller
//@AllArgsConstructor
//@RequestMapping("/user")
//public class CustomerViewController {
//
////    private final CustomerService customerService;
//    private final ProductttService productttService;
//    private final CongdongService congdongService;
//
//    // customer 상품 상세 정보 매핑 추가
//    @GetMapping("/product/detail/{id}")
//    public String detail(HttpSession session, @PathVariable Long id, Model model) {
//        // 상품 정보 찾기
//        ProdReadResponseDTO product = productttService.findById(id);
//
//        // mainPicture 경로에서 역슬래시(`\`)를 슬래시(`/`)로 변경
//        if (product.getMainPicturePath() != null) {
//            String mainPicturePath = product.getMainPicturePath().replace("\\", "/");
//            product.setMainPicturePath(mainPicturePath);
//        }
//
//        // 세션을 통해 로그인 여부 확인 (세션이 null일 때도 처리)
//        Customer customer = (session != null) ? customerService.findBySession(session) : null;
//        model.addAttribute("product", product);
//        model.addAttribute("customer", customer);
//
//        return "detail";
//    }
//
//    @GetMapping("/임시/product/list")
//    public String productList(@RequestParam(defaultValue = "0") int page, Model model) {
//        int pageSize = 15;
//        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());
//
//        Page<ProdReadResponseDTO> productPage = productttService.findAll(pageable);
//        List<ProdReadResponseDTO> products = productPage.getContent();
//
//        int totalPages = productPage.getTotalPages();
//        int pageBlock = 10;
//        int startPage = (page / pageBlock) * pageBlock;
//        int endPage = Math.min(startPage + pageBlock - 1, totalPages - 1);
//
//        model.addAttribute("products", products);
//        model.addAttribute("productPage", productPage);
//        model.addAttribute("startPage", startPage);
//        model.addAttribute("endPage", endPage);
//        model.addAttribute("showPrevious", startPage > 0);
//        model.addAttribute("showNext", endPage < totalPages - 1);
//
//        return "total_list";
//    }
//
//    @GetMapping("/search")
//    public String search() {
//        return "search";
//    }
//
//    // 상품 ID에 맞는 조건들을 반환
//    @GetMapping("/product/conditions/{productId}")
//    @ResponseBody
//    public Map<String, Object> getConditionsByProductId(@PathVariable Long productId) {
//        List<Congdong> conditions = congdongService.findConditionsByProductId(productId);
//
//        // 문자열 조건을 JSON 객체로 변환
//        List<Map<String, Integer>> formattedConditions = conditions.stream()
//                .map(condition -> Arrays.stream(condition.getConditions().split(","))
//                        .map(c -> {
//                            String[] parts = c.replace("{", "").replace("}", "").split(":");
//                            return Map.of("people", Integer.parseInt(parts[0]), "discount", Integer.parseInt(parts[1]));
//                        })
//                        .collect(Collectors.toList()))
//                .flatMap(List::stream)
//                .collect(Collectors.toList());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("message", "조건 조회 성공");
//        response.put("data", formattedConditions);
//
//        return response;
//    }
//
//}