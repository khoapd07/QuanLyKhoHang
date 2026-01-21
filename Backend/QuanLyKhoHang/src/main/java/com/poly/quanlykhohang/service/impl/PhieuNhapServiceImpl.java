package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.PhieuNhapDAO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.PhieuNhapService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhieuNhapServiceImpl implements PhieuNhapService {

    private final PhieuNhapDAO phieuNhapDAO;
    private final MayInDAO mayInDAO;

    @Override
    @Transactional // Quan trọng: Nếu lỗi ở bước nào, Rollback toàn bộ
    public PhieuNhap taoPhieuNhap(PhieuNhap phieuNhap) {
        if (phieuNhap.getSoPhieu() == null || phieuNhap.getSoPhieu().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số phiếu!");
        }

        // 2. LOGIC QUAN TRỌNG: Kiểm tra trùng mã phiếu
        if (phieuNhapDAO.existsById(phieuNhap.getSoPhieu())) {
            throw new RuntimeException("Số phiếu " + phieuNhap.getSoPhieu() + " đã tồn tại! Vui lòng kiểm tra lại.");
        }
        // 1. Set ngày nhập hiện tại nếu chưa có
        if (phieuNhap.getNgayNhap() == null) {
            phieuNhap.setNgayNhap(LocalDateTime.now());
        }

        // 2. Duyệt qua danh sách chi tiết (Cấp 2)
        for (ChiTietPhieuNhap ctNhap : phieuNhap.getDanhSachChiTiet()) {
            ctNhap.setPhieuNhap(phieuNhap); // Link ngược về cha

            // 3. Duyệt qua danh sách Seri (Cấp 3)
            for (ChiTietNhapSeri seriEntry : ctNhap.getDanhSachSeri()) {
                seriEntry.setChiTietPhieuNhap(ctNhap); // Link ngược về cấp 2

                // --- LOGIC QUAN TRỌNG: TẠO MÁY VÀO KHO ---
                MayIn mayMoi = seriEntry.getMayIn();

                // Kiểm tra xem máy này đã tồn tại chưa (tránh nhập trùng seri)
                if(mayInDAO.existsById(mayMoi.getMaMay())) {
                    throw new RuntimeException("Lỗi: Mã seri " + mayMoi.getMaMay() + " đã tồn tại trong hệ thống!");
                }

                // Thiết lập thông tin mặc định cho máy mới
                mayMoi.setSanPham(ctNhap.getSanPham()); // Máy thuộc dòng SP này
                mayMoi.setKho(phieuNhap.getKhoNhap());  // Máy nằm ở kho nhập
                mayMoi.setNgayTao(LocalDateTime.now());
                mayMoi.setTrangThai(1); // 1 = Đang tồn kho (Available)

                // Lưu máy vào bảng DMMay
                mayInDAO.save(mayMoi);
            }
        }

        // 4. Lưu toàn bộ Phiếu Nhập (JPA sẽ tự lưu các cấp con nhờ CascadeType.ALL)
        return phieuNhapDAO.save(phieuNhap);
    }
}