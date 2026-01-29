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

    @Autowired private GiaoDichKhoService giaoDichService;
    @Autowired private KhoService khoService;

    // ==================================================
    // 1. NHẬP KHO
    // ==================================================

    @GetMapping("/nhap")
    public ResponseEntity<?> layDanhSachPhieuNhap() {
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuNhapHienThi());
    }

    @GetMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuNhap(@PathVariable String soPhieu) {
        try {
            return ResponseEntity.ok(giaoDichService.layPhieuNhapChiTiet(soPhieu));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/nhap")
    public ResponseEntity<?> nhapKho(@RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.nhapKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi nhập kho: " + e.getMessage());
        }
    }

    @PutMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> capNhatPhieuNhap(@PathVariable String soPhieu, @RequestBody PhieuNhapDTO dto) {
        return ResponseEntity.ok(giaoDichService.capNhatPhieuNhap(soPhieu, dto));
    }

    @DeleteMapping("/nhap/{soPhieu}")
    public ResponseEntity<?> xoaPhieuNhap(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuNhap(soPhieu);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Xử lý chi tiết nhập ---
    @DeleteMapping("/nhap/chi-tiet/{maCTPN}")
    public ResponseEntity<?> xoaChiTiet(@PathVariable Integer maCTPN) {
        try {
            giaoDichService.xoaDongChiTietNhap(maCTPN);
            return ResponseEntity.ok("Đã xóa dòng chi tiết.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/nhap/{soPhieu}/bo-sung")
    public ResponseEntity<?> boSungChiTiet(@PathVariable String soPhieu, @RequestBody com.poly.quanlykhohang.dto.ChiTietNhapDTO dto) {
        try {
            giaoDichService.themDongVaoPhieuCu(soPhieu, dto);
            return ResponseEntity.ok("Đã bổ sung thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================================================
    // 2. XUẤT KHO
    // ==================================================

    @GetMapping("/xuat")
    public ResponseEntity<?> layDanhSachPhieuXuat() {
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuXuatHienThi());
    }

    @GetMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuXuat(@PathVariable String soPhieu) {
        try {
            return ResponseEntity.ok(giaoDichService.layPhieuXuatChiTiet(soPhieu));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/xuat")
    public ResponseEntity<?> xuatKho(@RequestBody PhieuXuatDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.xuatKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi xuất kho: " + e.getMessage());
        }
    }

    @DeleteMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> xoaPhieuXuat(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuXuat(soPhieu);
            return ResponseEntity.ok("Đã hủy phiếu xuất thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================================================
    // 3. QUẢN LÝ KHO (CRUD)
    // ==================================================

    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }
}