package com.poly.quanlykhohang.security;

import com.poly.quanlykhohang.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .cors(cors -> {}) // Kích hoạt CORS config (đã làm ở WebConfig)
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**", "/api/upload/**").permitAll() // Cho phép Login & Upload không cần token
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Chỉ Admin mới vào được /api/admin
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/san-pham/**", "/api/don-vi/**").authenticated()
                        // - Các thao tác còn lại (Thêm/Sửa/Xóa) bắt buộc là ADMIN
                        .requestMatchers("/api/san-pham/**", "/api/don-vi/**").hasRole("ADMIN")

                        .requestMatchers("/api/thong-ke/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/kho/**").hasRole("ADMIN")
//                       .requestMatchers("/**").permitAll()
                                .requestMatchers("/api/thong-ke/**").authenticated()
                        .anyRequest().authenticated() // Còn lại phải đăng nhập hết
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Không dùng Session
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Dùng BCrypt để so khớp pass
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}