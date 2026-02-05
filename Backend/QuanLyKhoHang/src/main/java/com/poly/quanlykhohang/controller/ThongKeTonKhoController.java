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
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    // =================================================================
    // 1. API BÁO CÁO XUẤT NHẬP TỒN (LIVE DATA - CÓ PHÂN TRANG)
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

            // 1. Lấy dữ liệu chi tiết (Phân trang)
            List<Object[]> rawResults = thongKeDAO.baoCaoPhanTrang(
                    maKho, tuNgay, denNgay + " 23:59:59", loaiLoc, page, size
            );

            // [SỬA LỖI] 2. Lấy TỔNG HỢP TOÀN BỘ
            // DAO trả về List<Object[]> -> Cần lấy phần tử đầu tiên
            List<Object[]> summaryList = thongKeDAO.getTongHopBaoCao(
                    maKho, tuNgay, denNgay + " 23:59:59", loaiLoc
            );

            // Mặc định giá trị 0 nếu không có kết quả
            long totalItems = 0;
            Map<String, Object> grandTotal = new HashMap<>();
            grandTotal.put("tdk", 0); grandTotal.put("ntk", 0); grandTotal.put("xtk", 0); grandTotal.put("tck", 0); grandTotal.put("tien", 0);

            if (summaryList != null && !summaryList.isEmpty()) {
                Object[] summaryData = summaryList.get(0); // Lấy dòng đầu tiên
                if (summaryData != null) {
                    totalItems = toNumber(summaryData[0]); // Cột 0 là Count(*)
                    grandTotal.put("tdk", toNumber(summaryData[1]));
                    grandTotal.put("ntk", toNumber(summaryData[2]));
                    grandTotal.put("xtk", toNumber(summaryData[3]));
                    grandTotal.put("tck", toNumber(summaryData[4]));
                    grandTotal.put("tien", toBigDecimal(summaryData[5]));
                }
            }

            int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / size) : 0;

            // 3. Convert dữ liệu chi tiết
            List<BaoCaoXuatNhapTonDTO> list = mapToDTOList(rawResults, loaiLoc);
            int startStt = (page * size) + 1;
            for (BaoCaoXuatNhapTonDTO dto : list) dto.setStt(startStt++);

            // 4. Trả về
            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", list);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("grandTotal", grandTotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // =================================================================
    // 2. API CHỐT SỔ (ACTION)
    // =================================================================
    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            // 1. Thực hiện chốt sổ
            thongKeDAO.chotSoDauNam(nam, maKho);
            String tenKho = thongKeDAO.getTenKhoById(maKho);

            // 2. Lấy dữ liệu demo của năm sau
            int namSau = nam + 1;
            int page = 0;
            int size = 20;

            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(namSau, maKho, page, size);
            long totalItems = thongKeDAO.countLichSuChotSo(namSau, maKho);
            int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / size) : 0;

            Map<String, Object> grandTotal = getGrandTotalMap(namSau, maKho);

            List<BaoCaoXuatNhapTonDTO> result = mapToDTOList(rawResults, 0);
            int startStt = 1;
            for (BaoCaoXuatNhapTonDTO dto : result) {
                dto.setStt(startStt++);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", result);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("grandTotal", grandTotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // 3. API XEM LỊCH SỬ (PHÂN TRANG)
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
            long totalItems = thongKeDAO.countLichSuChotSo(nam, maKho);
            int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / size) : 0;

            Map<String, Object> grandTotal = getGrandTotalMap(nam, maKho);

            List<BaoCaoXuatNhapTonDTO> listDTO = mapToDTOList(rawResults, 0);
            int startStt = (page * size) + 1;
            for (BaoCaoXuatNhapTonDTO dto : listDTO) {
                dto.setStt(startStt++);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", listDTO);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("grandTotal", grandTotal);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
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
            // Gọi hàm DAO có sẵn: demSoLuongBanGhiChotSo
            // Hàm này đếm trong bảng DMTonKho xem có dữ liệu của Nam và MaKho đó không
            int count = thongKeDAO.demSoLuongBanGhiChotSo(nam, maKho);

            // Trả về true nếu count > 0 (đã chốt), false nếu chưa
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi kiểm tra chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // HELPER: HÀM MAP DỮ LIỆU
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> mapToDTOList(List<Object[]> rawResults, Integer loaiLocCanLoc) {
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        if (rawResults == null || rawResults.isEmpty()) return listBaoCao;

        int stt = 1;
        for (Object[] row : rawResults) {
            Integer idTrangThai = toInteger(row[2]);
            if (loaiLocCanLoc != 0 && !idTrangThai.equals(loaiLocCanLoc)) continue;

            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);
            dto.setMaSP(toStringSafe(row[0]));

            String tenGoc = toStringSafe(row[1]);
            String tenTrangThai = toStringSafe(row[3]);
            dto.setTenSP(!tenTrangThai.isEmpty() ? tenGoc + " - " + tenTrangThai : tenGoc);

            dto.setDonvitinh(toStringSafe(row[4]));
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));

            Long tonCuoi = toNumber(row[8]);
            dto.setTonCuoi(tonCuoi);

            BigDecimal thanhTien = toBigDecimal(row[9]);
            dto.setThanhTien(thanhTien);

            if (tonCuoi > 0 && thanhTien.compareTo(BigDecimal.ZERO) > 0) {
                dto.setGiaBQ(thanhTien.divide(BigDecimal.valueOf(tonCuoi), 2, RoundingMode.HALF_UP));
            } else {
                dto.setGiaBQ(BigDecimal.ZERO);
            }
            listBaoCao.add(dto);
        }
        return listBaoCao;
    }

    // --- Helper Methods ---
    private String toStringSafe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
    private Integer toInteger(Object obj) {
        try { return obj == null ? 1 : Integer.valueOf(obj.toString()); } catch (Exception e) { return 1; }
    }
    private Long toNumber(Object obj) {
        try { return obj == null ? 0L : Long.valueOf(obj.toString()); } catch (Exception e) { return 0L; }
    }
    private BigDecimal toBigDecimal(Object obj) {
        try { return obj == null ? BigDecimal.ZERO : new BigDecimal(obj.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    // [SỬA LỖI] Helper lấy Grand Total (Map từ List<Object[]> sang Map)
    private Map<String, Object> getGrandTotalMap(Integer nam, Integer maKho) {
        Map<String, Object> grandTotal = new HashMap<>();
        grandTotal.put("tdk", 0); grandTotal.put("ntk", 0); grandTotal.put("xtk", 0); grandTotal.put("tck", 0); grandTotal.put("tien", 0);

        try {
            // DAO trả về List<Object[]>
            List<Object[]> rawList = thongKeDAO.getTongHopLichSu(nam, maKho);

            if (rawList != null && !rawList.isEmpty()) {
                Object[] row = rawList.get(0); // Lấy dòng đầu tiên
                if (row != null) {
                    grandTotal.put("tdk", toNumber(row[0]));
                    grandTotal.put("ntk", toNumber(row[1]));
                    grandTotal.put("xtk", toNumber(row[2]));
                    grandTotal.put("tck", toNumber(row[3]));
                    grandTotal.put("tien", toBigDecimal(row[4]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grandTotal;
    }
}