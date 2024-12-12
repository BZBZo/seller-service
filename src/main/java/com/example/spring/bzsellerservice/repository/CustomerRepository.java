package com.example.spring.bzsellerservice.repository;



import com.example.spring.bzsellerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByLoginId(String username);

}
