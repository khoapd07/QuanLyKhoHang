package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.dao.ThongKeDAO;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeTonKhoController {

    @Autowired
    private ThongKeDAO thongKeDAO;

    @Autowired
    private TaiKhoanDAO taiKhoanDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =================================================================
    // 1. API BÁO CÁO XUẤT NHẬP TỒN (Theo khoảng thời gian)
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
            // Lấy dữ liệu thô từ Database (Cộng thêm thời gian để lấy hết ngày cuối)
            List<Object[]> rawResults = thongKeDAO.baoCaoPhanTrang(
                    maKho, tuNgay, denNgay + " 23:59:59", loaiLoc, page, size
            );

            // Chuyển đổi dữ liệu thô sang danh sách DTO
            List<BaoCaoXuatNhapTonDTO> danhSach = mapToDTOList(rawResults);

            // Sử dụng hàm Helper để tạo Response rút gọn code
            Map<String, Object> response = createBaseResponse(maKho, danhSach, page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy báo cáo: " + e.getMessage());
        }
    }

    // =================================================================
    // 2. API CHỐT SỔ TỒN ĐẦU NĂM
    // =================================================================
    @PostMapping("/chot-so")
    public ResponseEntity<?> chotSoDauNam(@RequestParam Integer nam, @RequestParam Integer maKho) {
        try {
            // 1. Thực thi procedure Chốt Sổ dưới Database
            thongKeDAO.chotSoDauNam(nam, maKho);

            // 2. Sau khi chốt xong, tự động load lại danh sách chốt sổ (lấy max size để xem toàn bộ)
            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(nam, maKho, 0, 999999);
            List<BaoCaoXuatNhapTonDTO> danhSach = mapToDTOList(rawResults);

            return ResponseEntity.ok(createBaseResponse(maKho, danhSach, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi chốt sổ: " + e.getMessage());
        }
    }

    // =================================================================
    // 3. API XEM LỊCH SỬ CHỐT SỔ (Của các năm trước)
    // =================================================================
    @GetMapping("/lich-su")
    public ResponseEntity<?> xemLichSu(
            @RequestParam Integer nam,
            @RequestParam Integer maKho,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            // Lấy danh sách hiển thị
            List<Object[]> rawResults = thongKeDAO.getLichSuChotSoPhanTrang(nam, maKho, page, size);
            List<BaoCaoXuatNhapTonDTO> danhSach = mapToDTOList(rawResults);

            // Khởi tạo Response cơ bản
            Map<String, Object> response = createBaseResponse(maKho, danhSach, page);

            // Đếm tổng số dòng (phục vụ phân trang UI)
            long totalItems = thongKeDAO.countLichSuChotSo(nam, maKho);
            response.put("totalItems", totalItems);
            response.put("totalPages", (int) Math.ceil((double) totalItems / size));

            // Lấy tổng cộng (Tổng SL Tồn, Tổng Tiền Tồn) để hiển thị dòng Dưới Cùng
            List<Object[]> totals = thongKeDAO.getTongHopLichSu(nam, maKho);
            if (!totals.isEmpty() && totals.get(0) != null) {
                Object[] t = totals.get(0);
                Map<String, Object> grandTotal = new HashMap<>();
                grandTotal.put("tongSL", t[3]);   // Tùy index Procedure trả ra (3 = Tổng Cuối)
                grandTotal.put("tongTien", t[4]); // (4 = Tổng Tiền) - Đã giữ lại tính tiền!
                response.put("grandTotal", grandTotal);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải lịch sử: " + e.getMessage());
        }
    }

    // =================================================================
    // 4. API KIỂM TRA TRẠNG THÁI ĐÃ CHỐT SỔ CHƯA (Dùng để Validate UI)
    // =================================================================
    @GetMapping("/check-chot-so")
    public ResponseEntity<?> checkDaChotSo(
            @RequestParam Integer nam,
            @RequestParam Integer maKho
    ) {
        try {
            int count = thongKeDAO.demSoLuongBanGhiChotSo(nam, maKho);
            return ResponseEntity.ok(count > 0); // Trả về true nếu đã chốt, false nếu chưa
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi kiểm tra trạng thái chốt sổ: " + e.getMessage());
        }
    }


    // =================================================================
    // CÁC HÀM TIỆN ÍCH (HELPER METHODS) - DÙNG ĐỂ TỐI ƯU (DRY)
    // =================================================================

    /**
     * Hàm dùng chung: Lấy tên kho để đỡ lặp lại code kiểm tra (maKho == 0)
     */
    private String resolveTenKho(Integer maKho) {
        if (maKho == null || maKho == 0) return "Tất cả các kho";
        return thongKeDAO.getTenKhoById(maKho);
    }

    /**
     * Hàm dùng chung: Đóng gói các thuộc tính cơ bản của Response (Tên kho, Data, Current Page)
     */
    private Map<String, Object> createBaseResponse(Integer maKho, List<BaoCaoXuatNhapTonDTO> data, Integer page) {
        Map<String, Object> response = new HashMap<>();
        response.put("tenKho", resolveTenKho(maKho));
        response.put("danhSachChiTiet", data);
        if (page != null) {
            response.put("currentPage", page);
        }
        return response;
    }

    /**
     * Hàm dùng chung: Ánh xạ mảng Object[] từ SQL sang danh sách DTO
     * ĐÃ BAO GỒM TÍNH TOÁN TIỀN (Thành Tiền, Giá Bình Quân)
     */
    private List<BaoCaoXuatNhapTonDTO> mapToDTOList(List<Object[]> rawResults) {
        List<BaoCaoXuatNhapTonDTO> listBaoCao = new ArrayList<>();
        if (rawResults == null || rawResults.isEmpty()) return listBaoCao;

        for (Object[] row : rawResults) {
            BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
            dto.setMaSP(toStringSafe(row[0]));

            // Gộp Tên SP và Trạng Thái (Mới/Cũ) để nhìn trên UI rõ ràng hơn
            String tenSP = toStringSafe(row[1]);
            String trangThai = toStringSafe(row[3]);
            dto.setTenSP(trangThai.isEmpty() ? tenSP : tenSP + " - " + trangThai);

            dto.setDonvitinh(toStringSafe(row[4]));

            // Map Số Lượng
            dto.setTonDau(toNumber(row[5]));
            dto.setNhapTrong(toNumber(row[6]));
            dto.setXuatTrong(toNumber(row[7]));
            dto.setTonCuoi(toNumber(row[8]));

            // Map Tiền & Tính Giá Bình Quân (Vì bạn yêu cầu giữ lại tiền)
            dto.setThanhTien(toBigDecimal(row[9]));

            if (dto.getTonCuoi() > 0 && dto.getThanhTien().compareTo(BigDecimal.ZERO) > 0) {
                // Giá Bình Quân = Tổng Tiền / Số Lượng Tồn Cuối (Làm tròn 2 chữ số)
                BigDecimal giaBQ = dto.getThanhTien().divide(BigDecimal.valueOf(dto.getTonCuoi()), 2, RoundingMode.HALF_UP);
                dto.setGiaBQ(giaBQ);
            } else {
                dto.setGiaBQ(BigDecimal.ZERO);
            }

            listBaoCao.add(dto);
        }
        return listBaoCao;
    }

    // =================================================================
    // 5. API XÁC THỰC MẬT KHẨU (Dành cho chức năng Chốt Sổ)
    // =================================================================
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        String username = payload.get("username");
        String rawPassword = payload.get("password"); // Mật khẩu nhập từ popup trên Vue

        try {
            // 1. Tìm tài khoản trong Database
            Optional<TaiKhoan> optionalTaiKhoan = taiKhoanDAO.findByTenTaiKhoan(username);

            if (!optionalTaiKhoan.isPresent()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy tài khoản!");
                return ResponseEntity.ok(response);
            }

            TaiKhoan taiKhoan = optionalTaiKhoan.get();

            // 2. SO SÁNH MẬT KHẨU BẰNG BCRYPT
            // Hàm matches(mật_khẩu_nhập_vào, mật_khẩu_đã_băm_trong_DB)
            boolean isMatch = passwordEncoder.matches(rawPassword, taiKhoan.getPassword());

            // 3. Trả về kết quả
            if (isMatch) {
                response.put("success", true);
                response.put("message", "Xác thực thành công!");
            } else {
                response.put("success", false);
                response.put("message", "Mật khẩu không chính xác!");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // --- CÁC HÀM ÉP KIỂU AN TOÀN TRÁNH NULL POINTER EXCEPTION ---

    private String toStringSafe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private Long toNumber(Object obj) {
        try {
            return obj == null ? 0L : Long.valueOf(obj.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object obj) {
        try {
            return obj == null ? BigDecimal.ZERO : new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}