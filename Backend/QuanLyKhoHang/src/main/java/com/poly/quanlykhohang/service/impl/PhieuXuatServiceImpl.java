package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.PhieuXuatDAO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.PhieuXuatService;
import org.springframework.transaction.annotation.Transactional; // Dùng spring transaction
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhieuXuatServiceImpl implements PhieuXuatService {

    private final PhieuXuatDAO phieuXuatDAO;
    private final MayInDAO mayInDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat taoPhieuXuat(PhieuXuat phieuXuat) {
        // 1. Validate cơ bản
        if (phieuXuat.getSoPhieu() == null || phieuXuat.getSoPhieu().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số phiếu xuất!");
        }

        if (phieuXuatDAO.existsById(phieuXuat.getSoPhieu())) {
            throw new RuntimeException("Phiếu xuất " + phieuXuat.getSoPhieu() + " đã tồn tại!");
        }

        if (phieuXuat.getNgayXuat() == null) {
            phieuXuat.setNgayXuat(LocalDateTime.now());
        }

        // Lưu Header trước để có ID gán cho con
        PhieuXuat savedPhieu = phieuXuatDAO.save(phieuXuat);

        // 2. Duyệt danh sách chi tiết (Mỗi dòng là 1 máy - Logic Mới)
        if (phieuXuat.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuXuat ctXuat : phieuXuat.getDanhSachChiTiet()) {

                // Gán phiếu cha
                ctXuat.setPhieuXuat(savedPhieu);

                // --- LOGIC TRỪ KHO ---
                // Lấy thông tin máy từ dòng chi tiết (Vì giờ map trực tiếp 1-1)
                MayIn mayInInput = ctXuat.getMayIn();

                if (mayInInput == null || mayInInput.getMaMay() == null) {
                    throw new RuntimeException("Chi tiết xuất thiếu thông tin máy!");
                }

                // Tìm máy trong kho theo ID
                MayIn mayInKho = mayInDAO.findById(mayInInput.getMaMay())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy máy: " + mayInInput.getMaMay()));

                // Kiểm tra trạng thái
                if (mayInKho.getTrangThai() != 1) {
                    throw new RuntimeException("Máy " + mayInKho.getSoSeri() + " không khả dụng (Đã bán hoặc hỏng)!");
                }

                // Cập nhật trạng thái máy sang Đã Bán (2)
                mayInKho.setTrangThai(2);
                mayInDAO.save(mayInKho);

                // Cập nhật lại đối tượng máy trong chi tiết (để lưu DB chuẩn)
                ctXuat.setMayIn(mayInKho);
            }
        }

        // Lưu lại lần nữa để cập nhật danh sách chi tiết (Cascade)
        return phieuXuatDAO.save(savedPhieu);
    }
}