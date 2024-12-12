package com.example.spring.bzsellerservice.config;


import com.example.spring.bzsellerservice.config.security.CustomAuthenticationFailureHandler;
import com.example.spring.bzsellerservice.config.security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler
    ) throws Exception {
        http
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/static/uploads/**"
                                ).permitAll()
                                .requestMatchers(
                                        "/user/login", "/user/join", "/join", "/home/**",
                                        "/user/product/list", "/user/product/detail/**", // <-- 여기 수정
                                        "/user/product/list", "/user/product/api/list",
                                        "/user/review_list"
                                ).permitAll()
                                .requestMatchers(
                                        "/user/cart/add", "/user/buy"
                                ).authenticated()
                                .requestMatchers(
                                        "/seller/product/list", "/seller/product/upload", "/seller/**"
                                ).hasRole("SELLER")
                                .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/user/login")
                                .loginProcessingUrl("/user/login")
                                .successHandler(successHandler)
                                .failureHandler(failureHandler)
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/user/login")
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
