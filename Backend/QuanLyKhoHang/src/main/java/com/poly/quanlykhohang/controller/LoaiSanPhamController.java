package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.LoaiSanPhamDAO;
import com.poly.quanlykhohang.entity.LoaiSanPham;
import com.poly.quanlykhohang.service.LoaiSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-san-pham")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend gọi API
public class LoaiSanPhamController {

    @Autowired
    private LoaiSanPhamService service;

    @Autowired
    private LoaiSanPhamDAO dao;

    // 1. Lấy danh sách
    @GetMapping
    public ResponseEntity<Page<LoaiSanPham>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size // Mặc định 20
    ) {
        // Sắp xếp theo MaLoai mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("maLoai").descending());
        return ResponseEntity.ok(dao.findAll(pageable));
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