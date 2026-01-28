package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.ThongKeDAO;
import com.poly.quanlykhohang.dto.BaoCaoResponseDTO; // Import DTO Wrapper
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    /**
     * API 1: XEM BÁO CÁO (GET)
     * SỬA: Trả về BaoCaoResponseDTO thay vì List
     */
    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<BaoCaoResponseDTO> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc
    ) {
        // 1. Lấy tên kho riêng (để hiển thị tiêu đề dù không có số liệu)
        String tenKho = "Tất cả các kho"; // Mặc định
        if (maKho != 0) {
            // Nếu khác 0 mới gọi vào DB lấy tên
            tenKho = thongKeDAO.getTenKhoById(maKho);
        }

        // 2. Lấy danh sách chi tiết
        List<BaoCaoXuatNhapTonDTO> list = getBaoCaoList(maKho, tuNgay, denNgay, loaiLoc);

        // 3. Đóng gói trả về
        return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, list));
    }

    /**
     * API 2: CHỐT SỔ (POST)
     */
    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            thongKeDAO.chotSoDauNam(nam, maKho);
            String tenKho = thongKeDAO.getTenKhoById(maKho);
            List<BaoCaoXuatNhapTonDTO> result = getBaoCaoList(maKho, nam + "-01-01", nam + "-01-31", 0);
            return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // =================================================================
    // HÀM XỬ LÝ CHUNG (ĐÃ FIX INDEX VÀ TÊN SETTER)
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> getBaoCaoList(Integer maKho, String tuNgay, String denNgay, Integer loaiLoc) {
        List<Object[]> results = thongKeDAO.baoCaoTheoTrangThai(
                maKho, tuNgay, denNgay + " 23:59:59", loaiLoc
        );

        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        int stt = 1;

        for (Object[] row : results) {
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);

            // --- CẬP NHẬT INDEX MỚI (Lùi lại 1 số so với code cũ) ---

            dto.setMaSP((String) row[0]);           // Index 0 (Trước là 1)
            dto.setTenSP((String) row[1]);          // Index 1
            dto.setDonvitinh((String) row[2]);      // Index 2

            dto.setTonDau(toNumber(row[3]));        // Index 3
            dto.setNhapTrong(toNumber(row[4]));     // Index 4
            dto.setXuatTrong(toNumber(row[5]));     // Index 5
            dto.setTonCuoi(toNumber(row[6]));       // Index 6

            dto.setGiaBQ(toBigDecimal(row[7]));     // Index 7
            dto.setThanhTien(toBigDecimal(row[8])); // Index 8

            listBaoCao.add(dto);
        }
        return listBaoCao;
    }

    // Helper Convert Long
    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return 0L; }
    }

    // Helper Convert BigDecimal (Thay cho Double cũ)
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        try { return new BigDecimal(obj.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}