package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.PhieuXuatDAO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.PhieuXuatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhieuXuatServiceImpl implements PhieuXuatService {

    private final PhieuXuatDAO phieuXuatDAO;
    private final MayInDAO mayInDAO;

    @Override
    @Transactional
    public PhieuXuat taoPhieuXuat(PhieuXuat phieuXuat) {
        if (phieuXuat.getSoPhieu() == null || phieuXuat.getSoPhieu().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số phiếu xuất!");
        }

        // 2. Kiểm tra trùng lặp (Tránh lỗi DB)
        if (phieuXuatDAO.existsById(phieuXuat.getSoPhieu())) {
            throw new RuntimeException("Phiếu xuất " + phieuXuat.getSoPhieu() + " đã tồn tại!");
        }
        // 1. Set ngày xuất
        if (phieuXuat.getNgayXuat() == null) {
            phieuXuat.setNgayXuat(LocalDateTime.now());
        }

        // 2. Duyệt chi tiết dòng (Cấp 2)
        for (ChiTietPhieuXuat ctXuat : phieuXuat.getDanhSachChiTiet()) {
            ctXuat.setPhieuXuat(phieuXuat);

            // 3. Duyệt danh sách Seri cần bán (Cấp 3)
            for (ChiTietXuatSeri seriEntry : ctXuat.getDanhSachSeri()) {
                seriEntry.setChiTietPhieuXuat(ctXuat);

                // --- LOGIC QUAN TRỌNG: TRỪ KHO ---
                // Tìm máy trong kho theo mã Seri
                String maSeriCanBan = seriEntry.getMayIn().getMaMay();
                MayIn mayInKho = mayInDAO.findById(maSeriCanBan)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy máy có seri: " + maSeriCanBan));

                // Kiểm tra máy có bán được không
                if (mayInKho.getTrangThai() != 1) {
                    throw new RuntimeException("Máy " + maSeriCanBan + " không khả dụng (Đã bán hoặc hỏng)!");
                }

                // Cập nhật trạng thái máy
                mayInKho.setTrangThai(2); // 2 = Đã bán (Sold)
                mayInKho.setKho(null); // Hoặc giữ nguyên kho để biết xuất từ đâu (tùy nghiệp vụ)

                // Lưu cập nhật trạng thái máy
                mayInDAO.save(mayInKho);

                // Gán đối tượng máy thực tế vào chi tiết xuất để lưu lịch sử
                seriEntry.setMayIn(mayInKho);
            }
        }

        // 4. Lưu phiếu xuất
        return phieuXuatDAO.save(phieuXuat);
    }
}