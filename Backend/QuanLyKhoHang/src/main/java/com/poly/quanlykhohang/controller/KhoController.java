package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.PhieuNhapDTO;
import com.poly.quanlykhohang.dto.PhieuXuatDTO;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.service.GiaoDichKhoService;
import com.poly.quanlykhohang.service.KhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kho")
public class KhoController {

    @Autowired
    private GiaoDichKhoService giaoDichService;

    @Autowired
    private KhoService khoService;

    // ================== QUẢN LÝ NHẬP KHO ==================

    // GET ALL (Tóm tắt)
    @GetMapping("/nhap")
    public ResponseEntity<?> layDanhSachPhieuNhap() {
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuNhapTomTat());
    }

    // GET DETAIL
    @GetMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuNhap(@PathVariable String soPhieu) {
        try {
            return ResponseEntity.ok(giaoDichService.layPhieuNhapChiTiet(soPhieu));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST (Tạo mới)
    @PostMapping("/nhap")
    public ResponseEntity<?> nhapKho(@RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.nhapKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // PUT (Cập nhật)
    @PutMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> capNhatPhieuNhap(@PathVariable String soPhieu, @RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.capNhatPhieuNhap(soPhieu, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // DELETE (Xóa)
    @DeleteMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> xoaPhieuNhap(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuNhap(soPhieu);
            return ResponseEntity.ok("Đã xóa thành công phiếu nhập: " + soPhieu);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa phiếu: " + e.getMessage());
        }
    }

    // ================== QUẢN LÝ XUẤT KHO (MỚI BỔ SUNG) ==================

    // 1. GET ALL XUẤT (Tóm tắt)
    @GetMapping("/xuat")
    public ResponseEntity<?> layDanhSachPhieuXuat() {
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuXuatTomTat());
    }

    // 2. GET DETAIL XUẤT
    @GetMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuXuat(@PathVariable String soPhieu) {
        try {
            return ResponseEntity.ok(giaoDichService.layPhieuXuatChiTiet(soPhieu));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. POST XUẤT (Tạo mới)
    @PostMapping("/xuat")
    public ResponseEntity<?> xuatKho(@RequestBody PhieuXuatDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.xuatKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 4. PUT XUẤT (Cập nhật ghi chú/khách hàng)
    @PutMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> capNhatPhieuXuat(@PathVariable String soPhieu, @RequestBody PhieuXuatDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.capNhatPhieuXuat(soPhieu, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // 5. DELETE XUẤT (Hủy phiếu xuất - Trả hàng về kho)
    @DeleteMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> xoaPhieuXuat(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuXuat(soPhieu);
            return ResponseEntity.ok("Đã hủy phiếu xuất " + soPhieu + " và trả hàng về kho thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xóa phiếu: " + e.getMessage());
        }
    }

    // --- API QUẢN LÝ KHO (CRUD) ---
    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }
}