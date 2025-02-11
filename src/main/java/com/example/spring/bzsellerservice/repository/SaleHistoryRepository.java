package com.example.spring.bzsellerservice.repository;

import com.example.spring.bzsellerservice.entity.SaleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleHistoryRepository extends JpaRepository<SaleHistory, Long> {
}
