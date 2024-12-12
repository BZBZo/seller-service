package com.example.spring.bzsellerservice.service;

import com.example.spring.bzsellerservice.config.security.CustomUserDetails;
import com.example.spring.bzsellerservice.entity.Customer;
import com.example.spring.bzsellerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Customer customer = customerRepository.findByLoginId(username);
        if (customer == null) {
            throw new UsernameNotFoundException(username + " not found");
        }

        return CustomUserDetails.builder()
                .customer(customer)
                .build();
    }
}
