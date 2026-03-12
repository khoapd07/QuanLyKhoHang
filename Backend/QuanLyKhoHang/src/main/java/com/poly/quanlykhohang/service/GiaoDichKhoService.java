package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.util.IdGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GiaoDichKhoService {

    @Autowired
    private ThongKeDAO thongKeDAO;
    @Autowired
    private PhieuNhapDAO phieuNhapDAO;
    @Autowired
    private ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;
    @Autowired
    private PhieuXuatDAO phieuXuatDAO;
    @Autowired
    private ChiTietPhieuXuatDAO chiTietPhieuXuatDAO;
    @Autowired
    private DonViDAO donViDAO;
    @Autowired
    private KhoDAO khoDAO;
    @Autowired
    private SanPhamDAO sanPhamDAO;
    @Autowired
    private MayInDAO mayInDAO;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HinhThucXuatDAO hinhThucXuatDAO;

    public MayInDAO getMayInDAO() {
        return mayInDAO;
    }

    @PersistenceContext
    private EntityManager entityManager;

    // ================= NHẬP KHO =================
    public List<PhieuNhapResponseDTO> layDanhSachPhieuNhapHienThi(Integer maKho) {
        List<PhieuNhap> listEntity;

        if (maKho != null && maKho > 0) {
            listEntity = phieuNhapDAO.findByMaKhoOrderByNgayNhapDesc(maKho);
        } else {
            listEntity = phieuNhapDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayNhap"));
        }

        List<PhieuNhapResponseDTO> listDto = new ArrayList<>();

        for (PhieuNhap pn : listEntity) {
            PhieuNhapResponseDTO dto = new PhieuNhapResponseDTO();
            dto.setSoPhieu(pn.getSoPhieu());
            dto.setNgayNhap(pn.getNgayNhap());
            dto.setGhiChu(pn.getGhiChu());
            dto.setTongTien(pn.getTongTien());
            dto.setTongSoLuongMay(pn.getTongSoLuong());

            if (pn.getKhoNhap() != null) dto.setTenKho(pn.getKhoNhap().getTenKho());
            if (pn.getNhaCungCap() != null) dto.setTenKhachHang(pn.getNhaCungCap().getTenDonVi());

            int slConLai = 0;
            int slDaBan = 0;
            BigDecimal tienCon = BigDecimal.ZERO;

            Set<String> hangSet = new HashSet<>();
            Map<String, Integer> spCountMap = new LinkedHashMap<>();

            if (pn.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuNhap ct : pn.getDanhSachChiTiet()) {

                    if (ct.getSanPham() != null) {
                        String keyDisplay = "[" + ct.getSanPham().getMaSP() + "] " + ct.getSanPham().getTenSP();
                        spCountMap.put(keyDisplay, spCountMap.getOrDefault(keyDisplay, 0) + 1);
                        if (ct.getSanPham().getHangSanXuat() != null) {
                            hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
                        }
                    }

                    MayIn may = ct.getMayIn();
                    if (may != null) {
                        if (Boolean.TRUE.equals(may.getTonKho())) {
                            slConLai++;
                            if (ct.getDonGia() != null) {
                                tienCon = tienCon.add(ct.getDonGia());
                            }
                        } else {
                            slDaBan++;
                        }
                    }
                }
            }

            dto.setSoLuongConLai(slConLai);
            dto.setTienConLai(tienCon);
            dto.setDanhSachHang(String.join(", ", hangSet));

            List<String> summaryParts = new ArrayList<>();
            if (slDaBan > 0) summaryParts.add("(Đã xuất: " + slDaBan + " máy)");

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

        LocalDateTime thoiGianGiaoDich = (dto.getNgayTaoPhieu() != null) ? dto.getNgayTaoPhieu() : LocalDateTime.now();

        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setSoPhieu(soPhieuMoi);
        phieuNhap.setNgayNhap(thoiGianGiaoDich);
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
                    mayMoi.setNgayTao(thoiGianGiaoDich);

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

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        // 1. KIỂM TRA CHỐT SỔ
        int namPhieu = phieuNhap.getNgayNhap().getYear();
        int khoId = phieuNhap.getKhoNhap().getMaKho();
        int namKiemTraChotSo = namPhieu + 1;

        if (thongKeDAO.demSoLuongBanGhiChotSo(namKiemTraChotSo, khoId) > 0) {
            throw new RuntimeException("KHÔNG THỂ XÓA! Dữ liệu năm " + namPhieu + " đã được chốt sổ vào đầu năm " + namKiemTraChotSo + ".");
        }

        // 2. KIỂM TRA TỒN KHO & HOÀN LẠI SỐ LƯỢNG SẢN PHẨM
        for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
            MayIn may = ct.getMayIn();
            if (may != null && Boolean.FALSE.equals(may.getTonKho())) {
                throw new RuntimeException("Lỗi: Máy " + may.getMaMay() + " đã xuất bán, KHÔNG THỂ xóa phiếu nhập này!");
            }

            // Trừ lại số lượng Sản phẩm
            SanPham sp = ct.getSanPham();
            if (sp != null) {
                sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
                sanPhamDAO.save(sp);
            }
        }

        // 3. Ép lưu các thay đổi về số lượng Sản Phẩm xuống DB ngay lập tức
        sanPhamDAO.flush();

        // ====================================================================
        // 4. BÚA TẠ JDBCTEMPLATE: XÓA TRỰC TIẾP XUỐNG SQL SERVER
        // Bỏ qua toàn bộ cơ chế Cascade và Cache của Hibernate
        // ====================================================================

        // Chém bay toàn bộ Chi Tiết (Con)
        jdbcTemplate.update("DELETE FROM CTPhieuNhap WHERE SoPhieu = ?", soPhieu);

        // Chém bay toàn bộ Máy In (Cha)
        jdbcTemplate.update("DELETE FROM DMMay WHERE SoPhieuNhap = ?", soPhieu);

        // Tiễn Phiếu Nhập lên đường (Ông nội)
        jdbcTemplate.update("DELETE FROM PhieuNhap WHERE SoPhieu = ?", soPhieu);
    }

    public List<PhieuXuatResponseDTO> layDanhSachPhieuXuatHienThi(Integer maKho) {
        List<PhieuXuat> listEntity;
        if (maKho != null && maKho > 0) {
            listEntity = phieuXuatDAO.findByMaKhoOrderByNgayXuatDesc(maKho);
        } else {
            listEntity = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        }

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

            // LẤY TÊN HÌNH THỨC TRẢ VỀ CHO VUE
            if (px.getHinhThucXuat() != null) dto.setTenHinhThuc(px.getHinhThucXuat().getTenHT());

            Map<String, Integer> spCountMap = new HashMap<>();
            Set<String> hangSet = new HashSet<>();
            if (px.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuXuat ct : px.getDanhSachChiTiet()) {
                    if (ct.getSanPham() != null) {
                        String tenSP = ct.getSanPham().getTenSP();
                        spCountMap.put(tenSP, spCountMap.getOrDefault(tenSP, 0) + 1);
                        if (ct.getSanPham().getHangSanXuat() != null) hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
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

        LocalDateTime thoiGianGiaoDich = (dto.getNgayTaoPhieu() != null) ? dto.getNgayTaoPhieu() : LocalDateTime.now();

        PhieuXuat phieuXuat = new PhieuXuat();
        phieuXuat.setSoPhieu(soPhieuMoi);
        phieuXuat.setNgayXuat(thoiGianGiaoDich);
        phieuXuat.setGhiChu(dto.getGhiChu());

        DonVi khach = donViDAO.findById(dto.getMaDonVi()).orElseThrow();
        phieuXuat.setKhachHang(khach);
        Kho kho = khoDAO.findById(dto.getMaKho()).orElseThrow();
        phieuXuat.setKhoXuat(kho);

        // LƯU HÌNH THỨC VÀO DATABASE
        if (dto.getMaHT() != null) {
            HinhThucXuat ht = hinhThucXuatDAO.findById(dto.getMaHT()).orElseThrow(() -> new RuntimeException("Không tìm thấy hình thức xuất"));
            phieuXuat.setHinhThucXuat(ht);
        }

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

        // CẬP NHẬT KIỂM TRA CHỐT SỔ (+1 NĂM)
        int namPhieu = phieuXuat.getNgayXuat().getYear();
        int khoId = phieuXuat.getKhoXuat().getMaKho();
        int namKiemTraChotSo = namPhieu + 1; // Kiểm tra sổ của năm kế tiếp

        if (thongKeDAO.demSoLuongBanGhiChotSo(namKiemTraChotSo, khoId) > 0) {
            throw new RuntimeException("KHÔNG THỂ XÓA! Dữ liệu năm " + namPhieu + " đã được chốt sổ vào đầu năm " + namKiemTraChotSo + ".");
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
        if (dto.getNgayTaoPhieu() != null) {
            phieuCu.setNgayNhap(dto.getNgayTaoPhieu());
        }
        return phieuNhapDAO.save(phieuCu);
    }

    @Transactional
    public PhieuXuat capNhatPhieuXuat(String soPhieu, PhieuXuatDTO dto) {
        PhieuXuat phieuCu = phieuXuatDAO.findById(soPhieu).orElseThrow();
        phieuCu.setGhiChu(dto.getGhiChu());
        if (dto.getNgayTaoPhieu() != null) {
            phieuCu.setNgayXuat(dto.getNgayTaoPhieu());
        }

        // CẬP NHẬT HÌNH THỨC NẾU CÓ SỬA
        if (dto.getMaHT() != null) {
            HinhThucXuat ht = hinhThucXuatDAO.findById(dto.getMaHT()).orElseThrow();
            phieuCu.setHinhThucXuat(ht);
        }

        return phieuXuatDAO.save(phieuCu);
    }

    @Transactional
    public void xoaDongChiTietNhap(Integer maCTPN) {
        ChiTietPhieuNhap ct = chiTietPhieuNhapDAO.findById(maCTPN).orElseThrow();

        // BỔ SUNG KIỂM TRA CHỐT SỔ (+1 NĂM) TRƯỚC KHI XÓA CHI TIẾT
        PhieuNhap pn = ct.getPhieuNhap();
        int namPhieu = pn.getNgayNhap().getYear();
        int khoId = pn.getKhoNhap().getMaKho();
        int namKiemTraChotSo = namPhieu + 1;
        if (thongKeDAO.demSoLuongBanGhiChotSo(namKiemTraChotSo, khoId) > 0) {
            throw new RuntimeException("KHÔNG THỂ XÓA! Dữ liệu năm " + namPhieu + " đã được chốt sổ vào đầu năm " + namKiemTraChotSo + ".");
        }

        MayIn mayIn = ct.getMayIn();
        if (mayIn != null && Boolean.FALSE.equals(mayIn.getTonKho())) {
            throw new RuntimeException("Máy đã bán, không thể xóa!");
        }

        pn.setTongSoLuong(Math.max(0, pn.getTongSoLuong() - 1));
        if (ct.getDonGia() != null) pn.setTongTien(pn.getTongTien().subtract(ct.getDonGia()));
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

            mayInDAO.save(mayMoi);

            ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
            ctEntity.setPhieuNhap(phieuNhap);
            ctEntity.setSanPham(sp);
            ctEntity.setMayIn(mayMoi);
            ctEntity.setDonGia(dto.getDonGia());
            ctEntity.setGhiChu("Bổ sung");
            chiTietPhieuNhapDAO.save(ctEntity);
            if (dto.getDonGia() != null) tongTienThem = tongTienThem.add(dto.getDonGia());
        }
        phieuNhap.setTongSoLuong(phieuNhap.getTongSoLuong() + soLuongThem);
        phieuNhap.setTongTien(phieuNhap.getTongTien().add(tongTienThem));
        phieuNhapDAO.save(phieuNhap);
        sp.setSoLuong(sp.getSoLuong() + soLuongThem);
        sanPhamDAO.save(sp);
    }
    // ================= SỬA ĐƠN GIÁ ĐỒNG LOẠT THEO MÃ SẢN PHẨM =================
    @Transactional(rollbackFor = Exception.class)
    public void capNhatGiaTheoSanPham(String soPhieu, String maSP, BigDecimal giaMoi) {
        PhieuNhap pn = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        // Kiểm tra chốt sổ
        int namPhieu = pn.getNgayNhap().getYear();
        int khoId = pn.getKhoNhap().getMaKho();
        if (thongKeDAO.demSoLuongBanGhiChotSo(namPhieu + 1, khoId) > 0) {
            throw new RuntimeException("Dữ liệu đã chốt sổ, không thể sửa giá!");
        }

        BigDecimal tongTienChenhLech = BigDecimal.ZERO;
        BigDecimal giaMoiChuan = giaMoi != null ? giaMoi : BigDecimal.ZERO;

        // Duyệt qua toàn bộ máy trong phiếu, cứ trùng mã SP là cập nhật giá mới
        for (ChiTietPhieuNhap ct : pn.getDanhSachChiTiet()) {
            if (ct.getSanPham() != null && ct.getSanPham().getMaSP().equals(maSP)) {
                BigDecimal giaCu = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
                tongTienChenhLech = tongTienChenhLech.add(giaMoiChuan.subtract(giaCu));

                ct.setDonGia(giaMoiChuan);
                chiTietPhieuNhapDAO.save(ct);
            }
        }

        // Cập nhật lại tổng tiền của Phiếu Nhập
        pn.setTongTien(pn.getTongTien().add(tongTienChenhLech));
        phieuNhapDAO.save(pn);
    }
    // ================= XUẤT KHO: XÓA 1 DÒNG CHI TIẾT =================
    @Transactional(rollbackFor = Exception.class)
    public void xoaDongChiTietXuat(Integer maCTPX) {
        ChiTietPhieuXuat ct = chiTietPhieuXuatDAO.findById(maCTPX).orElseThrow();
        PhieuXuat px = ct.getPhieuXuat();

        int namPhieu = px.getNgayXuat().getYear();
        int khoId = px.getKhoXuat().getMaKho();
        if (thongKeDAO.demSoLuongBanGhiChotSo(namPhieu + 1, khoId) > 0) {
            throw new RuntimeException("Dữ liệu đã chốt sổ, không thể xóa!");
        }

        // Trả lại máy in vào kho
        MayIn mayIn = ct.getMayIn();
        if (mayIn != null) {
            mayIn.setTonKho(true);
            mayInDAO.save(mayIn);
        }

        // Trừ tiền và số lượng của phiếu
        px.setTongSoLuong(Math.max(0, px.getTongSoLuong() - 1));
        if (ct.getDonGia() != null) px.setTongTien(px.getTongTien().subtract(ct.getDonGia()));
        phieuXuatDAO.save(px);

        // Cộng lại số lượng tổng của sản phẩm
        SanPham sp = ct.getSanPham();
        sp.setSoLuong(sp.getSoLuong() + 1);
        sanPhamDAO.save(sp);

        chiTietPhieuXuatDAO.delete(ct);
    }

    // ================= XUẤT KHO: BỔ SUNG MÁY VÀO PHIẾU CŨ =================
    @Transactional(rollbackFor = Exception.class)
    public void themDongVaoPhieuCuXuat(String soPhieu, ChiTietXuatDTO dto) {
        PhieuXuat px = phieuXuatDAO.findById(soPhieu).orElseThrow();
        SanPham sp = sanPhamDAO.findById(dto.getMaSP()).orElseThrow();

        BigDecimal tongTienThem = BigDecimal.ZERO;
        int slThem = 0;

        if (dto.getDanhSachSeri() != null) {
            for (String maMayXuat : dto.getDanhSachSeri()) {
                MayIn mayInDb = mayInDAO.findById(maMayXuat).orElseThrow();
                if (Boolean.FALSE.equals(mayInDb.getTonKho())) {
                    throw new RuntimeException("Máy " + maMayXuat + " đã xuất kho rồi!");
                }

                // Trừ kho
                mayInDb.setTonKho(false);
                mayInDAO.save(mayInDb);

                ChiTietPhieuXuat ctEntity = new ChiTietPhieuXuat();
                ctEntity.setPhieuXuat(px);
                ctEntity.setSanPham(sp);
                ctEntity.setMayIn(mayInDb);
                ctEntity.setDonGia(dto.getDonGia());
                chiTietPhieuXuatDAO.save(ctEntity);

                slThem++;
                if (dto.getDonGia() != null) tongTienThem = tongTienThem.add(dto.getDonGia());
            }

            // Cập nhật số lượng tổng sản phẩm
            sp.setSoLuong(Math.max(0, sp.getSoLuong() - slThem));
            sanPhamDAO.save(sp);

            // Cập nhật phiếu
            px.setTongSoLuong(px.getTongSoLuong() + slThem);
            px.setTongTien(px.getTongTien().add(tongTienThem));
            phieuXuatDAO.save(px);
        }
    }

    // ================= XUẤT KHO: SỬA GIÁ ĐỒNG LOẠT =================
    @Transactional(rollbackFor = Exception.class)
    public void capNhatGiaTheoSanPhamXuat(String soPhieu, String maSP, BigDecimal giaMoi) {
        PhieuXuat px = phieuXuatDAO.findById(soPhieu).orElseThrow();

        int namPhieu = px.getNgayXuat().getYear();
        int khoId = px.getKhoXuat().getMaKho();
        if (thongKeDAO.demSoLuongBanGhiChotSo(namPhieu + 1, khoId) > 0) {
            throw new RuntimeException("Dữ liệu đã chốt sổ, không thể sửa giá!");
        }

        BigDecimal tongTienChenhLech = BigDecimal.ZERO;
        BigDecimal giaMoiChuan = giaMoi != null ? giaMoi : BigDecimal.ZERO;

        for (ChiTietPhieuXuat ct : px.getDanhSachChiTiet()) {
            if (ct.getSanPham() != null && ct.getSanPham().getMaSP().equals(maSP)) {
                BigDecimal giaCu = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
                tongTienChenhLech = tongTienChenhLech.add(giaMoiChuan.subtract(giaCu));

                ct.setDonGia(giaMoiChuan);
                chiTietPhieuXuatDAO.save(ct);
            }
        }
        px.setTongTien(px.getTongTien().add(tongTienChenhLech));
        phieuXuatDAO.save(px);
    }
}