package com.goorm.jpa_basic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (람다식 변경)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/activate", "/login").permitAll() // 로그인 페이지 및 회원가입 관련 요청 허용
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/login")  // 사용자 정의 로그인 페이지 URL 설정
                        .permitAll()  // 로그인 페이지는 인증 없이 접근 가능
                )
                .httpBasic(httpBasic -> httpBasic.disable()); // HTTP 기본 인증 비활성화

        return http.build();
    }
}


