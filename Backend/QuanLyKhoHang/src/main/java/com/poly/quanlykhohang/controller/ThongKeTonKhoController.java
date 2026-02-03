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

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<BaoCaoResponseDTO> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc
    ) {
        String tenKho = "Tất cả các kho";
        if (maKho != 0) {
            tenKho = thongKeDAO.getTenKhoById(maKho);
        }

        List<BaoCaoXuatNhapTonDTO> list = getBaoCaoList(maKho, tuNgay, denNgay, loaiLoc);
        return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, list));
    }

    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            thongKeDAO.chotSoDauNam(nam, maKho);
            String tenKho = thongKeDAO.getTenKhoById(maKho);
            int namSau = nam + 1;

            List<BaoCaoXuatNhapTonDTO> result = getBaoCaoList(
                    maKho,
                    namSau + "-01-01",
                    namSau + "-12-31",
                    0
            );

            return ResponseEntity.ok(new BaoCaoResponseDTO(tenKho, result));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // =================================================================
    // CORE LOGIC: MAPPING & FILTERING
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> getBaoCaoList(Integer maKho, String tuNgay, String denNgay, Integer loaiLocCanLoc) {

        List<Object[]> rawResults = thongKeDAO.baoCaoTheoTrangThai(
                maKho, tuNgay, denNgay + " 23:59:59", 0
        );

        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        int stt = 1;

        for (Object[] row : rawResults) {
            // Lấy ID trạng thái an toàn
            Integer idTrangThai = toInteger(row[2]);

            // Filter
            if (loaiLocCanLoc != 0 && !idTrangThai.equals(loaiLocCanLoc)) {
                continue;
            }

            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);

            // --- [FIX LỖI ClassCastException] ---
            // Dùng toStringSafe() thay vì ép kiểu (String)
            dto.setMaSP(toStringSafe(row[0]));

            String tenGoc = toStringSafe(row[1]);
            String tenTrangThai = toStringSafe(row[3]);

            if (!tenTrangThai.isEmpty()) {
                dto.setTenSP(tenGoc + " - " + tenTrangThai);
            } else {
                dto.setTenSP(tenGoc);
            }

            dto.setDonvitinh(toStringSafe(row[4]));

            // Số lượng & Tiền
            Long tonCuoi = toNumber(row[8]);
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));
            dto.setTonCuoi(tonCuoi);

            BigDecimal thanhTien = toBigDecimal(row[9]);
            dto.setThanhTien(thanhTien);

            if (tonCuoi > 0 && thanhTien.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal giaBQ = thanhTien.divide(BigDecimal.valueOf(tonCuoi), 2, RoundingMode.HALF_UP);
                dto.setGiaBQ(giaBQ);
            } else {
                dto.setGiaBQ(BigDecimal.ZERO);
            }

            listBaoCao.add(dto);
        }

        return listBaoCao;
    }

    // --- Helper Methods ---

    // [QUAN TRỌNG] Chuyển mọi đối tượng thành String an toàn
    private String toStringSafe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return 1;
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return 0L; }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        try { return new BigDecimal(obj.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}