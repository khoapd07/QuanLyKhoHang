package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.service.KhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-nhanh") // Đổi đường dẫn khác với /api/kho để tránh xung đột
@CrossOrigin(origins = "http://localhost:5173")
public class ChiNhanhKhoController {

    @Autowired
    private KhoService khoService;

    // 1. Lấy danh sách tất cả chi nhánh
    @GetMapping
    public List<Kho> getAllChiNhanh() {
        return khoService.layDanhSachKho();
    }

    // 2. Lấy chi tiết 1 chi nhánh theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Kho> getChiNhanhById(@PathVariable Integer id) {
        Kho kho = khoService.getKhoById(id);
        return kho != null ? ResponseEntity.ok(kho) : ResponseEntity.notFound().build();
    }

    // 3. Thêm mới chi nhánh kho
    @PostMapping
    public Kho createChiNhanh(@RequestBody Kho kho) {
        // Có thể thêm logic validate dữ liệu ở đây nếu cần
        return khoService.createKho(kho);
    }

    // 4. Cập nhật thông tin chi nhánh
    @PutMapping("/{id}")
    public ResponseEntity<Kho> updateChiNhanh(@PathVariable Integer id, @RequestBody Kho khoDetails) {
        Kho updatedKho = khoService.updateKho(id, khoDetails);
        return updatedKho != null ? ResponseEntity.ok(updatedKho) : ResponseEntity.notFound().build();
    }

    // 5. Xóa chi nhánh
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChiNhanh(@PathVariable Integer id) {
        khoService.deleteKho(id);
        return ResponseEntity.noContent().build();
    }
}