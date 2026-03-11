package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.SanPhamDAO;
import com.poly.quanlykhohang.entity.SanPham;
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
    private SanPhamDAO sanPhamDAO;

    // 1. Lấy danh sách sản phẩm (ĐÃ THÊM LOGIC LỌC)
    @GetMapping
    public ResponseEntity<Page<SanPham>> getAll(
            @RequestParam(required = false) Integer maHang,
            @RequestParam(required = false) Integer maLoai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("maSP").ascending());

        // Gọi hàm filter, nếu maHang hoặc maLoai là null thì DB sẽ tự động lấy tất cả
        Page<SanPham> result = sanPhamDAO.filterSanPham(maHang, maLoai, pageable);

        return ResponseEntity.ok(result);
    }

    // 2. Lấy chi tiết 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        return sanPhamDAO.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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
            spCu.setHangSanXuat(spMoi.getHangSanXuat());
            spCu.setLoaiSanPham(spMoi.getLoaiSanPham()); // Đừng quên cập nhật cả Loại Sản Phẩm nhé
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
        return ResponseEntity.ok(sanPhamDAO.findAll(Sort.by("tenSP").ascending()));
    }
}