package com.example.spring.bzsellerservice.controller;

import com.example.spring.bzsellerservice.service.ProductttService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeViewController {

    private final ProductttService productttService;

    @GetMapping
    public String home() {
        return "main";
    }

}
