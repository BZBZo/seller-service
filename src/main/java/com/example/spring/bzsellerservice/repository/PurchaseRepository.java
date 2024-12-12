package com.example.spring.bzsellerservice.repository;

import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByCustomer(Customer customer);
}
