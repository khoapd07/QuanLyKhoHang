package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.HinhThucNhapDAO;
import com.poly.quanlykhohang.dao.PhieuNhapDAO;
import com.poly.quanlykhohang.entity.HinhThucNhap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/hinh-thuc-nhap")
//@CrossOrigin("*") // Cho phép frontend VueJS gọi API
public class HinhThucNhapController {

    @Autowired
    private HinhThucNhapDAO hinhThucNhapDAO;

    @Autowired
    private PhieuNhapDAO phieuNhapDAO; // Cần DAO này để check ràng buộc khóa ngoại

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(hinhThucNhapDAO.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody HinhThucNhap hinhThucNhap) {
        // Validate trùng tên
        if (hinhThucNhapDAO.existsByTenHT((hinhThucNhap.getTenHT()))) {
            return ResponseEntity.badRequest().body("Tên hình thức nhập này đã tồn tại!");
        }
        return ResponseEntity.ok(hinhThucNhapDAO.save(hinhThucNhap));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody HinhThucNhap hinhThucNhap) {
        Optional<HinhThucNhap> existing = hinhThucNhapDAO.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validate trùng tên (nhưng bỏ qua chính nó)
        HinhThucNhap htnDuplicate = hinhThucNhapDAO.findByTenHT((hinhThucNhap.getTenHT()));
        if (htnDuplicate != null && !htnDuplicate.getMaHT().equals(id)) {
            return ResponseEntity.badRequest().body("Tên hình thức nhập này đã tồn tại!");
        }

        HinhThucNhap updateHtn = existing.get();
        updateHtn.setTenHT(hinhThucNhap.getTenHT());
        return ResponseEntity.ok(hinhThucNhapDAO.save(updateHtn));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        // Validate không cho xóa nếu đã có Phiếu Nhập sử dụng
        // Giả sử trong PhieuNhapDAO có hàm existsByHinhThucNhap_Id(Integer id)
        if (phieuNhapDAO.existsByHinhThucNhap_MaHT((id))) {
            return ResponseEntity.badRequest().body("Không thể xóa do hình thức này đã phát sinh phiếu nhập!");
        }
        hinhThucNhapDAO.deleteById(id);
        return ResponseEntity.ok("Xóa thành công!");
    }
}