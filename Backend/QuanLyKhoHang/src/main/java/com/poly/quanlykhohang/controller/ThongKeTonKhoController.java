package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.ThongKeDAO;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    // =================================================================
    // 1. API BÁO CÁO XUẤT NHẬP TỒN
    // =================================================================
    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<?> getBaoCao(
            @RequestParam Integer maKho,
            @RequestParam String tuNgay,
            @RequestParam String denNgay,
            @RequestParam(defaultValue = "0") Integer loaiLoc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            String tenKho = (maKho == 0) ? "Tất cả các kho" : thongKeDAO.getTenKhoById(maKho);

            List<Object[]> rawResults = thongKeDAO.baoCaoPhanTrang(
                    maKho, tuNgay, denNgay + " 23:59:59", loaiLoc, page, size
            );

            List<BaoCaoXuatNhapTonDTO> allList = mapToDTOList(rawResults);

            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", allList);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lấy báo cáo: " + e.getMessage());
        }
    }

    // =================================================================
    // 2. API CHỐT SỔ (ĐÃ THÊM ĐỦ 5 THAM SỐ)
    // =================================================================
    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            thongKeDAO.chotSoDauNam(nam, maKho);
            String tenKho = (maKho == 0) ? "Tất cả các kho" : thongKeDAO.getTenKhoById(maKho);

            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(nam, maKho, 0, 999999);
            List<BaoCaoXuatNhapTonDTO> result = mapToDTOList(rawResults);

            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // 3. API XEM LỊCH SỬ (Chế độ tĩnh - 4 tham số)
    // =================================================================
    @GetMapping("/lich-su")
    public ResponseEntity<?> xemLichSu(
            @RequestParam Integer nam,
            @RequestParam Integer maKho,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            String tenKho = (maKho == 0) ? "Tất cả các kho" : thongKeDAO.getTenKhoById(maKho);

            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(nam, maKho, page, size);
            List<BaoCaoXuatNhapTonDTO> listDTO = mapToDTOList(rawResults);

            long totalItems = thongKeDAO.countLichSuChotSo(nam, maKho);
            List<Object[]> totals = thongKeDAO.getTongHopLichSu(nam, maKho);

            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", listDTO);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", (int) Math.ceil((double) totalItems / size));

            if (!totals.isEmpty() && totals.get(0) != null) {
                Object[] t = totals.get(0);
                Map<String, Object> grandTotal = new HashMap<>();
                // Đã sửa index lấy data khớp với kết quả SP trả về (3 là Tồn Cuối, 4 là Tổng Tiền)
                grandTotal.put("tongSL", t[3]);
                grandTotal.put("tongTien", t[4]);
                response.put("grandTotal", grandTotal);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải lịch sử: " + e.getMessage());
        }
    }

    // =================================================================
    // 4. API KIỂM TRA ĐÃ CHỐT SỔ CHƯA (VALIDATE)
    // =================================================================
    @GetMapping("/check-chot-so")
    public ResponseEntity<?> checkDaChotSo(
            @RequestParam Integer nam,
            @RequestParam Integer maKho
    ) {
        try {
            int count = thongKeDAO.demSoLuongBanGhiChotSo(nam, maKho);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi kiểm tra trạng thái chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // HELPER MAPPING
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> mapToDTOList(List<Object[]> rawResults) {
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        if (rawResults == null) return listBaoCao;

        for (Object[] row : rawResults) {
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setMaSP(toStringSafe(row[0]));

            // Đã sửa: Nối Tên SP và Trạng thái để Frontend lọc được chuẩn xác
            String tenSP = toStringSafe(row[1]);
            String trangThai = toStringSafe(row[3]);
            if (!trangThai.isEmpty()) {
                dto.setTenSP(tenSP + " - " + trangThai);
            } else {
                dto.setTenSP(tenSP);
            }

            dto.setDonvitinh(toStringSafe(row[4]));
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));
            dto.setTonCuoi(toNumber(row[8]));
            dto.setThanhTien(toBigDecimal(row[9]));

            if (dto.getTonCuoi() > 0 && dto.getThanhTien().compareTo(BigDecimal.ZERO) > 0) {
                dto.setGiaBQ(dto.getThanhTien().divide(BigDecimal.valueOf(dto.getTonCuoi()), 2, RoundingMode.HALF_UP));
            } else {
                dto.setGiaBQ(BigDecimal.ZERO);
            }
            listBaoCao.add(dto);
        }
        return listBaoCao;
    }

    private String toStringSafe(Object obj) { return (obj == null) ? "" : obj.toString(); }

    private Long toNumber(Object obj) {
        try { return obj == null ? 0L : Long.valueOf(obj.toString()); }
        catch (Exception e) { return 0L; }
    }

    private BigDecimal toBigDecimal(Object obj) {
        try { return obj == null ? BigDecimal.ZERO : new BigDecimal(obj.toString()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }
}