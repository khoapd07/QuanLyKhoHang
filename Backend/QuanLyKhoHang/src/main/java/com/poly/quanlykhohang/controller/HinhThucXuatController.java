package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.HinhThucXuatDAO;
import com.poly.quanlykhohang.dao.PhieuXuatDAO;
import com.poly.quanlykhohang.entity.HinhThucXuat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/hinh-thuc-xuat")
//@CrossOrigin("*")
public class HinhThucXuatController {

    @Autowired
    private HinhThucXuatDAO hinhThucXuatDAO;

    @Autowired
    private PhieuXuatDAO phieuXuatDAO;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(hinhThucXuatDAO.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody HinhThucXuat hinhThucXuat) {
        if (hinhThucXuatDAO.existsByTenHT((hinhThucXuat.getTenHT()))) {
            return ResponseEntity.badRequest().body("Tên hình thức xuất này đã tồn tại!");
        }
        return ResponseEntity.ok(hinhThucXuatDAO.save(hinhThucXuat));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody HinhThucXuat hinhThucXuat) {
        Optional<HinhThucXuat> existing = hinhThucXuatDAO.findById(id);
        if (!existing.isPresent()) return ResponseEntity.notFound().build();

        HinhThucXuat htxDuplicate = hinhThucXuatDAO.findByTenHT((hinhThucXuat.getTenHT()));
        if (htxDuplicate != null && !htxDuplicate.getMaHT().equals(id)) {
            return ResponseEntity.badRequest().body("Tên hình thức xuất này đã tồn tại!");
        }

        HinhThucXuat updateHtx = existing.get();
        updateHtx.setTenHT(hinhThucXuat.getTenHT());
        return ResponseEntity.ok(hinhThucXuatDAO.save(updateHtx));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (phieuXuatDAO.existsByHinhThucXuat_MaHT((id))) {
            return ResponseEntity.badRequest().body("Không thể xóa do hình thức này đã phát sinh phiếu xuất!");
        }
        hinhThucXuatDAO.deleteById(id);
        return ResponseEntity.ok("Xóa thành công!");
    }
}