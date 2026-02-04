package com.poly.quanlykhohang.security;

import com.poly.quanlykhohang.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
                .csrf(csrf -> csrf.disable()) // Tắt CSRF vì dùng JWT
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Kích hoạt CORS

                .authorizeHttpRequests(auth -> auth
                        // ======================================================
                        // NHÓM 1: PUBLIC (Không cần đăng nhập)
                        // ======================================================
                        .requestMatchers("/api/auth/**", "/api/upload/**", "/error").permitAll()

                        // ======================================================
                        // NHÓM 2: QUẢN TRỊ HỆ THỐNG (Chỉ ADMIN)
                        // ======================================================
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ======================================================
                        // NHÓM 3: DANH MỤC (Sản phẩm, Kho, Đơn vị...)
                        // -> GET (Xem): Ai đăng nhập cũng xem được (để Staff chọn khi nhập liệu)
                        // -> CUD (Thêm/Sửa/Xóa): Chỉ ADMIN được phép
                        // ======================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/san-pham/**",
                                "/api/don-vi/**",
                                "/api/kho/**",
                                "/api/hang-san-xuat/**",
                                "/api/loai-san-pham/**",
                                "/api/danh-muc-may/**",
                                "/api/may-in/**",
                                "/api/dashboard"
                        ).authenticated()

                        .requestMatchers(
                                "/api/san-pham/**",
                                "/api/don-vi/**",
                                "/api/kho/**",
                                "/api/hang-san-xuat/**",
                                "/api/loai-san-pham/**",
                                "/api/danh-muc-may/**",
                                "/api/dashboard"
                        ).authenticated()

                        // ======================================================
                        // NHÓM 4: NGHIỆP VỤ & BÁO CÁO (ADMIN & STAFF)
                        // ======================================================
                        // Nhập kho, Xuất kho, Chuyển kho, Thống kê, Tra cứu tồn
                        // Logic phân quyền dữ liệu (Staff chỉ thấy kho mình) xử lý ở Service
                        .requestMatchers(
                                "/api/kho/nhap/**",
                                "/api/kho/xuat/**",
                                "/api/kho/chuyen/**",
                                "/api/kho/may-in/**",
                                "/api/thong-ke/**"
                        ).authenticated()

                        // ======================================================
                        // CÁC REQUEST CÒN LẠI
                        // ======================================================
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS để Frontend (Vite) gọi được API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // URL Frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}