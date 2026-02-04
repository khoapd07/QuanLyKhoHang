package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.KhoDAO; // Thêm import KhoDAO
import com.poly.quanlykhohang.entity.MayIn;
import com.poly.quanlykhohang.entity.Kho; // Thêm import Entity Kho
import com.poly.quanlykhohang.service.MayInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/may-in")
@CrossOrigin(origins = "http://localhost:5173")
public class MayInController {

    @Autowired
    private MayInDAO mayInDAO;

    @Autowired
    private KhoDAO khoDAO; // Inject thêm KhoDAO để cập nhật vị trí kho nếu cần

    @Autowired
    private MayInService mayInService;

    // 1. Lấy danh sách (Giữ nguyên)
    @GetMapping
    public ResponseEntity<Page<MayIn>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer maKho
    ) {
        // Tạo đối tượng Pageable (trang, kích thước, sắp xếp theo ngày tạo mới nhất)
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());

        // Trả về Page<MayIn> thay vì List<MayIn>
        return ResponseEntity.ok(mayInService.layDanhSachMayIn(page, size, maKho));
    }

    // 2. Cập nhật thông tin máy (API MỚI)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody MayIn mayMoi) {
        return mayInDAO.findById(id).map(mayCu -> {
            // Cập nhật Số Seri (Quan trọng)
            mayCu.setSoSeri(mayMoi.getSoSeri());

            // Cập nhật Trạng thái
            mayCu.setTrangThai(mayMoi.getTrangThai());

            // Cập nhật Kho (Nếu có gửi MaKho lên)
            if (mayMoi.getKho() != null && mayMoi.getKho().getMaKho() != null) {
                Kho khoMoi = khoDAO.findById(mayMoi.getKho().getMaKho()).orElse(null);
                if (khoMoi != null) {
                    mayCu.setKho(khoMoi);
                }
            }

            // Lưu lại
            MayIn updated = mayInDAO.save(mayCu);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 3. Xóa máy (Giữ nguyên hoặc thêm mới)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!mayInDAO.existsById(id)) return ResponseEntity.notFound().build();
        try {
            mayInDAO.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa: Máy đang có lịch sử giao dịch nhập/xuất!");
        }
    }
}