package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.*;
import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import com.poly.quanlykhohang.dto.SyncTonKhoDTO;
import com.poly.quanlykhohang.entity.*;
import com.poly.quanlykhohang.service.ThongKeExcelService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
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

                // ==============================================================
                // FIX LỖI BỎ SÓT: Nhận diện điểm dừng an toàn hơn
                // Chỉ dừng khi cột STT hoặc Mã SP chứa chữ "tổng",
                // hoặc cột Tên SP ghi đích danh "tổng cộng" (Tránh cắt nhầm máy tên "Tổng...")
                // ==============================================================
                if (c0.contains("tổng") || c1.toLowerCase().contains("tổng") || c2.toLowerCase().startsWith("tổng cộng")) {
                    break;
                }

                // Nếu thiếu cả Mã SP và Tên SP thì bỏ qua (Tránh đọc các dòng trống)
                if (c1.isEmpty() || c2.isEmpty() || c2.toLowerCase().contains("tên sản phẩm")) {
                    continue;
                }

                try {
                    Cell cell3 = row.getCell(3);
                    String donViTinhExcel = cell3 != null ? dataFormatter.formatCellValue(cell3).trim() : "Cái";
                    if (donViTinhExcel.isEmpty()) donViTinhExcel = "Cái";

                    long tonCuoi = getNumericValueSafely(row.getCell(7)).longValue(); // Cột H
                    BigDecimal thanhTien = getBigDecimalValueSafely(row.getCell(9));  // Cột J

                    if (tonCuoi < 0) {
                        throw new Exception("Tồn cuối bị ÂM (" + tonCuoi + "). Vui lòng kiểm tra lại Excel tại Mã SP: " + c1);
                    }

                    String maExcelRaw = c1;
                    String tenExcelRaw = c2;
                    String tenExcelLower = tenExcelRaw.toLowerCase();

                    // ==============================================================
                    // 1. NHẬN DIỆN TRẠNG THÁI (Ưu tiên Likenew)
                    // ==============================================================
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

                    // Dọn dẹp các ký tự rác (dấu trừ, khoảng trắng) ở cuối tên
                    maExcelClean = maExcelClean.replaceAll("[^\\p{L}\\p{N}]+$", "").trim();
                    tenExcelClean = tenExcelClean.replaceAll("[^\\p{L}\\p{N}]+$", "").trim();

                    // ==============================================================
// 2. TÌM HOẶC TẠO SẢN PHẨM MỚI
// ==============================================================
                    String matchedMaSP = null;
                    String maExcelUpper = maExcelClean.toUpperCase();

// 1. Chỉ tìm khớp chính xác tuyệt đối (Equals)
                    for (SanPham sp : danhSachSanPhamDB) {
                        if (maExcelUpper.equals(sp.getMaSP().toUpperCase())) {
                            matchedMaSP = sp.getMaSP(); break;
                        }
                    }

// KHÔNG DÙNG HÀM .contains() NỮA ĐỂ TRÁNH NHẬN DIỆN SAI MÃ CON VÀ MÃ CHA

// 2. Nếu không khớp chính xác mã nào -> TẠO MỚI LUÔN
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

                    // ==============================================================
                    // 3. LƯU CHỐT SỔ VÀO DATABASE (ĐÃ BỎ ĐỂ CHUYỂN SANG NÚT LƯU RIÊNG)
                    // ==============================================================
                    /*
                    DMTonKhoID id = new DMTonKhoID(finalNam, matchedMaSP, finalMaKho, trangThaiMatch);
                    Optional<DMTonKho> optTonKho = dmTonKhoDAO.findById(id);
                    DMTonKho tonKho;

                    if (optTonKho.isPresent()) {
                        tonKho = optTonKho.get();
                    } else {
                        tonKho = new DMTonKho();
                        tonKho.setNam(finalNam);
                        tonKho.setMaSP(matchedMaSP);
                        tonKho.setMaKho(finalMaKho);
                        tonKho.setMaTrangThai(trangThaiMatch);
                    }

                    tonKho.setSoLuong((int) tonCuoi);
                    tonKho.setGiaTri(thanhTien);
                    dmTonKhoDAO.save(tonKho);
                    */

                    // ==============================================================
                    // 4. TRẢ DỮ LIỆU LÊN GIAO DIỆN
                    // ==============================================================
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
                    dto.setMaTrangThai(trangThaiMatch); // Truyền trạng thái về frontend
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

    // ==============================================================
    // CÁC HÀM XỬ LÝ MỚI THÊM VÀO DƯỚI ĐÂY
    // ==============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncToDMTonKho(List<SyncTonKhoDTO> payload) throws Exception {
        for (SyncTonKhoDTO item : payload) {
            // SỬA SỐ 1 THÀNH SỐ 2 (ID 2 là Bình thường)
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

            // Cập nhật số lượng (tồn cuối) và giá trị
            tonKho.setSoLuong(item.getSoLuong());
            tonKho.setGiaTri(item.getGiaTri() != null ? item.getGiaTri() : BigDecimal.ZERO);
            dmTonKhoDAO.save(tonKho);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void xoaTonKhoTheoNam(Integer nam, Integer maKho) throws Exception {
        dmTonKhoDAO.deleteByNamAndMaKho(nam, maKho);
    }
}