package com.example.spring.bzsellerservice.repository;

import com.example.spring.bzsellerservice.entity.CongDongIng;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CongdongIngRepository extends JpaRepository<CongDongIng, Long> {
}