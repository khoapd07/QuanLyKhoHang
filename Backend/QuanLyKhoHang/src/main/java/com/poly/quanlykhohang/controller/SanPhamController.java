package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.SanPhamDAO;
import com.poly.quanlykhohang.entity.SanPham;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamDAO sanPhamDAO;

    // 1. Lấy danh sách sản phẩm (ĐÃ THÊM LOGIC LỌC)
    @GetMapping
    public ResponseEntity<Page<SanPham>> getAll(
            @RequestParam(required = false) Integer maHang,
            @RequestParam(required = false) Integer maLoai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("maSP").ascending());

        // Gọi hàm filter, nếu maHang hoặc maLoai là null thì DB sẽ tự động lấy tất cả
        Page<SanPham> result = sanPhamDAO.filterSanPham(maHang, maLoai, pageable);

        return ResponseEntity.ok(result);
    }

    // 2. Lấy chi tiết 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        return sanPhamDAO.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Thêm sản phẩm mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SanPham sanPham) {
        if (sanPhamDAO.existsById(sanPham.getMaSP())) {
            return ResponseEntity.badRequest().body("Mã sản phẩm đã tồn tại!");
        }
        return ResponseEntity.ok(sanPhamDAO.save(sanPham));
    }

    // 4. Cập nhật sản phẩm
    @PutMapping("/{id}")
    @Transactional // Đảm bảo an toàn dữ liệu, lỗi sẽ rollback
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody SanPham spMoi) {
        try {
            SanPham spCu = sanPhamDAO.findById(id).orElse(null);
            if (spCu == null) {
                return ResponseEntity.notFound().build();
            }

            // Trường hợp 1: NGƯỜI DÙNG THAY ĐỔI MÃ SẢN PHẨM
            if (!id.equals(spMoi.getMaSP())) {
                // 1.1 Kiểm tra mã mới đã bị trùng với SP khác chưa?
                if (sanPhamDAO.existsById(spMoi.getMaSP())) {
                    return ResponseEntity.badRequest().body("Mã sản phẩm mới (" + spMoi.getMaSP() + ") đã tồn tại!");
                }

                // 1.2 Tạo bản ghi mới với Mã Mới
                SanPham spMoiMoi = new SanPham();
                spMoiMoi.setMaSP(spMoi.getMaSP());
                spMoiMoi.setTenSP(spMoi.getTenSP());
                spMoiMoi.setDonViTinh(spMoi.getDonViTinh());
                spMoiMoi.setMoTa(spMoi.getMoTa());
                spMoiMoi.setHangSanXuat(spMoi.getHangSanXuat());
                spMoiMoi.setLoaiSanPham(spMoi.getLoaiSanPham());
                spMoiMoi.setSoLuong(spCu.getSoLuong()); // Giữ nguyên số lượng cũ

                sanPhamDAO.save(spMoiMoi); // Lưu mã mới vào DB

                // 1.3 [QUAN TRỌNG] Chuyển đổi toàn bộ dữ liệu lịch sử sang mã mới TRƯỚC KHI xóa mã cũ
                sanPhamDAO.updateMaSpInDMMay(id, spMoi.getMaSP());
                sanPhamDAO.updateMaSpInCTPhieuNhap(id, spMoi.getMaSP());
                sanPhamDAO.updateMaSpInCTPhieuXuat(id, spMoi.getMaSP());
                sanPhamDAO.updateMaSpInDMTonKho(id, spMoi.getMaSP());

                // 1.4 Xóa mã cũ an toàn (vì không còn máy in hay phiếu nào bị dính với mã cũ nữa)
                sanPhamDAO.deleteById(id);
                return ResponseEntity.ok(spMoiMoi);
            }

            // Trường hợp 2: CHỈ SỬA THÔNG TIN, KHÔNG SỬA MÃ
            else {
                spCu.setTenSP(spMoi.getTenSP());
                spCu.setDonViTinh(spMoi.getDonViTinh());
                spCu.setMoTa(spMoi.getMoTa());
                spCu.setHangSanXuat(spMoi.getHangSanXuat());
                spCu.setLoaiSanPham(spMoi.getLoaiSanPham());
                return ResponseEntity.ok(sanPhamDAO.save(spCu));
            }
        } catch (Exception e) {
            // Nếu vẫn còn sót bảng nào đó giữ khóa ngoại chưa được xử lý, nó sẽ nhảy vào đây để chặn sập Server
            return ResponseEntity.badRequest().body("Lỗi hệ thống: Không thể chuyển đổi mã sản phẩm vì ràng buộc dữ liệu!");
        }
    }
    // 5. Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (!sanPhamDAO.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            sanPhamDAO.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa sản phẩm này (đã có phát sinh giao dịch nhập/xuất)!");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<SanPham>> getAllList() {
        return ResponseEntity.ok(sanPhamDAO.findAll(Sort.by("tenSP").ascending()));
    }
}