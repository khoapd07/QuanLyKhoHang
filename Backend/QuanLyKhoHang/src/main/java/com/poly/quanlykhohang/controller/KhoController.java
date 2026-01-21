package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.PhieuNhapDTO;
import com.poly.quanlykhohang.dto.PhieuXuatDTO;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.service.GiaoDichKhoService; // Service nhập xuất
import com.poly.quanlykhohang.service.KhoService;         // Service quản lý kho (Interface)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kho")
@CrossOrigin(origins = "*")
public class KhoController {

    // 1. Service xử lý Nhập/Xuất (File mới đổi tên)
    @Autowired
    private GiaoDichKhoService giaoDichService;

    // 2. Service quản lý danh sách Kho (Interface & Impl bạn gửi)
    @Autowired
    private KhoService khoService;

    // --- API NGHIỆP VỤ NHẬP/XUẤT ---
    @PostMapping("/nhap")
    public ResponseEntity<?> nhapKho(@RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.nhapKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/xuat")
    public ResponseEntity<?> xuatKho(@RequestBody PhieuXuatDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.xuatKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // --- API QUẢN LÝ KHO (CRUD) ---
    // Ví dụ thêm API lấy danh sách kho
    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }
}