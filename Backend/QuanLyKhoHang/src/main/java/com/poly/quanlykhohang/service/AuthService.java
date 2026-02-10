package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import com.poly.quanlykhohang.security.JwtUtils;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    @Autowired
    private TaiKhoanDAO taiKhoanDAO;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> login(String username, String password) {
        // 1. Xác thực qua Spring Security (Tự động check pass mã hóa)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. Nếu OK -> Tìm user trong DB để lấy thêm thông tin (MaKho...)
        TaiKhoan user = taiKhoanDAO.findByTenTaiKhoan(username).orElseThrow();

        // 3. Tạo Token
        String token = jwtUtils.generateToken(username);

        // 4. Trả về kết quả (Token + Info)
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("maTaiKhoan", user.getMaTaiKhoan());
        response.put("hoTen", user.getTenTaiKhoan());
        response.put("maKho", user.getMaKho());
        response.put("role", user.getMaVaitro() == 1 ? "ADMIN" : "STAFF");

        return response;
    }

    // Hàm đăng ký (dùng để tạo user mới với pass đã mã hóa)
    public TaiKhoan register(TaiKhoan tk) {
        tk.setPassword(passwordEncoder.encode(tk.getPassword())); // Mã hóa pass trước khi lưu
        return taiKhoanDAO.save(tk);
    }
}