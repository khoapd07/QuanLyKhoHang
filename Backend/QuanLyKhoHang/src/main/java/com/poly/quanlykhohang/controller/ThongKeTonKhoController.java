package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.ThongKeDAO;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
     */
    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<List<BaoCaoXuatNhapTonDTO>> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc
    ) {
        // Gọi hàm xử lý chung bên dưới
        List<BaoCaoXuatNhapTonDTO> list = getBaoCaoList(maKho, tuNgay, denNgay, loaiLoc);
        return ResponseEntity.ok(list);
    }

    /**
     * API 2: CHỐT SỔ & HIỆN KẾT QUẢ NGAY (POST)
     * Trả về: List<DTO> (Giống hệt API 1) thay vì String thông báo
     */
    @PostMapping("/chot-so")
    @Transactional // Đảm bảo giao dịch an toàn
    public ResponseEntity<?> chotSoDauNam(
            @RequestParam Integer nam,
            @RequestParam Integer maKho
    ) {
        try {
            // Bước 1: Gọi DAO để tính toán và lưu vào DB
            thongKeDAO.chotSoDauNam(nam, maKho);

            // Bước 2: Tự động lấy dữ liệu Tháng 1 của năm đó để trả về
            // (Giúp người dùng thấy ngay kết quả vừa chốt)
            String tuNgay = nam + "-01-01";
            String denNgay = nam + "-01-31"; // Lấy hết tháng 1

            // Gọi hàm xử lý chung (Tái sử dụng code)
            List<BaoCaoXuatNhapTonDTO> result = getBaoCaoList(maKho, tuNgay, denNgay, 0);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // HÀM XỬ LÝ CHUNG (PRIVATE) - Để 2 API trên cùng gọi vào
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> getBaoCaoList(Integer maKho, String tuNgay, String denNgay, Integer loaiLoc) {
        // 1. Gọi SQL
        List<Object[]> results = thongKeDAO.baoCaoTheoTrangThai(
                maKho,
                tuNgay,
                denNgay + " 23:59:59",
                loaiLoc
        );

        // 2. Map dữ liệu sang DTO
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        int stt = 1;

        for (Object[] row : results) {
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);
            dto.setMaSanPham((String) row[0]);
            dto.setTenSanPham((String) row[1]);
            dto.setDvt((String) row[2]);
            dto.setTonDauKy(toNumber(row[3]));
            dto.setNhapTrongKy(toNumber(row[4]));
            dto.setXuatTrongKy(toNumber(row[5]));
            dto.setTonCuoiKy(toNumber(row[6]));
            dto.setGiaBinhQuan(toDouble(row[7]));
            dto.setThanhTien(toDouble(row[8]));
            listBaoCao.add(dto);
        }
        return listBaoCao;
    }

    // --- HELPER CONVERT ---
    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return 0L; }
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        try { return Double.parseDouble(obj.toString()); } catch (Exception e) { return 0.0; }
    }
}