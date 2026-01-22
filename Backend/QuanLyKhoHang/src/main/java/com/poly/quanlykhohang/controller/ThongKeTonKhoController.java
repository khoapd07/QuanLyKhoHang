package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.ThongKeDAO; // Đảm bảo DAO đã có hàm chotSoDauNam
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
// Cấu hình CORS chuẩn để tránh lỗi 500 khi có allowCredentials
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    /**
     * API 1: XEM BÁO CÁO XUẤT NHẬP TỒN
     * URL: GET /api/thong-ke/xuat-nhap-ton
     */
    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<List<BaoCaoXuatNhapTonDTO>> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc
    ) {
        // 1. Gọi SQL (Cộng thêm giờ cuối ngày để lấy trọn vẹn dữ liệu ngày đó)
        List<Object[]> results = thongKeDAO.baoCaoTheoTrangThai(
                maKho,
                tuNgay,
                denNgay + " 23:59:59",
                loaiLoc
        );

        // 2. Map dữ liệu từ SQL (Object[]) sang DTO
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        int stt = 1;

        for (Object[] row : results) {
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();

            dto.setStt(stt++);
            dto.setMaSanPham((String) row[0]);
            dto.setTenSanPham((String) row[1]);
            dto.setDvt((String) row[2]);

            // Ép kiểu an toàn bằng hàm Helper bên dưới
            dto.setTonDauKy(toNumber(row[3]));
            dto.setNhapTrongKy(toNumber(row[4]));
            dto.setXuatTrongKy(toNumber(row[5]));
            dto.setTonCuoiKy(toNumber(row[6]));

            dto.setGiaBinhQuan(toDouble(row[7]));
            dto.setThanhTien(toDouble(row[8]));

            listBaoCao.add(dto);
        }

        return ResponseEntity.ok(listBaoCao);
    }

    /**
     * API 2: CHỐT SỔ ĐẦU NĂM (QUAN TRỌNG)
     * URL: POST /api/thong-ke/chot-so
     * Tác dụng: Tính toán tồn kho đến hết năm cũ và lưu vào bảng DMTonKho cho năm mới.
     * Cách dùng: Gọi 1 lần duy nhất đầu năm hoặc khi sửa dữ liệu năm cũ.
     */
    @PostMapping("/chot-so")
    public ResponseEntity<String> chotSoDauNam(
            @RequestParam Integer nam,
            @RequestParam Integer maKho
    ) {
        try {
            thongKeDAO.chotSoDauNam(nam, maKho);
            return ResponseEntity.ok("Đã chốt sổ thành công cho năm " + nam + " tại kho " + maKho);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi khi chốt sổ: " + e.getMessage());
        }
    }

    // --- HELPER METHODS (CHUYỂN ĐỔI KIỂU DỮ LIỆU AN TOÀN) ---

    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try {
            // Xử lý cả trường hợp obj là Integer hoặc BigDecimal từ SQL trả về
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}