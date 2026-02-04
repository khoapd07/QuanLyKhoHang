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
    @Autowired private PhieuChuyenDAO phieuChuyenDAO;
    @Autowired private ChiTietPhieuChuyenDAO chiTietPhieuChuyenDAO;

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

            // --- [LOGIC MỚI BẮT ĐẦU] ---
            int slConLaiTaiKho = 0;   // Còn nằm im tại kho nhập
            int slDaChuyenDi = 0;     // Đã chuyển sang kho khác
            int slDaBan = 0;          // Đã xuất bán
            BigDecimal tienCon = BigDecimal.ZERO;

            Set<String> hangSet = new HashSet<>();
            Map<String, Integer> spCountMap = new LinkedHashMap<>();

            if (pn.getDanhSachChiTiet() != null) {
                for (ChiTietPhieuNhap ct : pn.getDanhSachChiTiet()) {
                    // 1. Tóm tắt tên SP
                    if (ct.getSanPham() != null) {
                        String keyDisplay = "[" + ct.getSanPham().getMaSP() + "] " + ct.getSanPham().getTenSP();
                        spCountMap.put(keyDisplay, spCountMap.getOrDefault(keyDisplay, 0) + 1);
                        if (ct.getSanPham().getHangSanXuat() != null) {
                            hangSet.add(ct.getSanPham().getHangSanXuat().getTenHang());
                        }
                    }

                    // 2. Phân loại trạng thái máy
                    MayIn may = ct.getMayIn();
                    if (may != null) {
                        boolean tonKho = Boolean.TRUE.equals(may.getTonKho());

                        if (!tonKho) {
                            // Case 1: Đã bán
                            slDaBan++;
                        } else {
                            // Case 2: Còn tồn kho (check xem đang ở kho nào)
                            boolean dungKho = may.getKho().getMaKho().equals(pn.getKhoNhap().getMaKho());

                            if (dungKho) {
                                // Vẫn ở kho nhập
                                slConLaiTaiKho++;
                                if (ct.getDonGia() != null) tienCon = tienCon.add(ct.getDonGia());
                            } else {
                                // Đã chuyển sang kho khác (Vẫn tồn kho nhưng không ở đây)
                                slDaChuyenDi++;
                            }
                        }
                    }
                }
            }

            // Gán giá trị hiển thị
            dto.setSoLuongConLai(slConLaiTaiKho); // Chỉ hiển thị số thực tế còn tại kho này
            dto.setTienConLai(tienCon);
            dto.setDanhSachHang(String.join(", ", hangSet));

            // Tạo chuỗi tóm tắt thông minh hơn
            List<String> summaryParts = new ArrayList<>();
            // Hiển thị cảnh báo nếu có hàng đã chuyển hoặc đã bán
            if (slDaChuyenDi > 0) summaryParts.add("(Đã chuyển: " + slDaChuyenDi + ")");
            if (slDaBan > 0) summaryParts.add("(Đã bán: " + slDaBan + ")");

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
        // 1. Tìm phiếu nhập
        PhieuNhap phieuNhap = phieuNhapDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        // 2. Tạo bản sao danh sách để duyệt (tránh lỗi thay đổi list khi đang duyệt)
        List<ChiTietPhieuNhap> listCanXoa = new ArrayList<>(phieuNhap.getDanhSachChiTiet());

        // [SỬA LỖI QUAN TRỌNG TẠI ĐÂY]
        // Thay vì set null, ta dùng .clear() để báo Hibernate danh sách này bị làm rỗng
        // nhưng vẫn giữ nguyên tham chiếu wrapper của Hibernate.
        phieuNhap.getDanhSachChiTiet().clear();

        // Lưu tạm để Hibernate cập nhật trạng thái "rỗng" của Phiếu Nhập xuống DB
        // Điều này giúp ngắt kết nối giữa Phiếu Nhập và các Chi Tiết
        phieuNhapDAO.save(phieuNhap);

        // 3. Xử lý xóa từng dòng chi tiết và máy móc đi kèm
        for (ChiTietPhieuNhap ct : listCanXoa) {
            MayIn may = ct.getMayIn();

            // Kiểm tra máy
            if (may != null) {
                // Nếu máy đã bán -> Chặn luôn (Rollback toàn bộ)
                if (Boolean.FALSE.equals(may.getTonKho())) {
                    throw new RuntimeException("Lỗi: Máy " + may.getMaMay() + " đã xuất bán, không thể xóa phiếu nhập!");
                }

                // Xóa lịch sử chuyển kho của máy này (nếu có)
                List<ChiTietPhieuChuyen> lichSuChuyen = chiTietPhieuChuyenDAO.findByMayIn(may);
                if (!lichSuChuyen.isEmpty()) {
                    chiTietPhieuChuyenDAO.deleteAll(lichSuChuyen);
                }

                // Xóa Máy (Vì dòng chi tiết nhập đã bị ngắt kết nối ở bước clear() trên, nên xóa máy an toàn)
                mayInDAO.delete(may);
            }

            // Xóa dòng chi tiết phiếu nhập (Dọn dẹp rác)
            chiTietPhieuNhapDAO.delete(ct);
        }

        // 4. Cuối cùng xóa Phiếu Nhập Header
        phieuNhapDAO.delete(phieuNhap);
    }

    public List<PhieuXuatResponseDTO> layDanhSachPhieuXuatHienThi(Integer maKho) {
        List<PhieuXuat> listEntity = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        List<PhieuXuatResponseDTO> listDto = new ArrayList<>();

        if (maKho != null && maKho > 0) {
            listEntity = phieuXuatDAO.findByMaKhoOrderByNgayXuatDesc(maKho);
        } else {
            listEntity = phieuXuatDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayXuat"));
        }

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
    // 1. LẤY DANH SÁCH PHIẾU CHUYỂN (HIỂN THỊ)
    public List<PhieuChuyenResponseDTO> layDanhSachPhieuChuyen() {
        List<PhieuChuyen> listEntity = phieuChuyenDAO.findAll(Sort.by(Sort.Direction.DESC, "ngayChuyen"));
        List<PhieuChuyenResponseDTO> listDto = new ArrayList<>();

        for (PhieuChuyen pc : listEntity) {
            PhieuChuyenResponseDTO dto = new PhieuChuyenResponseDTO();
            dto.setSoPhieu(pc.getSoPhieu());
            dto.setNgayChuyen(pc.getNgayChuyen());
            dto.setGhiChu(pc.getGhiChu());
            if (pc.getKhoDi() != null) dto.setTenKhoDi(pc.getKhoDi().getTenKho());
            if (pc.getKhoDen() != null) dto.setTenKhoDen(pc.getKhoDen().getTenKho());

            // Tóm tắt sản phẩm: [Mã SP] Tên SP xSL
            Map<String, Integer> spCountMap = new LinkedHashMap<>();
            int tongSL = 0;

            if (pc.getDanhSachChiTiet() != null) {
                tongSL = pc.getDanhSachChiTiet().size();
                for (ChiTietPhieuChuyen ct : pc.getDanhSachChiTiet()) {
                    if (ct.getSanPham() != null) {
                        String key = "[" + ct.getSanPham().getMaSP() + "] " + ct.getSanPham().getTenSP();
                        spCountMap.put(key, spCountMap.getOrDefault(key, 0) + 1);
                    }
                }
            }
            dto.setTongSoLuong(tongSL);

            List<String> summaryParts = new ArrayList<>();
            for (String key : spCountMap.keySet()) {
                summaryParts.add(key + " x" + spCountMap.get(key));
            }
            dto.setTomTatSanPham(String.join(", ", summaryParts));

            listDto.add(dto);
        }
        return listDto;
    }

    // 2. LẤY CHI TIẾT 1 PHIẾU
    public PhieuChuyen layChiTietPhieuChuyen(String soPhieu) {
        return phieuChuyenDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu chuyển: " + soPhieu));
    }

    // 3. THỰC HIỆN CHUYỂN KHO (TẠO MỚI)
    @Transactional(rollbackFor = Exception.class)
    public PhieuChuyen thucHienChuyenKho(ChuyenKhoDTO dto) {
        // Validate
        if (dto.getMaKhoDi().equals(dto.getMaKhoDen())) {
            throw new RuntimeException("Kho đi và Kho đến không được trùng nhau!");
        }
        if (dto.getDanhSachSerial() == null || dto.getDanhSachSerial().isEmpty()) {
            throw new RuntimeException("Chưa chọn máy nào để chuyển!");
        }

        Kho khoDi = khoDAO.findById(dto.getMaKhoDi()).orElseThrow(() -> new RuntimeException("Kho đi lỗi"));
        Kho khoDen = khoDAO.findById(dto.getMaKhoDen()).orElseThrow(() -> new RuntimeException("Kho đến lỗi"));

        // Sinh mã phiếu: CK + YYYYMM + STT
        String prefix = "CK" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String lastId = phieuChuyenDAO.findLastId(prefix).orElse(null);
        String soPhieuMoi = idGenerator.generateNextId("CK", lastId);

        // Lưu Header
        PhieuChuyen phieu = new PhieuChuyen();
        phieu.setSoPhieu(soPhieuMoi);
        phieu.setNgayChuyen(LocalDateTime.now());
        phieu.setKhoDi(khoDi);
        phieu.setKhoDen(khoDen);
        phieu.setGhiChu(dto.getGhiChu());

        PhieuChuyen savedPhieu = phieuChuyenDAO.save(phieu);

        // Xử lý từng máy
        List<ChiTietPhieuChuyen> listChiTiet = new ArrayList<>();
        for (String serial : dto.getDanhSachSerial()) {
            MayIn may = mayInDAO.findById(serial)
                    .orElseThrow(() -> new RuntimeException("Máy " + serial + " không tồn tại!"));

            // Validate logic kho
            if (!may.getKho().getMaKho().equals(khoDi.getMaKho())) {
                throw new RuntimeException("Lỗi: Máy " + serial + " không nằm trong kho đi!");
            }
            if (!Boolean.TRUE.equals(may.getTonKho())) {
                throw new RuntimeException("Lỗi: Máy " + serial + " đã bán, không thể chuyển!");
            }

            // A. Cập nhật vị trí máy sang Kho Đến
            may.setKho(khoDen);
            mayInDAO.save(may);

            // B. Lưu chi tiết lịch sử
            ChiTietPhieuChuyen ct = new ChiTietPhieuChuyen();
            ct.setPhieuChuyen(savedPhieu);
            ct.setMayIn(may);
            ct.setSanPham(may.getSanPham());
            chiTietPhieuChuyenDAO.save(ct);

            listChiTiet.add(ct);
        }

        savedPhieu.setDanhSachChiTiet(listChiTiet);
        return savedPhieu;
    }
    // =============================================================
    // [MỚI] XÓA PHIẾU CHUYỂN (Nút xóa bên giao diện Chuyển Kho)
    // Logic: Hoàn trả máy về Kho Đi (Undo)
    // =============================================================
    @Transactional(rollbackFor = Exception.class)
    public void xoaPhieuChuyen(String soPhieu) {
        PhieuChuyen phieuChuyen = phieuChuyenDAO.findById(soPhieu)
                .orElseThrow(() -> new RuntimeException("Phiếu chuyển không tồn tại"));

        Kho khoDi = phieuChuyen.getKhoDi(); // Kho gốc ban đầu

        // Duyệt qua danh sách máy trong phiếu chuyển
        for (ChiTietPhieuChuyen ct : phieuChuyen.getDanhSachChiTiet()) {
            MayIn may = ct.getMayIn();

            // Nếu máy vẫn còn tồn kho (chưa bán), ta trả nó về Kho Gốc
            if (may != null && Boolean.TRUE.equals(may.getTonKho())) {
                may.setKho(khoDi); // Cập nhật lại kho
                mayInDAO.save(may);
            }
            // Nếu máy đã bán rồi thì thôi, chỉ xóa lịch sử chuyển.
        }

        // Xóa phiếu chuyển (Chi tiết sẽ tự xóa do Cascade hoặc xóa tay nếu config khác)
        phieuChuyenDAO.delete(phieuChuyen);
    }
}