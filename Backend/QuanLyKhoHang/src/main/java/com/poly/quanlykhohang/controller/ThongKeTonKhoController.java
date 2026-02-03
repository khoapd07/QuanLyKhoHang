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

            // 2. Lấy TỔNG HỢP TOÀN BỘ (Count, Sum Tồn, Sum Nhập...)
            // Kết quả trả về là mảng Object[0] chứa các cột: [Count, TonDau, Nhap, Xuat, TonCuoi, Tien]
            Object[] summaryData = (Object[]) thongKeDAO.getTongHopBaoCao(
                    maKho, tuNgay, denNgay + " 23:59:59", loaiLoc
            )[0]; // Lấy dòng đầu tiên

            long totalItems = toNumber(summaryData[0]); // Cột 0 là Count(*)
            int totalPages = (int) Math.ceil((double) totalItems / size);

            // Tạo Object chứa tổng cộng để gửi xuống Vue
            Map<String, Object> grandTotal = new HashMap<>();
            grandTotal.put("tdk", toNumber(summaryData[1]));
            grandTotal.put("ntk", toNumber(summaryData[2]));
            grandTotal.put("xtk", toNumber(summaryData[3]));
            grandTotal.put("tck", toNumber(summaryData[4]));
            grandTotal.put("tien", toBigDecimal(summaryData[5]));

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
            response.put("grandTotal", grandTotal); // [QUAN TRỌNG]: Gửi tổng cộng xuống

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

            // 2. Lấy dữ liệu demo của năm sau (kết quả)
            int namSau = nam + 1;
            int page = 0;
            int size = 20;

            // Lấy danh sách phân trang
            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(namSau, maKho, page, size);

            // Đếm tổng số dòng
            long totalItems = thongKeDAO.countLichSuChotSo(namSau, maKho);
            int totalPages = (int) Math.ceil((double) totalItems / size);

            // [MỚI] Lấy Tổng Cộng Toàn Bộ (Grand Total)
            Map<String, Object> grandTotal = getGrandTotalMap(namSau, maKho);

            // Convert DTO & STT
            List<BaoCaoXuatNhapTonDTO> result = mapToDTOList(rawResults, 0);
            int startStt = 1;
            for (BaoCaoXuatNhapTonDTO dto : result) {
                dto.setStt(startStt++);
            }

            // 3. Đóng gói response
            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", result);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("grandTotal", grandTotal); // <--- Gửi tổng xuống Frontend

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // 3. API XEM LỊCH SỬ (PHÂN TRANG) - Đã thêm Grand Total
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

            // 1. Lấy dữ liệu phân trang
            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(nam, maKho, page, size);

            // 2. Tính tổng số trang
            long totalItems = thongKeDAO.countLichSuChotSo(nam, maKho);
            int totalPages = (int) Math.ceil((double) totalItems / size);

            // [MỚI] Lấy Tổng Cộng Toàn Bộ (Grand Total)
            Map<String, Object> grandTotal = getGrandTotalMap(nam, maKho);

            // 3. Convert DTO
            List<BaoCaoXuatNhapTonDTO> listDTO = mapToDTOList(rawResults, 0);

            // 4. Cập nhật STT
            int startStt = (page * size) + 1;
            for (BaoCaoXuatNhapTonDTO dto : listDTO) {
                dto.setStt(startStt++);
            }

            // 5. Đóng gói kết quả
            Map<String, Object> response = new HashMap<>();
            response.put("tenKho", tenKho);
            response.put("danhSachChiTiet", listDTO);
            response.put("currentPage", page);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("grandTotal", grandTotal); // <--- Gửi tổng xuống Frontend

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi tải lịch sử: " + e.getMessage());
        }
    }

    // =================================================================
    // HELPER: HÀM MAP DỮ LIỆU DÙNG CHUNG
    // =================================================================
    private List<BaoCaoXuatNhapTonDTO> mapToDTOList(List<Object[]> rawResults, Integer loaiLocCanLoc) {
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();

        if (rawResults == null || rawResults.isEmpty()) {
            return listBaoCao;
        }

        // Lưu ý: STT ở đây chỉ là tạm thời, sẽ được set lại ở Controller dựa trên page
        int stt = 1;

        for (Object[] row : rawResults) {
            Integer idTrangThai = toInteger(row[2]);

            if (loaiLocCanLoc != 0 && !idTrangThai.equals(loaiLocCanLoc)) {
                continue;
            }

            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setStt(stt++);

            // Mapping dữ liệu
            dto.setMaSP(toStringSafe(row[0]));
            String tenGoc = toStringSafe(row[1]);
            String tenTrangThai = toStringSafe(row[3]);

            if (!tenTrangThai.isEmpty()) {
                dto.setTenSP(tenGoc + " - " + tenTrangThai);
            } else {
                dto.setTenSP(tenGoc);
            }

            dto.setDonvitinh(toStringSafe(row[4]));
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));

            Long tonCuoi = toNumber(row[8]);
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

    // --- Helper Methods: Safe Conversion ---
    private String toStringSafe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private Integer toInteger(Object obj) {
        if (obj == null) return 1;
        try { return Integer.valueOf(obj.toString()); } catch (Exception e) { return 1; }
    }

    private Long toNumber(Object obj) {
        if (obj == null) return 0L;
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return 0L; }
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        try { return new BigDecimal(obj.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }


    private Map<String, Object> getGrandTotalMap(Integer nam, Integer maKho) {
        Map<String, Object> grandTotal = new HashMap<>();
        try {
            // Gọi hàm DAO lấy Object[] (đã khai báo ở bước trước)
            Object[] rawData = thongKeDAO.getTongHopLichSu(nam, maKho);

            // JPA thường trả về List<Object[]> nếu là native query
            // Nhưng nếu query trả về 1 dòng duy nhất, đôi khi nó trả về Object[] hoặc List tùy cấu hình
            // Code an toàn nhất: ép kiểu
            Object[] row = null;
            if (rawData != null && rawData.length > 0) {
                // Trường hợp trả về mảng 1 dòng
                if (rawData[0] instanceof Object[]) {
                    row = (Object[]) rawData[0];
                } else {
                    row = rawData;
                }
            }

            if (row != null) {
                grandTotal.put("tdk", toNumber(row[0]));
                grandTotal.put("ntk", toNumber(row[1]));
                grandTotal.put("xtk", toNumber(row[2]));
                grandTotal.put("tck", toNumber(row[3]));
                grandTotal.put("tien", toBigDecimal(row[4]));
            } else {
                // Không có dữ liệu thì trả về 0
                grandTotal.put("tdk", 0); grandTotal.put("ntk", 0); grandTotal.put("xtk", 0); grandTotal.put("tck", 0); grandTotal.put("tien", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback về 0 nếu lỗi
            grandTotal.put("tdk", 0); grandTotal.put("ntk", 0); grandTotal.put("xtk", 0); grandTotal.put("tck", 0); grandTotal.put("tien", 0);
        }
        return grandTotal;
    }
}