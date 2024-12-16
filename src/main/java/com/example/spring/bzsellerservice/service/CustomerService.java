package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.dto.customer.CustomerDeleteRequestDTO;
import com.example.spring.bzsellerservice.dto.customer.CustomerUpdateRequestDTO;
import com.example.spring.bzsellerservice.dto.customer.SignUpRequestDTO;
import com.example.spring.bzsellerservice.dto.product.ProdReadResponseDTO;
import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.repository.CustomerRepository;
import com.example.spring.bzsellerservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public void save(SignUpRequestDTO dto) {
        Customer customer = dto.toCustomer(bCryptPasswordEncoder);
        // role이 ROLE_SELLER인지 ROLE_CUSTOMER인지 설정
        customer.setRole("ROLE_SELLER");  // 권한 설정
        customerRepository.save(customer);
    }

    public Customer findBySession(HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");
        return customerRepository.findByLoginId(loginId);
    }

    public ProdReadResponseDTO findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id))
                .toProdReadResponseDTO();
    }

    public void updateUser(Long id, CustomerUpdateRequestDTO dto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
        existingCustomer.setName(dto.getName());
        existingCustomer.setEmail(dto.getEmail());
        existingCustomer.setPhone(dto.getPhone());
        existingCustomer.setAddress(dto.getAddress());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingCustomer.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        }

        customerRepository.save(existingCustomer);
    }

    public void deleteUser(Long id, CustomerDeleteRequestDTO dto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        if (bCryptPasswordEncoder.matches(dto.getPassword(), existingCustomer.getPassword())) {
            customerRepository.delete(existingCustomer);
        } else {
            throw new RuntimeException("Incorrect password.");
        }
    }

}