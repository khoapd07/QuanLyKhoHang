package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // === LẤY DANH SÁCH TÓM TẮT ===
    public List<PhieuNhap> layDanhSachPhieuNhapTomTat() {
        List<PhieuNhap> list = phieuNhapDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));
        // Ẩn chi tiết để JSON nhẹ
        for (PhieuNhap pn : list) {
            pn.setDanhSachChiTiet(null);
        }
        return list;
    }

    // === LẤY CHI TIẾT ===
    public PhieuNhap layPhieuNhapChiTiet(String soPhieu) {
        return phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));
    }

    // ================== 1. NHẬP KHO (LOGIC MỚI) ==================
    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap nhapKho(PhieuNhapDTO dto) {
        // 1. Tạo Header Phiếu Nhập
        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setSoPhieu(Year.now() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        phieuNhap.setNgayNhap(LocalDateTime.now());
        phieuNhap.setGhiChu(dto.getGhiChu());

        DonVi ncc = donViDAO.findById(dto.getMaDonVi())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC: " + dto.getMaDonVi()));
        phieuNhap.setNhaCungCap(ncc);

        Kho kho = khoDAO.findById(dto.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Kho: " + dto.getMaKho()));
        phieuNhap.setKhoNhap(kho);

        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);
        List<ChiTietPhieuNhap> listChiTiet = new ArrayList<>();

        // 2. Duyệt qua DTO gửi lên
        if (dto.getChiTietPhieuNhap() != null) {
            for (ChiTietNhapDTO ctDto : dto.getChiTietPhieuNhap()) {
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                // Với mỗi Serial trong danh sách -> Tạo 1 dòng Chi Tiết + 1 Máy In
                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {

                        // A. Tạo Máy In trước
                        if (mayInDAO.findBySoSeri(seriStr).isPresent()) {
                            throw new RuntimeException("Số seri " + seriStr + " đã tồn tại!");
                        }

                        MayIn mayMoi = new MayIn();
                        String maMayGen = "M-" + seriStr;
                        if (maMayGen.length() > 50) maMayGen = maMayGen.substring(0, 50);

                        mayMoi.setMaMay(maMayGen);
                        mayMoi.setSoSeri(seriStr);
                        mayMoi.setSanPham(sp);
                        mayMoi.setKho(kho);
                        mayMoi.setSoPhieuNhap(savedPhieu.getSoPhieu()); // Lưu vết phiếu nhập
                        mayMoi.setTrangThai(1); // 1 = Tồn kho
                        mayMoi.setNgayTao(LocalDateTime.now());

                        MayIn maySaved = mayInDAO.save(mayMoi);

                        // B. Tạo dòng Chi Tiết Phiếu Nhập (Link trực tiếp tới Máy)
                        ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                        ctEntity.setPhieuNhap(savedPhieu);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(maySaved); // <--- QUAN TRỌNG: Link 1-1
                        ctEntity.setDonGia(ctDto.getDonGia());
                        ctEntity.setGhiChu(ctDto.getGhiChu());

                        ChiTietPhieuNhap savedChiTiet = chiTietPhieuNhapDAO.save(ctEntity);
                        listChiTiet.add(savedChiTiet);
                    }
                }
            }
        }
        savedPhieu.setDanhSachChiTiet(listChiTiet);
        return savedPhieu;
    }

    // ================== 2. XUẤT KHO (LOGIC MỚI) ==================
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
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP())
                        .orElseThrow(() -> new RuntimeException("Lỗi SP: " + ctDto.getMaSP()));

                // Với mỗi Serial -> Tìm máy -> Update -> Tạo dòng chi tiết xuất
                if (ctDto.getDanhSachSeri() != null) {
                    for (String seriStr : ctDto.getDanhSachSeri()) {

                        // A. Tìm máy và update trạng thái
                        MayIn mayInDb = mayInDAO.findBySoSeri(seriStr)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy máy: " + seriStr));

                        if (mayInDb.getTrangThai() != 1) {
                            throw new RuntimeException("Máy " + seriStr + " không khả dụng.");
                        }
                        mayInDb.setTrangThai(2); // 2 = Đã bán
                        mayInDAO.save(mayInDb);

                        // B. Tạo dòng Chi Tiết Phiếu Xuất
                        ChiTietPhieuXuat ctEntity = new ChiTietPhieuXuat();
                        ctEntity.setPhieuXuat(savedPhieu);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(mayInDb); // <--- QUAN TRỌNG: Link 1-1
                        ctEntity.setDonGia(ctDto.getDonGia());

                        chiTietPhieuXuatDAO.save(ctEntity);
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));

        phieuCu.setGhiChu(dto.getGhiChu());
        if (dto.getMaDonVi() != null) {
            DonVi ncc = donViDAO.findById(dto.getMaDonVi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy NCC"));
            phieuCu.setNhaCungCap(ncc);
        }
        return phieuNhapDAO.save(phieuCu);
    }

    // ================== XÓA PHIẾU (LOGIC MỚI) ==================
    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu: " + soPhieu));

        // 1. Kiểm tra: Tất cả máy trong phiếu này phải còn tồn kho (Trạng thái = 1)
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                // Với DB mới, ta lấy trực tiếp MayIn từ ChiTietPhieuNhap
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null && mayIn.getTrangThai() != 1) {
                    throw new RuntimeException("Không thể xóa! Máy " + mayIn.getSoSeri() + " đã bán.");
                }
            }
        }

        // 2. Xóa Máy In trước (Vì MayIn ko có Cascade delete từ phía PhieuNhap)
        if (phieuNhap.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null) {
                    mayInDAO.delete(mayIn);
                }
            }
        }

        // 3. Xóa Phiếu (Sẽ tự xóa các dòng ChiTietPhieuNhap do Cascade)
        phieuNhapDAO.delete(phieuNhap);
    }
    // ... (Các phần import và code cũ giữ nguyên) ...

    // ========================================================================
    //                          PHẦN BỔ SUNG CHO PHIẾU XUẤT
    // ========================================================================

    // 1. LẤY DANH SÁCH XUẤT (TÓM TẮT)
    public List<PhieuXuat> layDanhSachPhieuXuatTomTat() {
        List<PhieuXuat> list = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        for (PhieuXuat px : list) {
            px.setDanhSachChiTiet(null); // Ẩn chi tiết để nhẹ JSON
        }
        return list;
    }

    // 2. LẤY CHI TIẾT 1 PHIẾU XUẤT
    public PhieuXuat layPhieuXuatChiTiet(String soPhieu) {
        return phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất: " + soPhieu));
    }

    // 3. CẬP NHẬT PHIẾU XUẤT (Chỉ cho sửa thông tin chung)
    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat capNhatPhieuXuat(String soPhieu, PhieuXuatDTO dto) {
        PhieuXuat phieuCu = phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất: " + soPhieu));

        phieuCu.setGhiChu(dto.getGhiChu());

        if (dto.getMaDonVi() != null) {
            DonVi khach = donViDAO.findById(dto.getMaDonVi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Khách hàng"));
            phieuCu.setKhachHang(khach);
        }
        return phieuXuatDAO.save(phieuCu);
    }

    // 4. XÓA PHIẾU XUẤT (HỦY BÁN HÀNG -> TRẢ MÁY VỀ KHO)
    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuXuat(String soPhieu) {
        PhieuXuat phieuXuat = phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất: " + soPhieu));

        // Logic phục hồi: Duyệt qua các máy đã bán trong phiếu này
        if (phieuXuat.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuXuat ct : phieuXuat.getDanhSachChiTiet()) {
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null) {
                    // Trả trạng thái về 1 (Tồn kho) để có thể bán lại
                    mayIn.setTrangThai(1);
                    mayInDAO.save(mayIn);
                }
            }
        }
        // Sau khi trả máy về kho xong thì xóa phiếu
        phieuXuatDAO.delete(phieuXuat);
    }
}