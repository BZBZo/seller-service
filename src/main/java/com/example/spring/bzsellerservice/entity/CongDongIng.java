package com.example.spring.bzsellerservice.entity;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "congdonging")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class CongDongIng {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // productId: 단순하게 숫자 ID만 저장
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // condition을 JSON String으로 저장
    @Lob
    @Column(name = "`condition`", columnDefinition = "TEXT")
    private String condition; // JSON 형태의 문자열

    // congs(people)를 JSON String으로 저장
    @Lob
    @Column(columnDefinition = "TEXT")
    private String congs; // JSON 형태의 문자열

    // startAt: 생성 시 현재 시간 설정
    private LocalDateTime startAt = LocalDateTime.now();

    @Builder
    public CongDongIng(Long productId, String condition, String congs) {
        this.productId = productId;
        this.condition = condition;
        this.congs = congs;
        this.startAt = LocalDateTime.now();
    }

    // JSON 변환 메서드 (ObjectMapper 활용)
    public static String toJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }
}