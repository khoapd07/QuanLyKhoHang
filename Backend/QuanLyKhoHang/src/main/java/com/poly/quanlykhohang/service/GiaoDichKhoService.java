package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GiaoDichKhoService {

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

    // === MỚI: LẤY DANH SÁCH TÓM TẮT (Ẩn chi tiết để JSON gọn nhẹ) ===
    public List<PhieuNhap> layDanhSachPhieuNhapTomTat() {
        // 1. Lấy tất cả phiếu, sắp xếp mới nhất lên đầu
        List<PhieuNhap> list = phieuNhapDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));

        // 2. Duyệt qua từng phiếu và SET DANH SÁCH CHI TIẾT = NULL
        // Mục đích: Khi trả về JSON, phần này sẽ biến mất hoặc null, giúp danh sách gọn gàng
        for (PhieuNhap pn : list) {
            pn.setDanhSachChiTiet(null);
        }
        return list;
    }

    // === MỚI: LẤY CHI TIẾT 1 PHIẾU (Hiện đầy đủ) ===
    public PhieuNhap layPhieuNhapChiTiet(String soPhieu) {
        return phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
        // Mặc định Hibernate sẽ load chi tiết khi được gọi, nên trả về nguyên object là có đủ data
    }

    // ================== 1. NHẬP KHO (Giữ nguyên logic cũ) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap nhapKho(PhieuNhapDTO dto) {
        PhieuNhap phieuNhap = new PhieuNhap();
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
        List<ChiTietPhieuNhap> listChiTiet = new ArrayList<>(); // List tạm để hiển thị lại ngay sau khi lưu

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
                listChiTiet.add(savedChiTiet);

                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {
                        if(mayInDAO.findBySoSeri(seriStr).isPresent()){
                            throw new RuntimeException("Số seri " + seriStr + " đã tồn tại!");
                        }

                        MayIn mayMoi = new MayIn();
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
        savedPhieu.setDanhSachChiTiet(listChiTiet); // Gán lại để trả về response cho đẹp
        return savedPhieu;
    }

    // ================== 2. XUẤT KHO (Giữ nguyên) ==================
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

    // ================== CẬP NHẬT PHIẾU (Giữ nguyên) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap capNhatPhieuNhap(String soPhieu, PhieuNhapDTO dto) {
        PhieuNhap phieuCu = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + soPhieu));

        phieuCu.setGhiChu(dto.getGhiChu());

        if (dto.getMaDonVi() != null) {
            DonVi ncc = donViDAO.findById(dto.getMaDonVi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC: " + dto.getMaDonVi()));
            phieuCu.setNhaCungCap(ncc);
        }
        return phieuNhapDAO.save(phieuCu);
    }

    // ================== XÓA PHIẾU (Giữ nguyên) ==================
    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập: " + soPhieu));

        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                if (ct.getDanhSachSeri() != null) {
                    for (ChiTietNhapSeri seriLog : ct.getDanhSachSeri()) {
                        MayIn mayIn = seriLog.getMayIn();
                        if (mayIn.getTrangThai() != 1) {
                            throw new RuntimeException("Không thể xóa! Máy " + mayIn.getSoSeri() + " đã bán.");
                        }
                    }
                }
            }
        }
        // Xóa máy trước
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                if (ct.getDanhSachSeri() != null) {
                    for (ChiTietNhapSeri seriLog : ct.getDanhSachSeri()) {
                        mayInDAO.delete(seriLog.getMayIn());
                    }
                }
            }
        }
        phieuNhapDAO.delete(phieuNhap);
    }
}