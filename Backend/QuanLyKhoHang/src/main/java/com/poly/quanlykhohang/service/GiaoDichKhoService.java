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
    @Autowired
    private HinhThucNhapDAO hinhThucNhapDAO;

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
            if (pn.getHinhThucNhap() != null) dto.setTenHinhThuc(pn.getHinhThucNhap().getTenHT());

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

        if (dto.getMaHT() != null) {
            HinhThucNhap ht = hinhThucNhapDAO.findById(dto.getMaHT()).orElseThrow(() -> new RuntimeException("Thiếu hình thức"));
            phieuNhap.setHinhThucNhap(ht);
        }

        PhieuNhap savedPhieu = phieuNhapDAO.save(phieuNhap);
        List<ChiTietPhieuNhap> listChiTiet = new ArrayList<>();
        int tongSoLuong = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        if (dto.getSoPhieuXuatNoiBo() != null && !dto.getSoPhieuXuatNoiBo().isEmpty()) {
            // =========================================================================
            // TRƯỜNG HỢP 1: NHẬP NỘI BỘ (LẤY LẠI MÁY CŨ, KHÔNG TẠO MÁY MỚI)
            // =========================================================================
            PhieuXuat pxNoiBo = phieuXuatDAO.findById(dto.getSoPhieuXuatNoiBo()).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất gốc!"));

            for (ChiTietPhieuXuat ctXuat : pxNoiBo.getDanhSachChiTiet()) {
                MayIn mayCu = ctXuat.getMayIn();
                SanPham sp = ctXuat.getSanPham();

                // Chuyển kho và bật lại trạng thái Tồn Kho, giữ nguyên SoPhieuNhap gốc
                mayCu.setKho(kho);
                mayCu.setTonKho(true);
                mayInDAO.save(mayCu);

                ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                ctEntity.setPhieuNhap(savedPhieu);
                ctEntity.setSanPham(sp);
                ctEntity.setMayIn(mayCu);
                ctEntity.setDonGia(ctXuat.getDonGia());
                ctEntity.setGhiChu("Nhập chuyển kho");
                chiTietPhieuNhapDAO.save(ctEntity);
                listChiTiet.add(ctEntity);

                tongSoLuong++;
                if (ctXuat.getDonGia() != null) {
                    tongTien = tongTien.add(ctXuat.getDonGia());
                }

                sp.setSoLuong((sp.getSoLuong() == null ? 0 : sp.getSoLuong()) + 1);
                sanPhamDAO.save(sp);
            }

            // Đóng dấu phiếu xuất gốc đã hoàn thành
            String safeNote = dto.getGhiChu() != null ? dto.getGhiChu() : "";
            pxNoiBo.setGhiChu(safeNote + " (Mã PN: " + savedPhieu.getSoPhieu() + ")");
            phieuXuatDAO.save(pxNoiBo);

        } else {
            // =========================================================================
            // TRƯỜNG HỢP 2: NHẬP BÌNH THƯỜNG (TẠO MÁY MỚI HOÀN TOÀN)
            // =========================================================================
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
        }

        savedPhieu.setTongSoLuong(tongSoLuong);
        savedPhieu.setTongTien(tongTien);
        savedPhieu.setDanhSachChiTiet(listChiTiet);
        return phieuNhapDAO.save(savedPhieu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuNhap(String soPhieu) {
        // TÌM MÃ KHO XUẤT GỐC TRƯỚC KHI XÓA DẤU VẾT ĐỂ TRẢ VỀ ĐÚNG KHO
        Integer maKhoGoc = null;
        try {
            maKhoGoc = jdbcTemplate.queryForObject("SELECT MaKho FROM PhieuXuat WHERE GhiChu LIKE ?", Integer.class, "%(Mã PN: " + soPhieu + ")%");
        } catch (Exception e) {}

        // MỞ KHÓA LẠI PHIẾU XUẤT NỘI BỘ NẾU PHIẾU NHẬP NÀY ĐƯỢC SINH RA TỪ XUẤT NỘI BỘ
        jdbcTemplate.update("UPDATE PhieuXuat SET GhiChu = NULL WHERE GhiChu LIKE ?", "%(Mã PN: " + soPhieu + ")%");

        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        boolean isNhapNoiBo = phieuNhap.getHinhThucNhap() != null && phieuNhap.getHinhThucNhap().getTenHT().toLowerCase().contains("nội bộ");
        Kho khoCuaMay = null;
        if (isNhapNoiBo && maKhoGoc != null) {
            khoCuaMay = khoDAO.findById(maKhoGoc).orElse(null);
        }

        // KIỂM TRA TỒN KHO & HOÀN LẠI SỐ LƯỢNG SẢN PHẨM VÀ KHO
        for (ChiTietPhieuNhap ct : phieuNhap.getDanhSachChiTiet()) {
            MayIn may = ct.getMayIn();

            // Chỉ chặn xóa nếu máy đã bán và đây là phiếu nhập bình thường.
            // Nếu là nội bộ, máy được quyền lơ lửng.
            if (may != null && Boolean.FALSE.equals(may.getTonKho()) && !isNhapNoiBo) {
                throw new RuntimeException("Lỗi: Máy " + may.getMaMay() + " đã xuất bán, KHÔNG THỂ xóa phiếu nhập này!");
            }

            // Trừ lại số lượng Sản phẩm
            SanPham sp = ct.getSanPham();
            if (sp != null) {
                sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
                sanPhamDAO.save(sp);
            }

            // Nếu là nội bộ, đẩy trạng thái máy về Đã xuất (Chờ nhập) và ĐỔI LẠI VỀ KHO GỐC BÊN XUẤT
            if (isNhapNoiBo && may != null) {
                may.setTonKho(false);
                if (khoCuaMay != null) {
                    may.setKho(khoCuaMay);
                }
                mayInDAO.save(may);
            }
        }

        // Ép lưu các thay đổi về số lượng Sản Phẩm xuống DB ngay lập tức
        sanPhamDAO.flush();

        // ====================================================================
        // BÚA TẠ JDBCTEMPLATE: XÓA TRỰC TIẾP XUỐNG SQL SERVER
        // ====================================================================

        // Chém bay toàn bộ Chi Tiết (Con)
        jdbcTemplate.update("DELETE FROM CTPhieuNhap WHERE SoPhieu = ?", soPhieu);

        // CHỈ XÓA KHỎI BẢNG MÁY IN NẾU LÀ MÁY MỚI TINH (NHẬP BÌNH THƯỜNG)
        if (!isNhapNoiBo) {
            jdbcTemplate.update("DELETE FROM DMMay WHERE SoPhieuNhap = ?", soPhieu);
        }

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
            if (px.getHinhThucXuat() != null) dto.setTenHinhThuc(px.getHinhThucXuat().getTenHT());

            Map<String, Integer> spCountMap = new LinkedHashMap<>();
            Set<String> hangSet = new HashSet<>();
            if (px.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuXuat ct : px.getDanhSachChiTiet()) {
                    if (ct.getSanPham() != null) {
                        String keyDisplay = "[" + ct.getSanPham().getMaSP() + "] " + ct.getSanPham().getTenSP();
                        spCountMap.put(keyDisplay, spCountMap.getOrDefault(keyDisplay, 0) + 1);
                        if (ct.getSanPham().getHangSanXuat() != null) hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
                    }
                }
            }
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

        if (dto.getMaDonVi() != null) {
            DonVi khach = donViDAO.findById(dto.getMaDonVi()).orElseThrow();
            phieuXuat.setKhachHang(khach);
        }
        if (dto.getKhoNhan() != null) {
            Kho khoNhanDb = khoDAO.findById(dto.getKhoNhan()).orElseThrow();
            phieuXuat.setKhoNhan(khoNhanDb);
        }

        Kho kho = khoDAO.findById(dto.getMaKho()).orElseThrow();
        phieuXuat.setKhoXuat(kho);

        if (dto.getMaHT() != null) {
            HinhThucXuat ht = hinhThucXuatDAO.findById(dto.getMaHT()).orElseThrow();
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

        // CHECK VALIDATE KHÔNG CHO XÓA XUẤT NỘI BỘ ĐÃ NHẬP
        if (phieuXuat.getHinhThucXuat() != null && phieuXuat.getHinhThucXuat().getTenHT().toLowerCase().contains("nội bộ")) {
            if (phieuXuat.getGhiChu() != null && phieuXuat.getGhiChu().contains("(Mã PN:")) {
                throw new RuntimeException("Không thể xóa! Phiếu xuất nội bộ này đã được nhập vào kho đích. Vui lòng xóa Phiếu Nhập tương ứng trước.");
            }
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
        if (dto.getMaHT() != null) {
            HinhThucNhap ht = hinhThucNhapDAO.findById(dto.getMaHT()).orElseThrow();
            phieuCu.setHinhThucNhap(ht);
        }

        // ĐỒNG BỘ GHI CHÚ SANG PHIẾU XUẤT NỘI BỘ NẾU ĐÂY LÀ PHIẾU NHẬP NỘI BỘ
        if (phieuCu.getHinhThucNhap() != null && phieuCu.getHinhThucNhap().getTenHT().toLowerCase().contains("nội bộ")) {
            String safeGhiChu = (dto.getGhiChu() != null) ? dto.getGhiChu() : "";
            String newGhiChuPX = safeGhiChu + " (Mã PN: " + soPhieu + ")";
            jdbcTemplate.update("UPDATE PhieuXuat SET GhiChu = ? WHERE GhiChu LIKE ?", newGhiChuPX, "%(Mã PN: " + soPhieu + ")%");
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
        if (dto.getMaHT() != null) {
            HinhThucXuat ht = hinhThucXuatDAO.findById(dto.getMaHT()).orElseThrow();
            phieuCu.setHinhThucXuat(ht);
        }
        return phieuXuatDAO.save(phieuCu);
    }

    @Transactional
    public void xoaDongChiTietNhap(Integer maCTPN) {
        ChiTietPhieuNhap ct = chiTietPhieuNhapDAO.findById(maCTPN).orElseThrow();
        PhieuNhap pn = ct.getPhieuNhap();

        if (pn.getHinhThucNhap() != null && pn.getHinhThucNhap().getTenHT().toLowerCase().contains("nội bộ")) {
            throw new RuntimeException("Lỗi: Không được xóa chi tiết của Phiếu Nhập Nội Bộ!");
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

        if (phieuNhap.getHinhThucNhap() != null && phieuNhap.getHinhThucNhap().getTenHT().toLowerCase().contains("nội bộ")) {
            throw new RuntimeException("Lỗi: Không được thêm máy thủ công vào Phiếu Nhập Nội Bộ!");
        }

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

    @Transactional(rollbackFor = Exception.class)
    public void capNhatGiaTheoSanPham(String soPhieu, String maSP, BigDecimal giaMoi) {
        PhieuNhap pn = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));

        BigDecimal tongTienChenhLech = BigDecimal.ZERO;
        BigDecimal giaMoiChuan = giaMoi != null ? giaMoi : BigDecimal.ZERO;

        for (ChiTietPhieuNhap ct : pn.getDanhSachChiTiet()) {
            if (ct.getSanPham() != null && ct.getSanPham().getMaSP().equals(maSP)) {
                BigDecimal giaCu = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
                tongTienChenhLech = tongTienChenhLech.add(giaMoiChuan.subtract(giaCu));

                ct.setDonGia(giaMoiChuan);
                chiTietPhieuNhapDAO.save(ct);
            }
        }
        pn.setTongTien(pn.getTongTien().add(tongTienChenhLech));
        phieuNhapDAO.save(pn);
    }

    @Transactional(rollbackFor = Exception.class)
    public void xoaDongChiTietXuat(Integer maCTPX) {
        ChiTietPhieuXuat ct = chiTietPhieuXuatDAO.findById(maCTPX).orElseThrow();
        PhieuXuat px = ct.getPhieuXuat();

        if (px.getHinhThucXuat() != null && px.getHinhThucXuat().getTenHT().toLowerCase().contains("nội bộ")) {
            if (px.getGhiChu() != null && px.getGhiChu().contains("(Mã PN:")) {
                throw new RuntimeException("Không thể thu hồi! Máy này đã được xác nhận nhập vào kho đích.");
            }
        }

        MayIn mayIn = ct.getMayIn();
        if (mayIn != null) {
            mayIn.setTonKho(true);
            mayInDAO.save(mayIn);
        }

        px.setTongSoLuong(Math.max(0, px.getTongSoLuong() - 1));
        if (ct.getDonGia() != null) px.setTongTien(px.getTongTien().subtract(ct.getDonGia()));
        phieuXuatDAO.save(px);

        SanPham sp = ct.getSanPham();
        sp.setSoLuong(sp.getSoLuong() + 1);
        sanPhamDAO.save(sp);

        chiTietPhieuXuatDAO.delete(ct);
    }

    @Transactional(rollbackFor = Exception.class)
    public void themDongVaoPhieuCuXuat(String soPhieu, ChiTietXuatDTO dto) {
        PhieuXuat px = phieuXuatDAO.findById(soPhieu).orElseThrow();

        // VALIDATE: KHÔNG CHO THÊM MÁY NẾU PHIẾU ĐÃ BỊ CHỐT NHẬP KHO ĐÍCH
        if (px.getHinhThucXuat() != null && px.getHinhThucXuat().getTenHT().toLowerCase().contains("nội bộ")) {
            if (px.getGhiChu() != null && px.getGhiChu().contains("(Mã PN:")) {
                throw new RuntimeException("Phiếu xuất nội bộ này đã được nhập kho hoàn tất, không thể bổ sung thêm máy!");
            }
        }

        SanPham sp = sanPhamDAO.findById(dto.getMaSP()).orElseThrow();

        BigDecimal tongTienThem = BigDecimal.ZERO;
        int slThem = 0;

        if (dto.getDanhSachSeri() != null) {
            for (String maMayXuat : dto.getDanhSachSeri()) {
                MayIn mayInDb = mayInDAO.findById(maMayXuat).orElseThrow();
                if (Boolean.FALSE.equals(mayInDb.getTonKho())) {
                    throw new RuntimeException("Máy " + maMayXuat + " đã xuất kho rồi!");
                }

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
            sp.setSoLuong(Math.max(0, sp.getSoLuong() - slThem));
            sanPhamDAO.save(sp);

            px.setTongSoLuong(px.getTongSoLuong() + slThem);
            px.setTongTien(px.getTongTien().add(tongTienThem));
            phieuXuatDAO.save(px);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void capNhatGiaTheoSanPhamXuat(String soPhieu, String maSP, BigDecimal giaMoi) {
        PhieuXuat px = phieuXuatDAO.findById(soPhieu).orElseThrow();

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

    public List<PhieuXuatResponseDTO> layDanhSachXuatNoiBoChoNhap(Integer maKhoNhan) {
        List<PhieuXuat> all = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        List<PhieuXuatResponseDTO> listDto = new ArrayList<>();
        for (PhieuXuat px : all) {
            if (px.getHinhThucXuat() != null && px.getHinhThucXuat().getTenHT().toLowerCase().contains("nội bộ")) {
                if (px.getKhoNhan() != null && px.getKhoNhan().getMaKho().equals(maKhoNhan)) {
                    if (px.getGhiChu() == null || px.getGhiChu().trim().isEmpty()) {
                        PhieuXuatResponseDTO dto = new PhieuXuatResponseDTO();
                        dto.setSoPhieu(px.getSoPhieu());
                        dto.setNgayXuat(px.getNgayXuat());
                        dto.setTongSoLuong(px.getTongSoLuong());
                        if (px.getKhoXuat() != null) dto.setTenKho(px.getKhoXuat().getTenKho());
                        listDto.add(dto);
                    }
                }
            }
        }
        return listDto;
    }
}