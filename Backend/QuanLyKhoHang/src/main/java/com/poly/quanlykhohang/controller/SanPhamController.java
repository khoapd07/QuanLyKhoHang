package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.SanPhamDAO;
import com.poly.quanlykhohang.entity.SanPham;
import com.poly.quanlykhohang.service.SanPhamService; // Nếu bạn đã có Service thì dùng, không thì dùng DAO tạm
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamDAO sanPhamDAO; // Hoặc SanPhamService

    // 1. Lấy danh sách sản phẩm (Ai cũng dùng được: Để hiển thị lên combobox nhập/xuất)
    @GetMapping
    public ResponseEntity<Page<SanPham>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Sắp xếp theo MaSP (hoặc TenSP tùy bạn)
        Pageable pageable = PageRequest.of(page, size, Sort.by("maSP").ascending());
        return ResponseEntity.ok(sanPhamDAO.findAll(pageable));
    }

    // 2. Lấy chi tiết 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        return sanPhamDAO.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // CÁC HÀM DƯỚI ĐÂY SẼ BỊ CHẶN NẾU KHÔNG PHẢI ADMIN (Cấu hình ở SecurityConfig)
    // ============================================================

    // 3. Thêm sản phẩm mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SanPham sanPham) {
        if (sanPhamDAO.existsById(sanPham.getMaSP())) {
            return ResponseEntity.badRequest().body("Mã sản phẩm đã tồn tại!");
        }
        return ResponseEntity.ok(sanPhamDAO.save(sanPham));
    }

    // 4. Cập nhật sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody SanPham spMoi) {
        return sanPhamDAO.findById(id).map(spCu -> {
            spCu.setTenSP(spMoi.getTenSP());
            spCu.setDonViTinh(spMoi.getDonViTinh());
            spCu.setMoTa(spMoi.getMoTa());
            spCu.setHangSanXuat(spMoi.getHangSanXuat()); // Cập nhật hãng
            // Không cập nhật số lượng ở đây (Số lượng do nhập/xuất quyết định)
            return ResponseEntity.ok(sanPhamDAO.save(spCu));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!sanPhamDAO.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            sanPhamDAO.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa sản phẩm này (đã có phát sinh giao dịch nhập/xuất)!");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<SanPham>> getAllList() {
        // Lấy tất cả, sắp xếp theo tên cho dễ tìm
        return ResponseEntity.ok(sanPhamDAO.findAll(Sort.by("tenSP").ascending()));
    }
}