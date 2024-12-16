package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.config.security.CustomUserDetails;
import com.example.spring.bzsellerservice.dto.UrlResponseDTO;
import com.example.spring.bzsellerservice.dto.customer.CustomerDeleteRequestDTO;
import com.example.spring.bzsellerservice.dto.customer.CustomerUpdateRequestDTO;
import com.example.spring.bzsellerservice.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageApiController {

    private final CustomerService customerService;

    @PutMapping("/edit/{id}")
    public ResponseEntity<UrlResponseDTO> update(
            @PathVariable Long id,
            @RequestBody CustomerUpdateRequestDTO dto) {

        // 서비스 메서드를 호출해 업데이트 실행
        customerService.updateUser(id, dto);

        return ResponseEntity.ok(
                UrlResponseDTO.builder()
                        .url("/mypage")
                        .build()
        );
    }

    @DeleteMapping("/quitout/{id}")
    public ResponseEntity<UrlResponseDTO> delete(
            @PathVariable Long id,
            @RequestBody CustomerDeleteRequestDTO dto) {

        customerService.deleteUser(id, dto);

        return ResponseEntity.ok(
                UrlResponseDTO.builder()
                        .url("/home")
                        .build()
        );
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/home";
    }

    private Long getCurrentCustomerId() {
        // SecurityContext에서 현재 인증 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자가 있는지 확인합니다.
        if (authentication != null && authentication.isAuthenticated()) {
            // 사용자 정보를 가져옵니다. (예: UserDetails 객체에서 ID를 가져오는 방식)
            // 여기서는 UserDetails 인터페이스를 구현한 CustomUserDetails를 가정합니다.
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 사용자 ID를 반환합니다.
            // Customer 객체에서 ID를 가져옴
            return userDetails.getCustomer().getId();
        }

        // 인증되지 않은 경우, 예외를 던지거나 null을 반환합니다.
        throw new RuntimeException("사용자가 인증되지 않았습니다");
    }

}