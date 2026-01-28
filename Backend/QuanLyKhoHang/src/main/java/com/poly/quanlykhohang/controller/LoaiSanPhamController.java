package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.entity.LoaiSanPham;
import com.poly.quanlykhohang.service.LoaiSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-san-pham")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend gọi API
public class LoaiSanPhamController {

    @Autowired
    private LoaiSanPhamService service;

    // 1. Lấy danh sách
    @GetMapping
    public List<LoaiSanPham> getAll() {
        return service.getAll();
    }

    // 2. Lấy chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<LoaiSanPham> getById(@PathVariable Integer id) {
        LoaiSanPham item = service.getById(id);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    // 3. Thêm mới
    @PostMapping
    public LoaiSanPham create(@RequestBody LoaiSanPham loaiSP) {
        return service.create(loaiSP);
    }

    // 4. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<LoaiSanPham> update(@PathVariable Integer id, @RequestBody LoaiSanPham loaiSP) {
        LoaiSanPham updated = service.update(id, loaiSP);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // 5. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}