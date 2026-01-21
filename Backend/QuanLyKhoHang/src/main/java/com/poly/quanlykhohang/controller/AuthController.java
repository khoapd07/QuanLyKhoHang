package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.LoginRequest;
import com.poly.quanlykhohang.entity.TaiKhoan;
import com.poly.quanlykhohang.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin("*") // Quan trọng: Cho phép Frontend gọi không bị chặn
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            TaiKhoan user = authService.login(request.getTenTaiKhoan(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}