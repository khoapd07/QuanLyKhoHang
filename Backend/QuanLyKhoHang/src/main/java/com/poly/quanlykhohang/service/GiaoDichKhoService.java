package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GiaoDichKhoService {

    @Autowired private ThongKeDAO thongKeDAO;
    @Autowired private PhieuNhapDAO phieuNhapDAO;
    @Autowired private ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    @Autowired private PhieuXuatDAO phieuXuatDAO;
    @Autowired private ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;
    @Autowired private DonViDAO donViDAO;
    @Autowired private KhoDAO khoDAO;
    @Autowired private SanPhamDAO sanPhamDAO;
    @Autowired private MayInDAO mayInDAO;
    @Autowired private IdGenerator idGenerator;

    // ================= NHẬP KHO =================
    public List<PhieuNhapResponseDTO> layDanhSachPhieuNhapHienThi() {
        List<PhieuNhap> listEntity = phieuNhapDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));
        List<PhieuNhapResponseDTO> listDto = new ArrayList<>();

        for (PhieuNhap pn : listEntity) {
            PhieuNhapResponseDTO dto = new PhieuNhapResponseDTO();
            dto.setSoPhieu(pn.getSoPhieu());
            dto.setNgayNhap(pn.getNgayNhap());
            dto.setGhiChu(pn.getGhiChu());

            // 1. Tổng số ban đầu
            dto.setTongTien(pn.getTongTien());
            dto.setTongSoLuongMay(pn.getTongSoLuong());

            if (pn.getKhoNhap() != null) dto.setTenKho(pn.getKhoNhap().getTenKho());
            if (pn.getNhaCungCap() != null) dto.setTenKhachHang(pn.getNhaCungCap().getTenDonVi());

            // 2. Tính toán SỐ LƯỢNG CÒN LẠI và TIỀN CÒN LẠI (Dựa trên TonKho = true)
            int demConLai = 0;
            BigDecimal tienCon = BigDecimal.ZERO;

            Set<String> hangSet = new HashSet<>();
            Map<String, Integer> spCountMap = new LinkedHashMap<>(); // Dùng LinkedHashMap để giữ thứ tự

            if (pn.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuNhap ct : pn.getDanhSachChiTiet()) {
                    if (ct.getSanPham() != null) {
                        // [SỬA] Hiển thị tóm tắt kèm Mã SP: "[IP15] iPhone 15 Pro Max"
                        String keyDisplay = "[" + ct.getSanPham().getMaSP() + "] " + ct.getSanPham().getTenSP();
                        spCountMap.put(keyDisplay, spCountMap.getOrDefault(keyDisplay, 0) + 1);

                        if (ct.getSanPham().getHangSanXuat() != null) {
                            hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
                        }
                    }

                    // Logic tính tồn kho thực tế
                    if (ct.getMayIn() != null && Boolean.TRUE.equals(ct.getMayIn().getTonKho())) {
                        demConLai++;
                        if (ct.getDonGia() != null) {
                            tienCon = tienCon.add(ct.getDonGia());
                        }
                    }
                }
            }

            dto.setSoLuongConLai(demConLai);
            dto.setTienConLai(tienCon);
            dto.setDanhSachHang(String.join(", ", hangSet));

            List<String> summaryParts = new ArrayList<>();
            for (String key : spCountMap.keySet()) {
                summaryParts.add(key + " x" + spCountMap.get(key));
            }
            dto.setTomTatSanPham(String.join(", ", summaryParts));
            listDto.add(dto);
        }
        return listDto;
    }

    public PhieuNhap layPhieuNhapChiTiet(String soPhieu) {
        return phieuNhapDAO.findById(soPhieu).orElseThrow();
    }

    @Transactional(rollbackFor = Exception.class)
    public PhieuNhap nhapKho(PhieuNhapDTO dto) {
        String prefixPN = "PN" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastIdPN = phieuNhapDAO.findLastId(prefixPN).orElse(null);
        String soPhieuMoi = idGenerator.generateNextId("PN", lastIdPN);

        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setSoPhieu(soPhieuMoi);
        phieuNhap.setNgayNhap(LocalDateTime.now());
        phieuNhap.setGhiChu(dto.getGhiChu());

        Kho kho = khoDAO.findById(dto.getMaKho()).orElseThrow(() -> new RuntimeException("Thiếu Kho"));
        phieuNhap.setKhoNhap(kho);

        if (dto.getMaDonVi() != null) {
            DonVi ncc = donViDAO.findById(dto.getMaDonVi()).orElseThrow(() -> new RuntimeException("Thiếu NCC"));
            phieuNhap.setNhaCungCap(ncc);
        }

        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);
        List<ChiTietPhieuNhap> listChiTiet = new ArrayList<>();
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        if (dto.getChiTietPhieuNhap() != null) {
            for (ChiTietNhapDTO ctDto : dto.getChiTietPhieuNhap()) {
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP()).orElseThrow();
                int soLuongCanNhap = ctDto.getSoLuong();
                Integer trangThaiNhap = (ctDto.getTrangThai() != null) ? ctDto.getTrangThai() : 1;

                // [LOGIC SINH MÃ MÁY]
                String prefixMaMay = sp.getMaSP();
                String lastMaMayInDB = mayInDAO.findLastId(prefixMaMay).orElse(null);

                for (int i = 0; i < soLuongCanNhap; i++) {
                    String maMayMoi = idGenerator.generateNextId(prefixMaMay, lastMaMayInDB);
                    lastMaMayInDB = maMayMoi;

                    MayIn mayMoi = new MayIn();
                    mayMoi.setMaMay(maMayMoi);
                    mayMoi.setSoSeri(maMayMoi);
                    mayMoi.setSanPham(sp);
                    mayMoi.setKho(kho);
                    mayMoi.setSoPhieuNhap(savedPhieu.getSoPhieu());
                    mayMoi.setTrangThai(trangThaiNhap);
                    mayMoi.setTonKho(true);
                    mayMoi.setNgayTao(LocalDateTime.now());

                    // [ĐÃ SỬA] Xóa dòng setHangSanXuat gây lỗi
                    // Hãng SX sẽ được truy xuất thông qua quan hệ MayIn -> SanPham -> HangSX

                    mayInDAO.save(mayMoi);

                    ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                    ctEntity.setPhieuNhap(savedPhieu);
                    ctEntity.setSanPham(sp);
                    ctEntity.setMayIn(mayMoi);
                    ctEntity.setDonGia(ctDto.getDonGia());
                    ctEntity.setGhiChu(ctDto.getGhiChu());
                    chiTietPhieuNhapDAO.save(ctEntity);
                    listChiTiet.add(ctEntity);
                }
                tongSoLuong += soLuongCanNhap;
                if (ctDto.getDonGia() != null) {
                    tongTien = tongTien.add(ctDto.getDonGia().multiply(BigDecimal.valueOf(soLuongCanNhap)));
                }
                sp.setSoLuong((sp.getSoLuong() == null ? 0 : sp.getSoLuong()) + soLuongCanNhap);
                sanPhamDAO.save(sp);
            }
        }
        savedPhieu.setTongSoLuong(tongSoLuong);
        savedPhieu.setTongTien(tongTien);
        savedPhieu.setDanhSachChiTiet(listChiTiet);
        return phieuNhapDAO.save(savedPhieu);
    }

    // [CÁC PHƯƠNG THỨC KHÁC GIỮ NGUYÊN]
    // ... xoaPhieuNhap, xuatKho, xoaPhieuXuat ...

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu).orElseThrow();
        for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
            if (ct.getMayIn() != null && Boolean.FALSE.equals(ct.getMayIn().getTonKho())) {
                throw new RuntimeException("Không thể xóa phiếu nhập! Máy " + ct.getMayIn().getMaMay() + " đã được xuất bán.");
            }
        }
        if (phieuNhap.getDanhSachChiTiet() != null) {
            List<ChiTietPhieuNhap> listCT = new ArrayList<>(phieuNhap.getDanhSachChiTiet());
            for (ChiTietPhieuNhap ct : listCT) {
                SanPham sp = ct.getSanPham();
                if(sp != null) sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
                sanPhamDAO.save(sp);
                chiTietPhieuNhapDAO.delete(ct);
                if(ct.getMayIn() != null) mayInDAO.delete(ct.getMayIn());
            }
        }
        phieuNhapDAO.delete(phieuNhap);
    }

    public List<PhieuXuatResponseDTO> layDanhSachPhieuXuatHienThi() {
        List<PhieuXuat> listEntity = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        List<PhieuXuatResponseDTO> listDto = new ArrayList<>();
        for (PhieuXuat px : listEntity) {
            PhieuXuatResponseDTO dto = new PhieuXuatResponseDTO();
            dto.setSoPhieu(px.getSoPhieu());
            dto.setNgayXuat(px.getNgayXuat());
            dto.setGhiChu(px.getGhiChu());
            dto.setTongTien(px.getTongTien());
            dto.setTongSoLuong(px.getTongSoLuong());
            if (px.getKhoXuat() != null) dto.setTenKho(px.getKhoXuat().getTenKho());
            if (px.getKhachHang() != null) dto.setTenKhachHang(px.getKhachHang().getTenDonVi());

            Map<String, Integer> spCountMap = new HashMap<>();
            Set<String> hangSet = new HashSet<>();
            if (px.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuXuat ct : px.getDanhSachChiTiet()) {
                    if (ct.getSanPham() != null) {
                        String tenSP = ct.getSanPham().getTenSP();
                        spCountMap.put(tenSP, spCountMap.getOrDefault(tenSP, 0) + 1);
                        if(ct.getSanPham().getHangSanXuat() != null) hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
                    }
                }
            }
            dto.setDanhSachHang(String.join(", ", hangSet));
            List<String> summaryParts = new ArrayList<>();
            for (String tenSP : spCountMap.keySet()) {
                summaryParts.add(tenSP + " x" + spCountMap.get(tenSP));
            }
            dto.setTomTatSanPham(String.join(", ", summaryParts));
            listDto.add(dto);
        }
        return listDto;
    }

    public PhieuXuat layPhieuXuatChiTiet(String soPhieu) {
        return phieuXuatDAO.findById(soPhieu).orElseThrow();
    }

    @Transactional(rollbackFor = Exception.class)
    public PhieuXuat xuatKho(PhieuXuatDTO dto) {
        String prefixPX = "PX" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastIdPX = phieuXuatDAO.findLastId(prefixPX).orElse(null);
        String soPhieuMoi = idGenerator.generateNextId("PX", lastIdPX);

        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setSoPhieu(soPhieuMoi);
        phieuXuat.setNgayXuat(LocalDateTime.now());
        phieuXuat.setGhiChu(dto.getGhiChu());

        DonVi khach = donViDAO.findById(dto.getMaDonVi()).orElseThrow();
        phieuXuat.setKhachHang(khach);
        Kho kho = khoDAO.findById(dto.getMaKho()).orElseThrow();
        phieuXuat.setKhoXuat(kho);

        PhieuXuat savedPhieu = phieuXuatDAO.save(phieuXuat);
        List<ChiTietPhieuXuat> listChiTiet = new ArrayList<>();
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        if (dto.getChiTietPhieuXuat() != null) {
            for (ChiTietXuatDTO ctDto : dto.getChiTietPhieuXuat()) {
                SanPham sp = sanPhamDAO.findById(ctDto.getMaSP()).orElseThrow();
                if (ctDto.getDanhSachSeri() != null) {
                    for (String maMayXuat : ctDto.getDanhSachSeri()) {
                        MayIn mayInDb = mayInDAO.findById(maMayXuat).orElseThrow();
                        if (Boolean.FALSE.equals(mayInDb.getTonKho())) {
                            throw new RuntimeException("Máy " + maMayXuat + " đã xuất kho rồi!");
                        }
                        mayInDb.setTonKho(false);
                        mayInDAO.save(mayInDb);

                        ChiTietPhieuXuat ctEntity = new ChiTietPhieuXuat();
                        ctEntity.setPhieuXuat(savedPhieu);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(mayInDb);
                        ctEntity.setDonGia(ctDto.getDonGia());
                        chiTietPhieuXuatDAO.save(ctEntity);
                        listChiTiet.add(ctEntity);

                        tongSoLuong++;
                        if (ctDto.getDonGia() != null) tongTien = tongTien.add(ctDto.getDonGia());
                    }
                    sp.setSoLuong(Math.max(0, sp.getSoLuong() - ctDto.getDanhSachSeri().size()));
                    sanPhamDAO.save(sp);
                }
            }
        }
        savedPhieu.setTongSoLuong(tongSoLuong);
        savedPhieu.setTongTien(tongTien);
        savedPhieu.setDanhSachChiTiet(listChiTiet);
        return phieuXuatDAO.save(savedPhieu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuXuat(String soPhieu) {
        PhieuXuat phieuXuat = phieuXuatDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất: " + soPhieu));

        int namPhieu = phieuXuat.getNgayXuat().getYear();
        int khoId = phieuXuat.getKhoXuat().getMaKho();
        int soLuongBanGhiChot = thongKeDAO.demSoLuongBanGhiChotSo(namPhieu, khoId);

        if (soLuongBanGhiChot > 0) {
            throw new RuntimeException("KHÔNG THỂ XÓA! Năm " + namPhieu + " đã được chốt sổ. Dữ liệu đã bị khóa.");
        }

        if (phieuXuat.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuXuat ct : phieuXuat.getDanhSachChiTiet()) {
                MayIn mayIn = ct.getMayIn();
                if (mayIn != null) {
                    mayIn.setTonKho(true);
                    mayInDAO.save(mayIn);
                    SanPham sp = ct.getSanPham();
                    sp.setSoLuong(sp.getSoLuong() + 1);
                    sanPhamDAO.save(sp);
                }
                chiTietPhieuXuatDAO.delete(ct);
            }
        }
        phieuXuatDAO.delete(phieuXuat);
    }

    @Transactional
    public PhieuNhap capNhatPhieuNhap(String soPhieu, PhieuNhapDTO dto) {
        PhieuNhap phieuCu = phieuNhapDAO.findById(soPhieu).orElseThrow();
        phieuCu.setGhiChu(dto.getGhiChu());
        return phieuNhapDAO.save(phieuCu);
    }

    @Transactional
    public void xoaDongChiTietNhap(Integer maCTPN) {
        ChiTietPhieuNhap ct = chiTietPhieuNhapDAO.findById(maCTPN).orElseThrow();
        MayIn mayIn = ct.getMayIn();
        if (mayIn != null && Boolean.FALSE.equals(mayIn.getTonKho())) {
            throw new RuntimeException("Máy đã bán, không thể xóa!");
        }
        PhieuNhap pn = ct.getPhieuNhap();
        pn.setTongSoLuong(Math.max(0, pn.getTongSoLuong() - 1));
        if(ct.getDonGia()!=null) pn.setTongTien(pn.getTongTien().subtract(ct.getDonGia()));
        phieuNhapDAO.save(pn);

        SanPham sp = ct.getSanPham();
        sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
        sanPhamDAO.save(sp);

        chiTietPhieuNhapDAO.delete(ct);
        if (mayIn != null) mayInDAO.delete(mayIn);
    }

    @Transactional
    public void themDongVaoPhieuCu(String soPhieu, ChiTietNhapDTO dto) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu).orElseThrow();
        SanPham sp = sanPhamDAO.findById(dto.getMaSP()).orElseThrow();
        int soLuongThem = dto.getSoLuong();
        Integer trangThaiNhap = (dto.getTrangThai() != null) ? dto.getTrangThai() : 1;

        String prefixMaMay = sp.getMaSP();
        String lastId = mayInDAO.findLastId(prefixMaMay).orElse(null);
        BigDecimal tongTienThem = BigDecimal.ZERO;

        for (int i = 0; i < soLuongThem; i++) {
            String maMayMoi = idGenerator.generateNextId(prefixMaMay, lastId);
            lastId = maMayMoi;

            MayIn mayMoi = new MayIn();
            mayMoi.setMaMay(maMayMoi);
            mayMoi.setSoSeri(maMayMoi);
            mayMoi.setSanPham(sp);
            mayMoi.setKho(phieuNhap.getKhoNhap());
            mayMoi.setSoPhieuNhap(soPhieu);
            mayMoi.setTrangThai(trangThaiNhap);
            mayMoi.setTonKho(true);
            mayMoi.setNgayTao(LocalDateTime.now());
            // [ĐÃ SỬA] Xóa dòng setHangSanXuat
            mayInDAO.save(mayMoi);

            ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
            ctEntity.setPhieuNhap(phieuNhap);
            ctEntity.setSanPham(sp);
            ctEntity.setMayIn(mayMoi);
            ctEntity.setDonGia(dto.getDonGia());
            ctEntity.setGhiChu("Bổ sung");
            chiTietPhieuNhapDAO.save(ctEntity);
            if(dto.getDonGia()!=null) tongTienThem = tongTienThem.add(dto.getDonGia());
        }
        phieuNhap.setTongSoLuong(phieuNhap.getTongSoLuong() + soLuongThem);
        phieuNhap.setTongTien(phieuNhap.getTongTien().add(tongTienThem));
        phieuNhapDAO.save(phieuNhap);
        sp.setSoLuong(sp.getSoLuong() + soLuongThem);
        sanPhamDAO.save(sp);
    }
}