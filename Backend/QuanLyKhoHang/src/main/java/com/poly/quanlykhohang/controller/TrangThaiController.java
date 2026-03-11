package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.TrangThaiDAO;
import com.poly.quanlykhohang.entity.TrangThai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trang-thai")
//@CrossOrigin(origins = "*")
public class TrangThaiController {

    @Autowired
    private TrangThaiDAO trangThaiDAO;

    @GetMapping
    public ResponseEntity<List<TrangThai>> getAll() {
        return ResponseEntity.ok(trangThaiDAO.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Integer id) {
        return trangThaiDAO.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Thêm mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TrangThai trangThai) {
        // ĐÃ BỎ KIỂM TRA TRÙNG MÃ (Vì DB tự sinh mã mới)

        // Chỉ cần kiểm tra trùng Tên
        if (trangThaiDAO.existsByTenTrangThaiIgnoreCase(trangThai.getTenTrangThai().trim())) {
            return ResponseEntity.badRequest().body("Tên trạng thái này đã tồn tại!");
        }

        return ResponseEntity.ok(trangThaiDAO.save(trangThai));
    }

    // 4. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TrangThai ttMoi) {
        return trangThaiDAO.findById(id).map(ttCu -> {
            String tenMoi = ttMoi.getTenTrangThai().trim();

            // Nếu đổi tên khác tên cũ -> Cần check trùng với các trạng thái khác trong DB
            if (!ttCu.getTenTrangThai().equalsIgnoreCase(tenMoi)) {
                if (trangThaiDAO.existsByTenTrangThaiIgnoreCase(tenMoi)) {
                    throw new IllegalArgumentException("Tên trạng thái này đã tồn tại!");
                }
            }

            ttCu.setTenTrangThai(tenMoi);
            return ResponseEntity.ok(trangThaiDAO.save(ttCu));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!trangThaiDAO.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            trangThaiDAO.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa trạng thái này vì đang được sử dụng ở Máy In!");
        }
    }
}