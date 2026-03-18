package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import com.poly.quanlykhohang.dto.SyncTonKhoDTO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.ThongKeExcelService;
import com.poly.quanlykhohang.util.IdGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ThongKeExcelServiceImpl implements ThongKeExcelService {

    @Autowired
    private KhoDAO khoDAO;

    @Autowired
    private TrangThaiDAO trangThaiDAO;

    @Autowired
    private SanPhamDAO sanPhamDAO;

    @Autowired
    private HangSanXuatDAO hangSanXuatDAO;

    @Autowired
    private LoaiSanPhamDAO loaiSanPhamDAO;

    @Autowired
    private DMTonKhoDAO dmTonKhoDAO;

    @Autowired
    private PhieuNhapDAO phieuNhapDAO;

    @Autowired
    private ChiTietPhieuNhapDAO chiTietPhieuNhapDAO;

    @Autowired
    private MayInDAO mayInDAO;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BaoCaoXuatNhapTonDTO> processAndImportExcel(MultipartFile file, Integer maKhoTuGiaoDien, Integer namLuuTon) throws Exception {
        List<BaoCaoXuatNhapTonDTO> list = new ArrayList<>();

        int finalMaKho = (maKhoTuGiaoDien != null && maKhoTuGiaoDien > 0) ? maKhoTuGiaoDien : 1;
        int finalNam = (namLuuTon != null && namLuuTon > 0) ? namLuuTon : LocalDate.now().getYear();

        List<TrangThai> danhSachTrangThaiDB = trangThaiDAO.findAll();
        List<SanPham> danhSachSanPhamDB = sanPhamDAO.findAll();
        List<HangSanXuat> danhSachHangDB = hangSanXuatDAO.findAll();

        danhSachSanPhamDB.sort((sp1, sp2) -> Integer.compare(sp2.getMaSP().length(), sp1.getMaSP().length()));
        danhSachTrangThaiDB.sort((tt1, tt2) -> Integer.compare(tt2.getTenTrangThai().length(), tt1.getTenTrangThai().length()));

        LoaiSanPham loaiMayVanPhong = loaiSanPhamDAO.findAll().stream()
                .filter(l -> l.getTenLoai().equalsIgnoreCase("Máy Văn Phòng"))
                .findFirst()
                .orElseGet(() -> {
                    LoaiSanPham newLoai = new LoaiSanPham();
                    newLoai.setTenLoai("Máy Văn Phòng");
                    return loaiSanPhamDAO.save(newLoai);
                });

        DataFormatter dataFormatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                Cell cell2 = row.getCell(2);

                String c0 = cell0 != null ? dataFormatter.formatCellValue(cell0).trim().toLowerCase() : "";
                String c1 = cell1 != null ? dataFormatter.formatCellValue(cell1).trim() : "";
                String c2 = cell2 != null ? dataFormatter.formatCellValue(cell2).trim() : "";

                if (c0.contains("tổng") || c1.toLowerCase().contains("tổng") || c2.toLowerCase().startsWith("tổng cộng")) {
                    break;
                }

                if (c1.isEmpty() || c2.isEmpty() || c2.toLowerCase().contains("tên sản phẩm")) {
                    continue;
                }

                try {
                    Cell cell3 = row.getCell(3);
                    String donViTinhExcel = cell3 != null ? dataFormatter.formatCellValue(cell3).trim() : "Cái";
                    if (donViTinhExcel.isEmpty()) donViTinhExcel = "Cái";

                    long tonCuoi = getNumericValueSafely(row.getCell(7)).longValue();
                    BigDecimal thanhTien = getBigDecimalValueSafely(row.getCell(9));

                    if (tonCuoi < 0) {
                        throw new Exception("Tồn cuối bị ÂM (" + tonCuoi + "). Vui lòng kiểm tra lại Excel tại Mã SP: " + c1);
                    }

                    String maExcelRaw = c1;
                    String tenExcelRaw = c2;
                    String tenExcelLower = tenExcelRaw.toLowerCase();

                    int trangThaiMatch = 2; // Mặc định: 1 (Bình Thường)
                    String maExcelClean = maExcelRaw;
                    String tenExcelClean = tenExcelRaw;
                    boolean isLikeNew = false;

                    if (tenExcelLower.contains("likenew") || tenExcelLower.contains("like new")) {
                        isLikeNew = true;
                        for (TrangThai tt : danhSachTrangThaiDB) {
                            String ttName = tt.getTenTrangThai().toLowerCase().replace(" ", "");
                            if (ttName.contains("likenew")) {
                                trangThaiMatch = tt.getMaTrangThai();
                                break;
                            }
                        }
                        maExcelClean = maExcelClean.replaceAll("(?i)like\\s*new", "");
                        tenExcelClean = tenExcelClean.replaceAll("(?i)like\\s*new", "");
                    }

                    if (!isLikeNew) {
                        for (TrangThai tt : danhSachTrangThaiDB) {
                            String tenTT = tt.getTenTrangThai().toLowerCase().trim();
                            if(tenTT.equals("chưa xác định") || tenTT.equals("bình thường") || tenTT.isEmpty()) continue;

                            List<String> keywords = new ArrayList<>();
                            keywords.add(tenTT);
                            if (tenTT.contains("(") && tenTT.contains(")")) {
                                keywords.add(tenTT.substring(tenTT.indexOf("(") + 1, tenTT.indexOf(")")).trim());
                            }

                            boolean isMatched = false;
                            for (String kw : keywords) {
                                if (tenExcelLower.contains(kw)) {
                                    trangThaiMatch = tt.getMaTrangThai();
                                    String regexRemove = "(?i)" + java.util.regex.Pattern.quote(kw);
                                    maExcelClean = maExcelClean.replaceAll(regexRemove, "");
                                    tenExcelClean = tenExcelClean.replaceAll(regexRemove, "");
                                    isMatched = true;
                                    break;
                                }
                            }
                            if (isMatched) break;
                        }
                    }

                    maExcelClean = maExcelClean.replaceAll("[^\\p{L}\\p{N}]+$", "").trim();
                    tenExcelClean = tenExcelClean.replaceAll("[^\\p{L}\\p{N}]+$", "").trim();

                    String matchedMaSP = null;
                    String maExcelUpper = maExcelClean.toUpperCase();

                    for (SanPham sp : danhSachSanPhamDB) {
                        if (maExcelUpper.equals(sp.getMaSP().toUpperCase())) {
                            matchedMaSP = sp.getMaSP(); break;
                        }
                    }

                    if (matchedMaSP == null) {
                        SanPham newSp = new SanPham();
                        newSp.setMaSP(maExcelClean);
                        newSp.setTenSP(tenExcelClean);
                        newSp.setSoLuong(0);
                        newSp.setDonViTinh(donViTinhExcel);
                        newSp.setLoaiSanPham(loaiMayVanPhong);

                        HangSanXuat hangDuocChon = timTenHangTuTenSanPham(tenExcelClean, danhSachHangDB);
                        newSp.setHangSanXuat(hangDuocChon);

                        sanPhamDAO.save(newSp);
                        sanPhamDAO.flush();

                        danhSachSanPhamDB.add(newSp);
                        danhSachSanPhamDB.sort((sp1, sp2) -> Integer.compare(sp2.getMaSP().length(), sp1.getMaSP().length()));
                        matchedMaSP = maExcelClean;
                    }

                    BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
                    dto.setStt(list.size() + 1);
                    dto.setMaSP(matchedMaSP);
                    dto.setTenSP(tenExcelRaw);
                    dto.setDonvitinh(donViTinhExcel);
                    dto.setTonDau(getNumericValueSafely(row.getCell(4)).longValue());
                    dto.setNhapTrong(getNumericValueSafely(row.getCell(5)).longValue());
                    dto.setXuatTrong(getNumericValueSafely(row.getCell(6)).longValue());
                    dto.setGiaBQ(getBigDecimalValueSafely(row.getCell(8)));
                    dto.setTonCuoi(tonCuoi);
                    dto.setThanhTien(thanhTien);
                    dto.setMaTrangThai(trangThaiMatch);
                    list.add(dto);

                } catch (Exception ex) {
                    throw new Exception("Lỗi tại STT " + c0 + " (" + c1 + ") - Chi tiết: " + ex.getMessage());
                }
            }
        }
        return list;
    }

    private HangSanXuat timTenHangTuTenSanPham(String tenSP, List<HangSanXuat> danhSachHangDB) {
        if (tenSP == null || tenSP.trim().isEmpty()) return getOrTaoHangKhac(danhSachHangDB);

        String tenSPLower = tenSP.toLowerCase();
        for (HangSanXuat hang : danhSachHangDB) {
            String tenHangDB = hang.getTenHang().toLowerCase().trim();
            if (tenHangDB.equals("khác")) continue;
            if (tenSPLower.contains(tenHangDB)) return hang;
        }

        String[] words = tenSP.split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9À-ỹ]", "").trim();
            if (word.isEmpty() || word.equalsIgnoreCase("Máy") || word.equalsIgnoreCase("In") || word.equalsIgnoreCase("Scan") || word.equalsIgnoreCase("Photocopy")) {
                continue;
            }
            char firstChar = word.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                HangSanXuat hangMoi = new HangSanXuat();
                hangMoi.setTenHang(word);
                hangMoi = hangSanXuatDAO.save(hangMoi);
                danhSachHangDB.add(hangMoi);
                return hangMoi;
            }
        }
        return getOrTaoHangKhac(danhSachHangDB);
    }

    private HangSanXuat getOrTaoHangKhac(List<HangSanXuat> danhSachHangDB) {
        for (HangSanXuat h : danhSachHangDB) {
            if (h.getTenHang().equalsIgnoreCase("Khác")) return h;
        }
        HangSanXuat hangKhac = new HangSanXuat();
        hangKhac.setTenHang("Khác");
        hangKhac = hangSanXuatDAO.save(hangKhac);
        danhSachHangDB.add(hangKhac);
        return hangKhac;
    }

    private Double getNumericValueSafely(Cell cell) {
        if (cell == null) return 0.0;
        try {
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
            String str = new DataFormatter().formatCellValue(cell).replaceAll("[^\\d.-]", "");
            return str.isEmpty() ? 0.0 : Double.parseDouble(str);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private BigDecimal getBigDecimalValueSafely(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            String str = new DataFormatter().formatCellValue(cell).replaceAll("[^\\d.-]", "");
            return str.isEmpty() ? BigDecimal.ZERO : new BigDecimal(str);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncToDMTonKho(List<SyncTonKhoDTO> payload) throws Exception {
        if (payload == null || payload.isEmpty()) return;

        Integer nam = payload.get(0).getNam();
        Integer maKho = payload.get(0).getMaKho();
        Kho khoDb = khoDAO.findById(maKho).orElseThrow(() -> new Exception("Không tìm thấy kho"));

        String soPhieuNhapAo = "PN" + nam + "0000";
        Optional<PhieuNhap> optPnCu = phieuNhapDAO.findById(soPhieuNhapAo);

        if (optPnCu.isPresent()) {
            for (ChiTietPhieuNhap ct : optPnCu.get().getDanhSachChiTiet()) {
                SanPham sp = ct.getSanPham();
                if (sp != null) {
                    sp.setSoLuong(Math.max(0, sp.getSoLuong() - 1));
                    sanPhamDAO.save(sp);
                }
            }
            sanPhamDAO.flush();
            jdbcTemplate.update("DELETE FROM CTPhieuNhap WHERE SoPhieu = ?", soPhieuNhapAo);
            jdbcTemplate.update("DELETE FROM DMMay WHERE SoPhieuNhap = ?", soPhieuNhapAo);
            jdbcTemplate.update("DELETE FROM PhieuNhap WHERE SoPhieu = ?", soPhieuNhapAo);
            entityManager.clear(); // Ép hệ thống quên đi các dữ liệu rác vừa bị xóa
        }

        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setSoPhieu(soPhieuNhapAo);
        phieuNhap.setKhoNhap(khoDb);
        phieuNhap.setNgayNhap(LocalDateTime.of(nam, 1, 1, 0, 0));
        phieuNhap.setGhiChu("Nhập tồn kho đầu kỳ năm " + nam);

        phieuNhap = phieuNhapDAO.save(phieuNhap);
        entityManager.flush(); // Lưu Phiếu nhập xuống DB ngay lập tức

        int tongSoLuongPhieu = 0;
        BigDecimal tongTienPhieu = BigDecimal.ZERO;
        List<ChiTietPhieuNhap> dsChiTiet = new ArrayList<>();

        for (SyncTonKhoDTO item : payload) {
            int trangThai = (item.getMaTrangThai() != null) ? item.getMaTrangThai() : 2;

            DMTonKhoID id = new DMTonKhoID(item.getNam(), item.getMaSP(), item.getMaKho(), trangThai);
            Optional<DMTonKho> optTonKho = dmTonKhoDAO.findById(id);

            DMTonKho tonKho;
            if (optTonKho.isPresent()) {
                tonKho = optTonKho.get();
            } else {
                tonKho = new DMTonKho();
                tonKho.setNam(item.getNam());
                tonKho.setMaKho(item.getMaKho());
                tonKho.setMaSP(item.getMaSP());
                tonKho.setMaTrangThai(trangThai);
            }
            tonKho.setSoLuong(item.getSoLuong());
            tonKho.setGiaTri(item.getGiaTri() != null ? item.getGiaTri() : BigDecimal.ZERO);
            dmTonKhoDAO.save(tonKho);

            int slMay = item.getSoLuong() != null ? item.getSoLuong() : 0;
            if (slMay > 0) {
                SanPham sp = sanPhamDAO.findById(item.getMaSP()).orElse(null);
                if (sp != null) {
                    String prefixMaMay = sp.getMaSP();
                    String lastMaMayInDB = mayInDAO.findLastId(prefixMaMay).orElse(null);

                    BigDecimal giaTriRow = item.getGiaTri() != null ? item.getGiaTri() : BigDecimal.ZERO;
                    BigDecimal donGiaBinhQuan = giaTriRow.divide(BigDecimal.valueOf(slMay), 2, RoundingMode.HALF_UP);

                    for (int i = 0; i < slMay; i++) {
                        String maMayMoi = idGenerator.generateNextId(prefixMaMay, lastMaMayInDB);
                        lastMaMayInDB = maMayMoi;

                        MayIn mayMoi = new MayIn();
                        mayMoi.setMaMay(maMayMoi);
                        mayMoi.setSoSeri(maMayMoi);
                        mayMoi.setSanPham(sp);
                        mayMoi.setKho(khoDb);
                        mayMoi.setSoPhieuNhap(soPhieuNhapAo);
                        mayMoi.setTrangThai(trangThai);
                        mayMoi.setTonKho(true);
                        mayMoi.setNgayTao(LocalDateTime.now());

                        MayIn savedMay = mayInDAO.save(mayMoi);
                        entityManager.flush(); // BẮT BUỘC CHỐT HẠ XUỐNG DB ĐỂ TRÁNH LỖI NULL

                        ChiTietPhieuNhap ctEntity = new ChiTietPhieuNhap();
                        ctEntity.setPhieuNhap(phieuNhap);
                        ctEntity.setSanPham(sp);
                        ctEntity.setMayIn(savedMay); // Móc khóa ngoại vào MayIn đã tồn tại dưới DB
                        ctEntity.setDonGia(donGiaBinhQuan);
                        ctEntity.setGhiChu("Tồn đầu kỳ " + nam);

                        chiTietPhieuNhapDAO.save(ctEntity);
                        entityManager.flush(); // BẮT BUỘC CHỐT HẠ XUỐNG DB TIẾP

                        dsChiTiet.add(ctEntity);
                    }

                    tongSoLuongPhieu += slMay;
                    tongTienPhieu = tongTienPhieu.add(giaTriRow);

                    sp.setSoLuong((sp.getSoLuong() == null ? 0 : sp.getSoLuong()) + slMay);
                    sanPhamDAO.save(sp);
                }
            }
        }

        phieuNhap.setTongSoLuong(tongSoLuongPhieu);
        phieuNhap.setTongTien(tongTienPhieu);
        phieuNhap.setDanhSachChiTiet(dsChiTiet);
        phieuNhapDAO.save(phieuNhap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void xoaTonKhoTheoNam(Integer nam, Integer maKho) throws Exception {
        dmTonKhoDAO.deleteByNamAndMaKho(nam, maKho);
    }
}