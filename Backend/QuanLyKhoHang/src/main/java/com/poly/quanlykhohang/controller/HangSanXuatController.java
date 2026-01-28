package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.entity.HangSanXuat;
import com.poly.quanlykhohang.service.HangSanXuatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hang-san-xuat")
//@CrossOrigin(origins = "http://localhost:5173") // Cho phép Frontend truy cập
public class HangSanXuatController {

    @Autowired
    private HangSanXuatService service;

    // 1. Lấy danh sách
    @GetMapping
    public List<HangSanXuat> getAll() {
        return service.getAllHang();
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