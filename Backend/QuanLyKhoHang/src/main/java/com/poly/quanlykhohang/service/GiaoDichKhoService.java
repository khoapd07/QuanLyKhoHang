package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GiaoDichKhoService { // <--- ĐÃ ĐỔI TÊN TỪ KhoService CŨ

    @Autowired private PhieuNhapDAO phieuNhapDAO;
    @Autowired private ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    @Autowired private ChiTietNhapSeriDAO chiTietNhapSeriDAO;

    @Autowired private PhieuXuatDAO phieuXuatDAO;
    @Autowired private ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;
    @Autowired private ChiTietXuatSeriDAO chiTietXuatSeriDAO;

    @Autowired private DonViDAO donViDAO;
    @Autowired private KhoDAO khoDAO;
    @Autowired private SanPhamDAO sanPhamDAO;
    @Autowired private MayInDAO mayInDAO;

    // ================== 1. NHẬP KHO ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap nhapKho(PhieuNhapDTO dto) {
        PhieuNhap phieuNhap = new PhieuNhap();
        // Tự sinh mã phiếu
        phieuNhap.setSoPhieu("PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        phieuNhap.setNgayNhap(LocalDateTime.now());
        phieuNhap.setGhiChu(dto.getGhiChu());

        DonVi ncc = donViDAO.findById(dto.getMaDonVi())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC: " + dto.getMaDonVi()));
        phieuNhap.setNhaCungCap(ncc);

        Kho kho = khoDAO.findById(dto.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Kho: " + dto.getMaKho()));
        phieuNhap.setKhoNhap(kho);

        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);

        if (dto.getChiTietPhieuNhap() != null) {
            for (ChiTietNhapDTO ctDto : dto.getChiTietPhieuNhap()) {
                ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                ctEntity.setSanPham(sp);
                ctEntity.setSoLuong(ctDto.getSoLuong());
                ctEntity.setDonGia(ctDto.getDonGia());
                ctEntity.setPhieuNhap(savedPhieu);
                ctEntity.setGhiChu(ctDto.getGhiChu());

                ChiTietPhieuNhap savedChiTiet = chiTietPhieuNhapDAO.save(ctEntity);

                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {
                        if(mayInDAO.findBySoSeri(seriStr).isPresent()){
                            throw new RuntimeException("Số seri " + seriStr + " đã tồn tại!");
                        }

                        MayIn mayMoi = new MayIn();
                        // Fix độ dài ID
                        String maMayGen = "M-" + seriStr;
                        if (maMayGen.length() > 50) maMayGen = maMayGen.substring(0, 50);

                        mayMoi.setMaMay(maMayGen);
                        mayMoi.setSoSeri(seriStr);
                        mayMoi.setSanPham(sp);
                        mayMoi.setKho(kho);
                        mayMoi.setTrangThai(1);
                        mayMoi.setNgayTao(LocalDateTime.now());
                        mayInDAO.save(mayMoi);

                        ChiTietNhapSeri seriLog = new ChiTietNhapSeri();
                        seriLog.setMayIn(mayMoi);
                        seriLog.setChiTietPhieuNhap(savedChiTiet);
                        chiTietNhapSeriDAO.save(seriLog);
                    }
                }
            }
        }
        return savedPhieu;
    }

    // ================== 2. XUẤT KHO ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat xuatKho(PhieuXuatDTO dto) {
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setSoPhieu("PX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setGhiChu(dto.getGhiChu());

        DonVi khach = donViDAO.findById(dto.getMaDonVi())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khách Hàng"));
        phieuXuat.setKhachHang(khach);

        Kho kho = khoDAO.findById(dto.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Kho Xuất"));
        phieuXuat.setKhoXuat(kho);

        PhieuXuat savedPhieu = phieuXuatDAO.save(phieuXuat);

        if (dto.getChiTietPhieuXuat() != null) {
            for (ChiTietXuatDTO ctDto : dto.getChiTietPhieuXuat()) {
                ChiTietPhieuXuat ctEntity = new ChiTietPhieuXuat();
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                ctEntity.setSanPham(sp);
                ctEntity.setSoLuong(ctDto.getSoLuong());
                ctEntity.setDonGia(ctDto.getDonGia());
                ctEntity.setPhieuXuat(savedPhieu);

                ChiTietPhieuXuat savedChiTiet = chiTietPhieuXuatDAO.save(ctEntity);

                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {
                        MayIn mayInDb = mayInDAO.findBySoSeri(seriStr)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy máy: " + seriStr));

                        if (mayInDb.getTrangThai() != 1) {
                            throw new RuntimeException("Máy không khả dụng.");
                        }

                        mayInDb.setTrangThai(2); // Đã bán
                        mayInDAO.save(mayInDb);

                        ChiTietXuatSeri seriLog = new ChiTietXuatSeri();
                        seriLog.setMayIn(mayInDb);
                        seriLog.setChiTietPhieuXuat(savedChiTiet);
                        chiTietXuatSeriDAO.save(seriLog);
                    }
                }
            }
        }
        return savedPhieu;
    }
}