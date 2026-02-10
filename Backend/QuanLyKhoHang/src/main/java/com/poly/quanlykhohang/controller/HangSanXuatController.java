package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.HangSanXuatDAO;
import com.poly.quanlykhohang.entity.HangSanXuat;
import com.poly.quanlykhohang.service.HangSanXuatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hang-san-xuat")
//@CrossOrigin(origins = "*")
public class HangSanXuatController {

    @Autowired
    private HangSanXuatService service;

    @Autowired
    private HangSanXuatDAO dao;

    // 1. Lấy danh sách
    @GetMapping
    public ResponseEntity<Page<HangSanXuat>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size // Mặc định 20 phần tử/trang
    ) {
        // Sắp xếp theo ID giảm dần (Mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("maHang").descending());
        return ResponseEntity.ok(dao.findAll(pageable));
    }

    // 2. Lấy chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<HangSanXuat> getById(@PathVariable Integer id) {
        HangSanXuat hang = service.getHangById(id);
        return hang != null ? ResponseEntity.ok(hang) : ResponseEntity.notFound().build();
    }

    // 3. Thêm mới
    @PostMapping
    public HangSanXuat create(@RequestBody HangSanXuat hang) {
        return service.createHang(hang);
    }

    // 4. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<HangSanXuat> update(@PathVariable Integer id, @RequestBody HangSanXuat hang) {
        HangSanXuat updated = service.updateHang(id, hang);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    // 5. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteHang(id);
        return ResponseEntity.noContent().build();
    }
}