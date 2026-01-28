package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GiaoDichKhoService {

    @Autowired private PhieuNhapDAO phieuNhapDAO;
    @Autowired private ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    @Autowired private PhieuXuatDAO phieuXuatDAO;
    @Autowired private ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;
    @Autowired private DonViDAO donViDAO;
    @Autowired private KhoDAO khoDAO;
    @Autowired private SanPhamDAO sanPhamDAO;
    @Autowired private MayInDAO mayInDAO;

    // ================== LẤY DANH SÁCH ==================
    public List<PhieuNhap> layDanhSachPhieuNhapTomTat() {
        List<PhieuNhap> list = phieuNhapDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));
        list.forEach(pn -> pn.setDanhSachChiTiet(null));
        return list;
    }

    public PhieuNhap layPhieuNhapChiTiet(String soPhieu) {
        return phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
    }

    public List<PhieuXuat> layDanhSachPhieuXuatTomTat() {
        List<PhieuXuat> list = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        list.forEach(px -> px.setDanhSachChiTiet(null));
        return list;
    }

    public PhieuXuat layPhieuXuatChiTiet(String soPhieu) {
        return phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
    }

    // ================== 1. NHẬP KHO (LOGIC MỚI) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap nhapKho(PhieuNhapDTO dto) {
        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setSoPhieu("PN-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        phieuNhap.setNgayNhap(LocalDateTime.now());
        phieuNhap.setGhiChu(dto.getGhiChu());

//        DonVi ncc = donViDAO.findById(dto.getMaDonVi())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));
//        phieuNhap.setNhaCungCap(ncc);

        Kho kho = khoDAO.findById(dto.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Kho"));
        phieuNhap.setKhoNhap(kho);

        // Lưu tạm phiếu để có ID
        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);

        List<ChiTietPhieuNhap> listChiTiet = new ArrayList<>();
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        if (dto.getChiTietPhieuNhap() != null) {
            for (ChiTietNhapDTO ctDto : dto.getChiTietPhieuNhap()) {
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {
                        // 1. Tạo máy in mới
                        if (mayInDAO.findBySoSeri(seriStr).isPresent()) {
                            throw new RuntimeException("Số seri " + seriStr + " đã tồn tại!");
                        }

                        MayIn mayMoi = new MayIn();
                        // Tạo ID máy: M + Seri (cắt ngắn nếu cần)
                        String maMayGen = "M-" + seriStr;
                        mayMoi.setMaMay(maMayGen.length() > 50 ? maMayGen.substring(0, 50) : maMayGen);

                        mayMoi.setSoSeri(seriStr);
                        mayMoi.setSanPham(sp);
                        mayMoi.setKho(kho);
                        mayMoi.setSoPhieuNhap(savedPhieu.getSoPhieu());
                        mayMoi.setTrangThai(1); // 1 = Tồn kho
                        mayMoi.setNgayTao(LocalDateTime.now());
                        // Copy MaHang từ sản phẩm sang máy (để khớp DB)
                        if(sp.getHangSanXuat() != null) mayMoi.setHangSanXuat(sp.getHangSanXuat());

                        MayIn maySaved = mayInDAO.save(mayMoi);

                        // 2. Tạo chi tiết phiếu nhập
                        ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                        ctEntity.setPhieuNhap(savedPhieu);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(maySaved);
                        ctEntity.setDonGia(ctDto.getDonGia());
                        ctEntity.setGhiChu(ctDto.getGhiChu());

                        chiTietPhieuNhapDAO.save(ctEntity);
                        listChiTiet.add(ctEntity);

                        // Cộng dồn tổng
                        tongSoLuong++;
                        if (ctDto.getDonGia() != null) {
                            tongTien = tongTien.add(ctDto.getDonGia());
                        }
                    }

                    // Cập nhật số lượng tồn tổng trong bảng Sản Phẩm (Snapshot)
                    sp.setSoLuong((sp.getSoLuong() == null ? 0 : sp.getSoLuong()) + ctDto.getDanhSachSeri().size());
                    sanPhamDAO.save(sp);
                }
            }
        }

        // Cập nhật lại tổng tiền và số lượng cho phiếu
        savedPhieu.setTongSoLuong(tongSoLuong);
        savedPhieu.setTongTien(tongTien);
        savedPhieu.setDanhSachChiTiet(listChiTiet);

        return phieuNhapDAO.save(savedPhieu);
    }

    // ================== 2. XUẤT KHO (LOGIC MỚI) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat xuatKho(PhieuXuatDTO dto) {
        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setSoPhieu("PX-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setGhiChu(dto.getGhiChu());

//        DonVi khach = donViDAO.findById(dto.getMaDonVi())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy Khách Hàng"));
//        phieuXuat.set(khach);

        Kho kho = khoDAO.findById(dto.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Kho Xuất"));
        phieuXuat.setKhoXuat(kho);

        PhieuXuat savedPhieu = phieuXuatDAO.save(phieuXuat);

        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        if (dto.getChiTietPhieuXuat() != null) {
            for (ChiTietXuatDTO ctDto : dto.getChiTietPhieuXuat()) {
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {
                        // 1. Tìm máy và update trạng thái
                        MayIn mayInDb = mayInDAO.findBySoSeri(seriStr)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy máy: " + seriStr));

                        if (mayInDb.getTrangThai() != 1) {
                            throw new RuntimeException("Máy " + seriStr + " không khả dụng (Trạng thái: " + mayInDb.getTrangThai() + ")");
                        }

                        // Kiểm tra xem máy có đúng ở kho xuất không
                        if (mayInDb.getKho() != null && !mayInDb.getKho().getMaKho().equals(kho.getMaKho())) {
                            throw new RuntimeException("Máy " + seriStr + " đang ở kho khác, không thể xuất từ " + kho.getTenKho());
                        }

                        mayInDb.setTrangThai(2); // 2 = Đã bán
                        mayInDAO.save(mayInDb);

                        // 2. Tạo dòng Chi Tiết Phiếu Xuất
                        ChiTietPhieuXuat ctEntity = new ChiTietPhieuXuat();
                        ctEntity.setPhieuXuat(savedPhieu);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(mayInDb);
                        ctEntity.setDonGia(ctDto.getDonGia());

                        chiTietPhieuXuatDAO.save(ctEntity);

                        tongSoLuong++;
                        if (ctDto.getDonGia() != null) {
                            tongTien = tongTien.add(ctDto.getDonGia());
                        }
                    }

                    // Trừ tồn kho tổng trong bảng Sản Phẩm
                    int currentQty = sp.getSoLuong() == null ? 0 : sp.getSoLuong();
                    sp.setSoLuong(Math.max(0, currentQty - ctDto.getDanhSachSeri().size()));
                    sanPhamDAO.save(sp);
                }
            }
        }

        savedPhieu.setTongSoLuong(tongSoLuong);
        savedPhieu.setTongTien(tongTien);
        return phieuXuatDAO.save(savedPhieu);
    }

    // ================== CẬP NHẬT & XÓA (Giữ nguyên logic nhưng update hàm) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap capNhatPhieuNhap(String soPhieu, PhieuNhapDTO dto) {
        PhieuNhap phieuCu = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
        phieuCu.setGhiChu(dto.getGhiChu());
        // Logic update khác nếu cần...
        return phieuNhapDAO.save(phieuCu);
    }

    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat capNhatPhieuXuat(String soPhieu, PhieuXuatDTO dto) {
        PhieuXuat phieuCu = phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
        phieuCu.setGhiChu(dto.getGhiChu());
        return phieuXuatDAO.save(phieuCu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));

        // Kiểm tra ràng buộc
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null && mayIn.getTrangThai() != 1) {
                    throw new RuntimeException("Không thể xóa! Máy " + mayIn.getSoSeri() + " đã bán.");
                }
            }
        }
        // Xóa máy in (Hard delete)
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                if(ct.getMayIn() != null) {
                    // Cần cập nhật lại số lượng tồn SP
                    SanPham sp = ct.getSanPham();
                    sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
                    sanPhamDAO.save(sp);
                    mayInDAO.delete(ct.getMayIn());
                }
            }
        }
        phieuNhapDAO.delete(phieuNhap);
    }

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuXuat(String soPhieu) {
        PhieuXuat phieuXuat = phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất: " + soPhieu));

        if (phieuXuat.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuXuat ct : phieuXuat.getDanhSachChiTiet()) {
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null) {
                    // Trả trạng thái về 1 (Tồn kho)
                    mayIn.setTrangThai(1);
                    mayInDAO.save(mayIn);

                    // Cộng lại số lượng tồn SP
                    SanPham sp = ct.getSanPham();
                    sp.setSoLuong((sp.getSoLuong() == null ? 0 : sp.getSoLuong()) + 1);
                    sanPhamDAO.save(sp);
                }
            }
        }
        phieuXuatDAO.delete(phieuXuat);
    }
}