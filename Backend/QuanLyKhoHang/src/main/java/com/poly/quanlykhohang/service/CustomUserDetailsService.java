package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private TaiKhoanDAO taiKhoanDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong DB
        TaiKhoan tk = taiKhoanDAO.findByTenTaiKhoan(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Chuyển đổi MaVaitro (1, 2) thành Role (ADMIN, STAFF)
        String role = "ROLE_USER"; // Mặc định
        if (tk.getMaVaitro() == 1) role = "ROLE_ADMIN";
        if (tk.getMaVaitro() == 2) role = "ROLE_STAFF";

        // 3. Trả về đối tượng User của Spring Security
        return new User(
                tk.getTenTaiKhoan(),
                tk.getPassword(), // Pass này lát nữa phải là mã hóa BCrypt
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}