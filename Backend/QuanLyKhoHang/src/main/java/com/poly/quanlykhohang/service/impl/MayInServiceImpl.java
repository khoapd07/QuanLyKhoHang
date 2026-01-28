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
    private final ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    private final ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;

    @Override
    public MayIn timTheoSeri(String soSeri) {
        return mayInDAO.findBySoSeri(soSeri)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy máy có seri: " + soSeri));
    }

    @Override
    public List<MayIn> timMayTonKhoTheoSanPham(String maSP) {
        return mayInDAO.findAvailableMachinesByProduct(maSP);
    }

    @Override
    public Map<String, Object> traCuuLichSuMay(String soSeri) {
        Map<String, Object> history = new HashMap<>();

        // 1. Lấy thông tin máy
        MayIn mayIn = timTheoSeri(soSeri);
        history.put("thongTinMay", mayIn);

        // [MỚI] Hiển thị Hãng thay vì Nhà Cung Cấp
        if (mayIn.getSanPham() != null && mayIn.getSanPham().getHangSanXuat() != null) {
            history.put("hangSanXuat", mayIn.getSanPham().getHangSanXuat().getTenHang());
        }

        // 2. Tìm thông tin NHẬP
        Optional<ChiTietPhieuNhap> nhapEntry = chiTietPhieuNhapDAO.findByMayIn(mayIn);

        if (nhapEntry.isPresent()) {
            PhieuNhap pn = nhapEntry.get().getPhieuNhap();
            history.put("ngayNhap", pn.getNgayNhap());
            history.put("phieuNhap", pn.getSoPhieu());

            // [ĐÃ SỬA] Đã xóa logic lấy Nhà Cung Cấp ở đây vì PhieuNhap không còn cột đó
        } else {
            // Fallback: Dùng cột SoPhieuNhap trong bảng DMMay nếu bảng chi tiết không tìm thấy
            history.put("phieuNhap", mayIn.getSoPhieuNhap());
        }

        // 3. Tìm thông tin XUẤT
        Optional<ChiTietPhieuXuat> xuatEntry = chiTietPhieuXuatDAO.findByMayIn(mayIn);

        if (xuatEntry.isPresent()) {
            PhieuXuat px = xuatEntry.get().getPhieuXuat();
            history.put("ngayXuat", px.getNgayXuat());
            history.put("phieuXuat", px.getSoPhieu());

            // PhieuXuat vẫn còn KhachHang (MaDonVi) nên giữ nguyên logic này
            if (px.getKhachHang() != null) {
                history.put("khachHang", px.getKhachHang().getTenDonVi());
            }

            // Bảo hành 12 tháng
            LocalDateTime hanBaoHanh = px.getNgayXuat().plusMonths(12);
            history.put("hanBaoHanh", hanBaoHanh);
            history.put("trangThaiBaoHanh", LocalDateTime.now().isBefore(hanBaoHanh) ? "CÒN BẢO HÀNH" : "HẾT BẢO HÀNH");
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