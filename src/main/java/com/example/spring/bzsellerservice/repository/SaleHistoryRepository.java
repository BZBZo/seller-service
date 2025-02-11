package com.example.spring.bzsellerservice.repository;

import com.example.spring.bzsellerservice.dto.SaleHistoryDTO;
import com.example.spring.bzsellerservice.entity.SaleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleHistoryRepository extends JpaRepository<SaleHistory, Long> {

    List<SaleHistory> findAllBySellerIdOrderByApprovedAtDesc(Long sellerId);
}
