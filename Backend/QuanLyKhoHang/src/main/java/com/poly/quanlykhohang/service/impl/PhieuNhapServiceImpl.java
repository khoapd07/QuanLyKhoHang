package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.PhieuNhapDAO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.PhieuNhapService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhieuNhapServiceImpl implements PhieuNhapService {

    private final PhieuNhapDAO phieuNhapDAO;
    private final MayInDAO mayInDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap taoPhieuNhap(PhieuNhap phieuNhap) {
        if (phieuNhap.getSoPhieu() == null) {
            throw new RuntimeException("Thiếu số phiếu!");
        }

        if (phieuNhap.getNgayNhap() == null) {
            phieuNhap.setNgayNhap(LocalDateTime.now());
        }

        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);

        // Logic Mới: 1 Chi tiết = 1 Máy
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ctNhap : phieuNhap.getDanhSachChiTiet()) {
                ctNhap.setPhieuNhap(savedPhieu);

                // Xử lý thông tin máy
                MayIn mayIn = ctNhap.getMayIn();
                if (mayIn != null) {
                    // Nếu máy chưa có trong DB thì set thông tin để tạo mới
                    if (!mayInDAO.existsById(mayIn.getMaMay())) {
                        mayIn.setNgayTao(LocalDateTime.now());
                        mayIn.setTrangThai(1); // Tồn kho
                        mayIn.setKho(savedPhieu.getKhoNhap());
                        mayIn.setSoPhieuNhap(savedPhieu.getSoPhieu());
                        mayInDAO.save(mayIn);
                    }
                }
            }
        }

        return phieuNhapDAO.save(savedPhieu);
    }
}