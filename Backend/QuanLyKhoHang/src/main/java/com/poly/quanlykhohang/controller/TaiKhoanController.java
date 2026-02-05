package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import com.poly.quanlykhohang.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;          // Import Page
import org.springframework.data.domain.PageRequest;   // Import PageRequest
import org.springframework.data.domain.Pageable;      // Import Pageable
import org.springframework.data.domain.Sort;          // Import Sort
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tai-khoan")
public class TaiKhoanController {

    @Autowired
    private TaiKhoanDAO taiKhoanDAO;

    @Autowired
    private AuthService authService;

    // 1. Lấy danh sách CÓ PHÂN TRANG (Sửa lại hàm này)
    @GetMapping
    public ResponseEntity<Page<TaiKhoan>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Sắp xếp theo ID giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by("maTaiKhoan").descending());
        return ResponseEntity.ok(taiKhoanDAO.findAll(pageable));
    }

    // [MỚI] Hàm lấy toàn bộ list (nếu cần dùng cho dropdown ở đâu đó)
    @GetMapping("/list")
    public ResponseEntity<List<TaiKhoan>> getAllList() {
        return ResponseEntity.ok(taiKhoanDAO.findAll());
    }

    // ... Các hàm create, update, delete giữ nguyên ...
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaiKhoan taiKhoan) {
        try {
            if (taiKhoanDAO.findByTenTaiKhoan(taiKhoan.getTenTaiKhoan()).isPresent()) {
                return ResponseEntity.badRequest().body("Tên tài khoản đã tồn tại!");
            }
            if (taiKhoan.getMaVaitro() == null) {
                taiKhoan.setMaVaitro(2);
            }
            TaiKhoan newTK = authService.register(taiKhoan);
            return ResponseEntity.ok(newTK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo tài khoản: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TaiKhoan taiKhoanDetails) {
        return taiKhoanDAO.findById(id).map(tk -> {
            tk.setMaKho(taiKhoanDetails.getMaKho());
            tk.setMaVaitro(taiKhoanDetails.getMaVaitro());
            TaiKhoan updated = taiKhoanDAO.save(tk);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!taiKhoanDAO.existsById(id)) return ResponseEntity.notFound().build();
        taiKhoanDAO.deleteById(id);
        return ResponseEntity.ok().build();
    }
}