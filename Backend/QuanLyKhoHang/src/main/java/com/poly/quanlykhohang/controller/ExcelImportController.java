package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import com.poly.quanlykhohang.dto.SyncTonKhoDTO;
import com.poly.quanlykhohang.service.ThongKeExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
            @RequestParam(value = "nam", required = false) Integer namLuuTon) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("message", "Vui lòng chọn file Excel hợp lệ.");
                return ResponseEntity.badRequest().body(response);
            }

            // Nếu giao diện không gửi năm hoặc gửi lỗi, tự động lấy năm hiện tại
            if (namLuuTon == null || namLuuTon <= 0) {
                namLuuTon = LocalDate.now().getYear();
            }

            // Gọi service chỉ đọc file và tính Tồn Cuối (KHÔNG LƯU DB TỰ ĐỘNG NỮA)
            List<BaoCaoXuatNhapTonDTO> dataList = excelService.processAndImportExcel(file, maKho, namLuuTon);

            // Gửi dữ liệu và thông báo về cho VueJS
            response.put("danhSachChiTiet", dataList);
            response.put("thongBao", "Đọc file Excel thành công! Vui lòng kiểm tra dữ liệu và bấm 'Lưu Tồn Cuối' để chốt sổ.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console của Backend để dễ debug

            // Trả về JSON chứa key "message" để VueJS (Axios) bắt được lỗi
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==============================================================
    // 2 API MỚI: ĐỒNG BỘ TỒN KHO & XÓA TỒN KHO
    // ==============================================================

    @PostMapping("/sync-import")
    public ResponseEntity<?> syncImportTonKho(@RequestBody List<SyncTonKhoDTO> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (payload == null || payload.isEmpty()) {
                response.put("message", "Dữ liệu trống, không có gì để lưu.");
                return ResponseEntity.badRequest().body(response);
            }

            excelService.syncToDMTonKho(payload);

            response.put("message", "Lưu tồn kho thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Lỗi lưu dữ liệu: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/xoa-theo-nam")
    public ResponseEntity<?> xoaTonKhoTheoNam(
            @RequestParam("nam") Integer nam,
            @RequestParam("maKho") Integer maKho) {
        Map<String, Object> response = new HashMap<>();
        try {
            excelService.xoaTonKhoTheoNam(nam, maKho);

            response.put("message", "Xóa danh mục tồn kho thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Lỗi khi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}