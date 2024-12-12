package com.example.spring.bzsellerservice.dto.customer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class CustomerDeleteRequestDTO {

    private String loginId;
    private String password;

}
