package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.KhoDAO;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.service.KhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-nhanh") // Đổi đường dẫn khác với /api/kho để tránh xung đột
//@CrossOrigin(origins = "*")
public class ChiNhanhKhoController {

    @Autowired
    private KhoService khoService;

    @Autowired
    private KhoDAO khoDAO;

    // 1. Lấy danh sách tất cả chi nhánh
    @GetMapping
    public ResponseEntity<Page<Kho>> getAllChiNhanh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Sắp xếp theo mã kho tăng dần
        Pageable pageable = PageRequest.of(page, size, Sort.by("maKho").ascending());
        return ResponseEntity.ok(khoDAO.findAll(pageable));
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