package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.dto.congdong.CongDongIngDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.CongDongIng;
import com.example.spring.bzsellerservice.service.CongdongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class CongdongController {

    private final CongdongService congdongService;

    @GetMapping("/congdong")
    public List<ProdReadResponseDTO> getCongDongProducts() {
        log.info("[Seller Service] 공구 가능한 상품 요청 수신");
        log.info("Fetching CongDong products..."); // 로그 추가

        List<ProdReadResponseDTO> products = congdongService.getAllCongDongProducts();

        log.info("[Seller Service] 공구 가능한 상품 데이터 반환. 상품 수: {}", products.size());
        log.info("[Seller Service] 상품 데이터: {}", products);

        return products;
    }

    @GetMapping("/congdonging")
    public List<CongDongIngDTO> getCongDongActiveProducts() {
        List<CongDongIngDTO> activeProducts = congdongService.getAllCongDongingProducts();

        return activeProducts;
    }

    @PostMapping("/congdong")
    public ResponseEntity<CongDongIngDTO> startCongdong(
            @RequestBody Map<String, Object> requestBody // JSON 데이터 받기
    ) {

        Long productId = Long.valueOf(requestBody.get("productId").toString());
        String condition = requestBody.get("condition").toString();
        List<Long> congs = (List<Long>) requestBody.get("congs");

        log.info("Received JSON for starting CongDong: productId={}, condition={}, congs={}",
                productId, condition, congs);

        // 서비스 호출
        CongDongIngDTO newCongdong = congdongService.startCongdong(productId, condition, congs);

        // 결과 반환
        return ResponseEntity.ok(newCongdong);
    }

    // ✅ 공동구매 참여 (PUT)
    @PutMapping("/congdong")
    public ResponseEntity<CongDongIngDTO> joinCongdong(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> requestBody
    ) {
        log.info("🔵 공동구매 참여 요청 수신 (Seller Controller) - Token: {}", token);
        log.info("📌 요청 바디: {}", requestBody);

        Long productId = Long.valueOf(requestBody.get("productId").toString());
        String condition = requestBody.get("condition").toString();
        String congsJson = requestBody.get("congs").toString(); // JSON String 그대로 받기

        // 🔥 따옴표(") 제거 후 로깅
        String cleanedCongsJson = congsJson.replaceAll("\"", "");
        log.info("🔍 수신된 congs(JSON, 정리됨): {}", cleanedCongsJson);

        log.info("🔎 추출된 productId: {}, condition: {}", productId, condition);
        log.info("🔍 수신된 congs(JSON): {}", congsJson);

        return congdongService.joinCongdong(token, productId, condition, congsJson);
    }

    @PutMapping("/congdong/state")
    void completeCongdong(@RequestParam Long id, @RequestBody List<Long> congs){
        congdongService.completeCongdong(id, congs);
    }

    @PutMapping("/congdong/pay")
    void updateCongPayState(@RequestParam Long congId, @RequestParam Long memberNo){
        congdongService.updateCongPayState(congId, memberNo);
    }


    // **상품 ID로 공동구매 진행 중인 정보 전체 반환하는 API**
    @GetMapping("/{productId}/congdongIng")
    public ResponseEntity<List<CongDongIng>> getCongdongIngByProductId(@PathVariable Long productId) {
        log.info("공동구매 진행 정보 요청 - productId: {}", productId);

        // 서비스에서 congdongIng 목록 조회
        List<CongDongIng> congdongIngList = congdongService.getCongdongIngByProductId(productId);

        if (congdongIngList == null || congdongIngList.isEmpty()) {
            log.warn("해당 상품의 공동구매 정보 없음 - productId: {}", productId);
            return ResponseEntity.ok(Collections.emptyList()); // 빈 리스트 반환
        }

        log.info("반환된 공동구매 목록: {}", congdongIngList);
        return ResponseEntity.ok(congdongIngList);
    }


    @GetMapping("/congdong/history")
    public ResponseEntity<List<CongDongIng>> getMyCongs(
            @RequestHeader("Authorization") String token,  // ✅ 토큰 받기
            @RequestParam("memberNo") Long memberNo) {  // ✅ memberNo 받기

        log.info("📢 [Seller API 요청] 공동구매 참여 목록 조회 - Authorization 헤더 포함");
        log.info("📢 [Seller API 요청] token: {}", token);
        log.info("📢 [Seller API 요청] memberNo: {}", memberNo);

        // ✅ 서비스 호출 시 `memberNo`만 넘김
        List<CongDongIng> groupPurchases = congdongService.getCongs(memberNo);

        log.info("📢 [Seller API 응답] 조회된 공동구매 개수: {}", groupPurchases.size());

        return ResponseEntity.ok(groupPurchases); // ✅ 엔티티 리스트 그대로 반환
    }

}
