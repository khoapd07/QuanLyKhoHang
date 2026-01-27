package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.entity.DonVi;
import com.poly.quanlykhohang.service.DonViService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/don-vi")
public class DonViController {

    @Autowired
    private DonViService donViService;

    // 1. Lấy danh sách (Ai cũng xem được)
    @GetMapping
    public ResponseEntity<List<DonVi>> getAll() {
        return ResponseEntity.ok(donViService.getAllDonVi());
    }

    // API phụ: Lấy riêng NCC (để load vào combobox Nhập kho)
    @GetMapping("/nha-cung-cap")
    public ResponseEntity<List<DonVi>> getNhaCungCap() {
        return ResponseEntity.ok(donViService.getNhaCungCap());
    }

    // ============================================================
    // CÁC HÀM DƯỚI ĐÂY CHỈ DÀNH CHO ADMIN
    // ============================================================

    // 2. Thêm mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DonVi donVi) {
        try {
            // Kiểm tra trùng mã
            if (donViService.getDonViById(donVi.getMaDonVi()) != null) {
                return ResponseEntity.badRequest().body("Mã đơn vị đã tồn tại!");
            }
            return ResponseEntity.ok(donViService.saveDonVi(donVi));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 3. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody DonVi donViMoi) {
        DonVi existing = donViService.getDonViById(id); // Giờ hàm này đã có trong Service
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setTenDonVi(donViMoi.getTenDonVi());
        existing.setSoDienThoai(donViMoi.getSoDienThoai());
        existing.setEmail(donViMoi.getEmail());
        existing.setDiaChi(donViMoi.getDiaChi());
        existing.setLoaiDonVi(donViMoi.getLoaiDonVi());

        return ResponseEntity.ok(donViService.saveDonVi(existing));
    }

    // 4. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (donViService.getDonViById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            donViService.deleteDonVi(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa (Đang có dữ liệu ràng buộc)!");
        }
    }
}