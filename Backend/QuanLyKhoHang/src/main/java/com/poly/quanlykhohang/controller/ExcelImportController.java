package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import com.poly.quanlykhohang.service.ThongKeExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
public class ExcelImportController {

    @Autowired
    private ThongKeExcelService excelService;

    @PostMapping("/import-excel")
    public ResponseEntity<?> importBaoCaoExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maKho", defaultValue = "1") Integer maKho,
            @RequestParam(value = "ngayNhap", required = false) String ngayNhapStr) { // <--- THÊM THAM SỐ NÀY
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng chọn file Excel.");
            }

            // Chuyển đổi chuỗi ngày giờ từ UI sang LocalDateTime
            LocalDateTime ngayTaoPhieu = null;
            if (ngayNhapStr != null && !ngayNhapStr.trim().isEmpty()) {
                ngayTaoPhieu = LocalDateTime.parse(ngayNhapStr);
            }

            // Gọi service đọc file, TỰ ĐỘNG SINH PHIẾU, và lấy danh sách DTO
            List<BaoCaoXuatNhapTonDTO> dataList = excelService.processAndImportExcel(file, maKho, ngayTaoPhieu); // <--- TRUYỀN THÊM VÀO HÀM

            Map<String, Object> response = new HashMap<>();
            response.put("danhSachChiTiet", dataList);
            response.put("thongBao", "Import thành công! Đã tự động tạo Phiếu Nhập & Phiếu Xuất.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi import file Excel: " + e.getMessage());
        }
    }
}