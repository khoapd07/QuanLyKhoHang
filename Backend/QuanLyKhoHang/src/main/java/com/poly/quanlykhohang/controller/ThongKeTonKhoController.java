package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.ThongKeDAO;
import com.poly.quanlykhohang.dto.BaoCaoResponseDTO;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    /**
     * API 1: XEM BÁO CÁO (GET)
     * - Backend lấy HẾT dữ liệu từ SQL (loaiLoc = 0).
     * - Sau đó Java tự lọc lại theo yêu cầu của Frontend.
     */
    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<BaoCaoResponseDTO> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc // 0: Tất cả, 1: Mới, 2: Cũ...
    ) {
        // 1. Lấy tên kho hiển thị
        String tenKho = "Tất cả các kho";
        if (maKho != 0) {
            tenKho = thongKeDAO.getTenKhoById(maKho);
        }

        // 2. Gọi hàm xử lý logic (Lấy dữ liệu thô -> Map -> Lọc -> Format)
        List<BaoCaoXuatNhapTonDTO> list = getBaoCaoList(maKho, tuNgay, denNgay, loaiLoc);

        // 3. Trả về
        return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, list));
    }

    /**
     * API 2: CHỐT SỔ (POST)
     */
    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            // 1. Chốt sổ SQL (Lưu vào năm + 1)
            thongKeDAO.chotSoDauNam(nam, maKho);

            // 2. Lấy tên kho
            String tenKho = thongKeDAO.getTenKhoById(maKho);

            // 3. Lấy lại dữ liệu hiển thị của NĂM SAU
            int namSau = nam + 1;
            List<BaoCaoXuatNhapTonDTO> result = getBaoCaoList(
                    maKho,
                    namSau + "-01-01",
                    namSau + "-12-31", // Lấy cả năm sau để xem cho trọn vẹn
                    0 // Mặc định hiển thị tất cả sau khi chốt
            );

            return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, result));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // =================================================================
    // CORE LOGIC: MAPPING & FILTERING (JAVA XỬ LÝ)
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> getBaoCaoList(Integer maKho, String tuNgay, String denNgay, Integer loaiLocCanLoc) {

        // BƯỚC 1: Gọi SQL lấy TOÀN BỘ dữ liệu (Truyền 0 vào SQL để lấy hết)
        // SQL Procedure: sp_BaoCaoXuatNhapTon_ChiTiet (hoặc bản cũ nhưng truyền 0)
        List<Object[]> rawResults = thongKeDAO.baoCaoTheoTrangThai(
                maKho, tuNgay, denNgay + " 23:59:59", 0
        );

        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        int stt = 1;

        for (Object[] row : rawResults) {
            /* * MAP INDEX THEO THỦ TỤC SQL MỚI (sp_BaoCaoXuatNhapTon_ChiTiet):
             * [0]: MaSP
             * [1]: TenSP (Gốc)
             * [2]: MaTrangThai (ID)
             * [3]: TenTrangThai (Tên)
             * [4]: Donvitinh
             * [5]: TonDau
             * [6]: NhapTrong
             * [7]: XuatTrong
             * [8]: TonCuoi
             * [9]: ThanhTien
             */

            // --- LỌC TẠI JAVA (FILTER) ---
            // Lấy ID trạng thái từ row[2]
            Integer idTrangThai = (row[2] != null) ? (Integer) row[2] : 1;

            // Nếu người dùng chọn lọc (khác 0) và dòng này không khớp -> BỎ QUA
            if (loaiLocCanLoc != 0 && !idTrangThai.equals(loaiLocCanLoc)) {
                continue;
            }

            // --- MAPPING DỮ LIỆU ---
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);

            dto.setMaSP((String) row[0]);

            // Xử lý Tên hiển thị: "iPhone 15" + " (Mới)"
            String tenGoc = (String) row[1];
            String tenTrangThai = (String) row[3];
            if (tenTrangThai != null && !tenTrangThai.trim().isEmpty()) {
                dto.setTenSP(tenGoc + " - " + tenTrangThai);
            } else {
                dto.setTenSP(tenGoc);
            }

            dto.setDonvitinh((String) row[4]);

            // Số lượng
            Long tonCuoi = toNumber(row[8]);
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));
            dto.setTonCuoi(tonCuoi);

            // Tiền tệ (BigDecimal)
            BigDecimal thanhTien = toBigDecimal(row[9]);
            dto.setThanhTien(thanhTien);

            // Tính Giá Bình Quân (GiaBQ) = Thành Tiền / Tồn Cuối (Java tự tính)
            if (tonCuoi > 0 && thanhTien.compareTo(BigDecimal.ZERO) > 0) {
                // Chia lấy 2 số thập phân, làm tròn Half Up
                BigDecimal giaBQ = thanhTien.divide(BigDecimal.valueOf(tonCuoi), 2, RoundingMode.HALF_UP);
                dto.setGiaBQ(giaBQ);
            } else {
                dto.setGiaBQ(BigDecimal.ZERO);
            }

            listBaoCao.add(dto);
        }

        return listBaoCao;
    }

    // Helper Convert Long an toàn
    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return 0L; }
    }

    // Helper Convert BigDecimal an toàn
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        try { return new BigDecimal(obj.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}