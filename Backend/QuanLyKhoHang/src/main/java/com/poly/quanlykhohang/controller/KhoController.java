package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.entity.PhieuChuyen;
import com.poly.quanlykhohang.service.GiaoDichKhoService;
import com.poly.quanlykhohang.service.KhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kho")
public class KhoController {

    @Autowired
    private GiaoDichKhoService giaoDichService; // [QUAN TRỌNG] Dùng thống nhất 1 tên biến này

    @Autowired
    private MayInDAO mayInDAO;

    @Autowired
    private KhoService khoService;

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
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    @PostMapping("/nhap")
    public ResponseEntity<?> nhapKho(@RequestBody PhieuNhapDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.nhapKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createError("Lỗi nhập kho: " + e.getMessage()));
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
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    // --- Xử lý chi tiết nhập ---
    @DeleteMapping("/nhap/chi-tiet/{maCTPN}")
    public ResponseEntity<?> xoaChiTiet(@PathVariable Integer maCTPN) {
        try {
            giaoDichService.xoaDongChiTietNhap(maCTPN);
            return ResponseEntity.ok("Đã xóa dòng chi tiết.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    @PostMapping("/nhap/{soPhieu}/bo-sung")
    public ResponseEntity<?> boSungChiTiet(@PathVariable String soPhieu, @RequestBody com.poly.quanlykhohang.dto.ChiTietNhapDTO dto) {
        try {
            giaoDichService.themDongVaoPhieuCu(soPhieu, dto);
            return ResponseEntity.ok("Đã bổ sung thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
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
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    @PostMapping("/xuat")
    public ResponseEntity<?> xuatKho(@RequestBody PhieuXuatDTO dto) {
        try {
            return ResponseEntity.ok(giaoDichService.xuatKho(dto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createError("Lỗi xuất kho: " + e.getMessage()));
        }
    }

    @DeleteMapping("/xuat/{soPhieu}")
    public ResponseEntity<?> xoaPhieuXuat(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuXuat(soPhieu);
            return ResponseEntity.ok("Đã hủy phiếu xuất thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    // ==================================================
    // 3. QUẢN LÝ KHO (CRUD) & TRA CỨU
    // ==================================================

    @GetMapping
    public ResponseEntity<?> layDanhSachKho() {
        return ResponseEntity.ok(khoService.layDanhSachKho());
    }

    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoMoiKho(@RequestBody Kho kho) {
        return ResponseEntity.ok(khoService.luuKho(kho));
    }

    @GetMapping("/may-in/kha-dung")
    public ResponseEntity<List<String>> getMayInKhaDung(
            @RequestParam("maSP") String maSP,
            @RequestParam("maKho") Integer maKho) {

        List<String> listSerials = mayInDAO.findMaMayTonKho(maSP, maKho);
        return ResponseEntity.ok(listSerials);
    }

    // ==================================================
    // 4. QUẢN LÝ CHUYỂN KHO
    // ==================================================

    @GetMapping("/chuyen")
    public ResponseEntity<List<PhieuChuyenResponseDTO>> layDanhSachChuyen() {
        // [SỬA LỖI] Dùng đúng tên biến 'giaoDichService' đã khai báo ở trên
        return ResponseEntity.ok(giaoDichService.layDanhSachPhieuChuyen());
    }

    @GetMapping("/chuyen/{soPhieu}")
    public ResponseEntity<PhieuChuyen> layChiTietChuyen(@PathVariable String soPhieu) {
        return ResponseEntity.ok(giaoDichService.layChiTietPhieuChuyen(soPhieu));
    }

    @PostMapping("/chuyen")
    public ResponseEntity<?> taoPhieuChuyen(@RequestBody ChuyenKhoDTO dto) {
        try {
            PhieuChuyen phieu = giaoDichService.thucHienChuyenKho(dto);
            return ResponseEntity.ok(phieu);
        } catch (Exception e) {
            // [SỬA LỖI] Thay ErrorResponse bằng Map đơn giản
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }

    // Helper method để tạo JSON lỗi trả về frontend
    private Map<String, String> createError(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return error;
    }

    @DeleteMapping("/chuyen/{soPhieu}")
    public ResponseEntity<?> xoaPhieuChuyen(@PathVariable String soPhieu) {
        try {
            giaoDichService.xoaPhieuChuyen(soPhieu);
            return ResponseEntity.ok(createError("Đã hủy phiếu chuyển và hoàn trả máy về kho cũ."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createError(e.getMessage()));
        }
    }
}