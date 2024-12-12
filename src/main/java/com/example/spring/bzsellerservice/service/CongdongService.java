package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.repository.CongdongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CongdongService {

    private final CongdongRepository congdongRepository;



}
