package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import com.poly.quanlykhohang.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tai-khoan")
public class TaiKhoanController {

    @Autowired
    private TaiKhoanDAO taiKhoanDAO;

    @Autowired
    private AuthService authService; // Dùng để gọi hàm register (có mã hóa pass)

    // 1. Lấy danh sách tất cả nhân viên
    @GetMapping
    public ResponseEntity<List<TaiKhoan>> getAll() {
        return ResponseEntity.ok(taiKhoanDAO.findAll());
    }

    // 2. Tạo tài khoản mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaiKhoan taiKhoan) {
        try {
            // Kiểm tra trùng username
            if (taiKhoanDAO.findByTenTaiKhoan(taiKhoan.getTenTaiKhoan()).isPresent()) {
                return ResponseEntity.badRequest().body("Tên tài khoản đã tồn tại!");
            }

            // Nếu không chọn vai trò, mặc định là Staff (2)
            if (taiKhoan.getMaVaitro() == null) {
                taiKhoan.setMaVaitro(2);
            }

            // Gọi AuthService để lưu (nó sẽ tự mã hóa mật khẩu)
            TaiKhoan newTK = authService.register(taiKhoan);
            return ResponseEntity.ok(newTK);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo tài khoản: " + e.getMessage());
        }
    }

    // 3. Cập nhật thông tin (Đổi kho, đổi vai trò...)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TaiKhoan taiKhoanDetails) {
        return taiKhoanDAO.findById(id).map(tk -> {
            tk.setMaKho(taiKhoanDetails.getMaKho());
            tk.setMaVaitro(taiKhoanDetails.getMaVaitro());
            // Không cho đổi username & password ở API này cho an toàn
            TaiKhoan updated = taiKhoanDAO.save(tk);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. Xóa tài khoản (Hoặc khóa)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!taiKhoanDAO.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taiKhoanDAO.deleteById(id);
        return ResponseEntity.ok().build();
    }
}