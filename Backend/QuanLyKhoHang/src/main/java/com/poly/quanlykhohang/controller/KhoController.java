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

    // --- NHẬP KHO ---
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

    // --- XUẤT KHO ---
    @GetMapping("/xuat")
    public ResponseEntity<?> layDanhSachPhieuXuat() {
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuXuatTomTat());
    }

    @GetMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> layChiTietPhieuXuat(@PathVariable String soPhieu) {
        return ResponseEntity.ok(giaoDichService.layPhieuXuatChiTiet(soPhieu));
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

    @PutMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> capNhatPhieuXuat(@PathVariable String soPhieu, @RequestBody PhieuXuatDTO dto) {
        return ResponseEntity.ok(giaoDichService.capNhatPhieuXuat(soPhieu, dto));
    }

    @DeleteMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> xoaPhieuXuat(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuXuat(soPhieu);
            return ResponseEntity.ok("Đã hủy phiếu xuất thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- QUẢN LÝ KHO ---
    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }
}