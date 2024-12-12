package com.example.spring.bzsellerservice.dto.congdong;

import com.example.spring.bzsellerservice.entity.Congdong;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class CongdongDTO {
    private Long id;
    private Long productId;
    private String condition; // 변환된 {?:?} 형태

    public static CongdongDTO fromEntity(Congdong congdong) {
        Map<Integer, Integer> conditionMap = parseCondition(congdong.getConditon());
        return CongdongDTO.builder()
                .id(congdong.getId())
                .productId(congdong.getProduct().getId())
                .condition(formatCondition(conditionMap))
                .build();
    }

    private static Map<Integer, Integer> parseCondition(String condition) {
        Map<Integer, Integer> conditionMap = new HashMap<>();

        if (condition == null || condition.isEmpty()) {
            return conditionMap; // 비어있을 경우 빈 Map 반환
        }

        try {
            // {3:10,5:15} 형식 파싱
            String cleanedCondition = condition.replaceAll("[{}]", ""); // 중괄호 제거
            String[] pairs = cleanedCondition.split(","); // 콤마로 구분

            for (String pair : pairs) {
                String[] keyValue = pair.split(":"); // 콜론으로 구분
                if (keyValue.length == 2) {
                    Integer key = Integer.parseInt(keyValue[0].trim()); // 모집인원
                    Integer value = Integer.parseInt(keyValue[1].trim()); // 할인율
                    conditionMap.put(key, value);
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 로깅 (디버깅 용도)
            System.err.println("Error parsing condition: " + condition);
            e.printStackTrace();
        }

        return conditionMap;
    }

    private static String formatCondition(Map<Integer, Integer> conditionMap) {
        return conditionMap.entrySet().stream()
                .map(entry -> "{" + entry.getKey() + ":" + entry.getValue() + "}")
                .collect(Collectors.joining(", "));
    }
}