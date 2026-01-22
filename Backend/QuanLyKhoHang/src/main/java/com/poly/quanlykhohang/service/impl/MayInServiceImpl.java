package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.MayInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MayInServiceImpl implements MayInService {

    private final MayInDAO mayInDAO;
    private final KhoDAO khoDAO;

    // [THAY ĐỔI] Dùng DAO của bảng chi tiết mới thay cho bảng Serial cũ
    private final ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    private final ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;

    @Override
    public MayIn timTheoSeri(String soSeri) {
        return mayInDAO.findBySoSeri(soSeri)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy máy có seri: " + soSeri));
    }

    @Override
    public List<MayIn> timMayTonKhoTheoSanPham(String maSP) {
        // Lưu ý: Bạn cần chắc chắn MayInDAO đã có hàm này
        // Nếu báo lỗi, hãy kiểm tra lại file MayInDAO
        return mayInDAO.findAvailableMachinesByProduct(maSP);
    }

    @Override
    public Map<String, Object> traCuuLichSuMay(String soSeri) {
        Map<String, Object> history = new HashMap<>();

        // 1. Lấy thông tin hiện tại
        MayIn mayIn = timTheoSeri(soSeri);
        history.put("thongTinMay", mayIn);

        // 2. Tìm nguồn gốc Nhập (Dựa vào bảng CTPhieuNhap mới)
        Optional<ChiTietPhieuNhap> nhapEntry = chiTietPhieuNhapDAO.findByMayIn(mayIn);

        if (nhapEntry.isPresent()) {
            PhieuNhap pn = nhapEntry.get().getPhieuNhap();
            history.put("ngayNhap", pn.getNgayNhap());

            if (pn.getNhaCungCap() != null) {
                history.put("nhaCungCap", pn.getNhaCungCap().getTenDonVi());
            }
            history.put("phieuNhap", pn.getSoPhieu());
        } else {
            // Fallback: Nếu không tìm thấy trong chi tiết (do dữ liệu cũ), thử lấy từ trường SoPhieuNhap trong MayIn
            if (mayIn.getSoPhieuNhap() != null) {
                history.put("phieuNhap", mayIn.getSoPhieuNhap());
                history.put("ghiChu", "Dữ liệu nhập từ hệ thống cũ");
            }
        }

        // 3. Tìm lịch sử Xuất (Dựa vào bảng CTPhieuXuat mới)
        Optional<ChiTietPhieuXuat> xuatEntry = chiTietPhieuXuatDAO.findByMayIn(mayIn);

        if (xuatEntry.isPresent()) {
            PhieuXuat px = xuatEntry.get().getPhieuXuat();
            history.put("ngayXuat", px.getNgayXuat());

            if (px.getKhachHang() != null) {
                history.put("khachHang", px.getKhachHang().getTenDonVi());
            }
            history.put("phieuXuat", px.getSoPhieu());

            // Logic tính bảo hành (Ví dụ 12 tháng)
            LocalDateTime hanBaoHanh = px.getNgayXuat().plusMonths(12);
            history.put("hanBaoHanh", hanBaoHanh);

            if (LocalDateTime.now().isBefore(hanBaoHanh)) {
                history.put("trangThaiBaoHanh", "CÒN BẢO HÀNH");
            } else {
                history.put("trangThaiBaoHanh", "HẾT BẢO HÀNH");
            }
        } else {
            history.put("trangThaiBaoHanh", "CHƯA BÁN");
        }

        return history;
    }

    @Override
    public void capNhatTrangThai(String soSeri, Integer trangThaiMoi) {
        MayIn mayIn = timTheoSeri(soSeri);
        mayIn.setTrangThai(trangThaiMoi);
        mayInDAO.save(mayIn);
    }

    @Override
    public void chuyenKho(String soSeri, Integer maKhoMoi) {
        MayIn mayIn = timTheoSeri(soSeri);
        Kho khoMoi = khoDAO.findById(maKhoMoi)
                .orElseThrow(() -> new RuntimeException("Kho đích không tồn tại"));

        mayIn.setKho(khoMoi);
        mayInDAO.save(mayIn);
    }
}