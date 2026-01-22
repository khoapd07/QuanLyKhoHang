package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.PhieuNhapDTO;
import com.poly.quanlykhohang.dto.PhieuXuatDTO;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.service.GiaoDichKhoService;
import com.poly.quanlykhohang.service.KhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kho")
// @CrossOrigin(origins = "*") // Đã cấu hình bên SecurityConfig rồi thì bỏ dòng này
public class KhoController {

    @Autowired
    private GiaoDichKhoService giaoDichService;

    @Autowired
    private KhoService khoService;

    // --- 1. API LẤY DANH SÁCH TÓM TẮT (Dùng cho bảng danh sách) ---
    // GET: http://localhost:8080/api/kho/nhap
    @GetMapping("/nhap")
    public ResponseEntity<?> layDanhSachPhieuNhap() {
        // API này sẽ trả về list phiếu nhưng list chi tiết sản phẩm sẽ là null
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuNhapTomTat());
    }

    // --- 2. API LẤY CHI TIẾT CỤ THỂ (Dùng khi bấm vào xem) ---
    // GET: http://localhost:8080/api/kho/nhap/{soPhieu}
    @GetMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuNhap(@PathVariable String soPhieu) {
        try {
            // API này trả về đầy đủ thông tin sản phẩm, serial của phiếu đó
            return ResponseEntity.ok(giaoDichService.layPhieuNhapChiTiet(soPhieu));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- CÁC API POST/PUT/DELETE GIỮ NGUYÊN ---

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

    @PutMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> capNhatPhieu(@PathVariable String soPhieu, @RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.capNhatPhieuNhap(soPhieu, dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    @DeleteMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> xoaPhieuNhap(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuNhap(soPhieu);
            return ResponseEntity.ok("Đã xóa thành công phiếu nhập: " + soPhieu);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi xóa phiếu: " + e.getMessage());
        }
    }

    // --- API QUẢN LÝ KHO (CRUD) ---
    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }
}