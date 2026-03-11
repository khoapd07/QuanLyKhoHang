package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.HangSanXuatDAO;
import com.poly.quanlykhohang.dao.KhoDAO;
import com.poly.quanlykhohang.dao.LoaiSanPhamDAO;
import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dao.SanPhamDAO;
import com.poly.quanlykhohang.dao.TrangThaiDAO;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.ChiTietPhieuNhap;
import com.poly.quanlykhohang.entity.HangSanXuat;
import com.poly.quanlykhohang.entity.LoaiSanPham;
import com.poly.quanlykhohang.entity.PhieuNhap;
import com.poly.quanlykhohang.entity.SanPham;
import com.poly.quanlykhohang.entity.TrangThai;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ThongKeExcelService {

    @Autowired
    private GiaoDichKhoService giaoDichKhoService;

    @Autowired
    private MayInDAO mayInDAO;

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

    @Transactional(rollbackFor = Exception.class)
    public List<BaoCaoXuatNhapTonDTO> processAndImportExcel(MultipartFile file, Integer maKhoTuGiaoDien, LocalDateTime ngayTaoPhieuTuGiaoDien) throws Exception {
        List<BaoCaoXuatNhapTonDTO> list = new ArrayList<>();

        Map<String, ChiTietNhapDTO> mapNhap = new LinkedHashMap<>();
        Map<String, ChiTietXuatDTO> mapXuat = new LinkedHashMap<>();

        // NẾU TỪ GIAO DIỆN CÓ CHỌN NGÀY THÌ LẤY NGÀY ĐÓ, KHÔNG THÌ LẤY TẠM NGÀY HIỆN TẠI
        LocalDateTime ngayGiaoDich = (ngayTaoPhieuTuGiaoDien != null) ? ngayTaoPhieuTuGiaoDien : LocalDateTime.now();
        int finalMaKho = (maKhoTuGiaoDien != null && maKhoTuGiaoDien > 0) ? maKhoTuGiaoDien : 1;

        List<TrangThai> danhSachTrangThaiDB = trangThaiDAO.findAll();
        List<SanPham> danhSachSanPhamDB = sanPhamDAO.findAll();
        List<HangSanXuat> danhSachHangDB = hangSanXuatDAO.findAll();

        danhSachSanPhamDB.sort((sp1, sp2) -> Integer.compare(sp2.getMaSP().length(), sp1.getMaSP().length()));

        LoaiSanPham loaiMayVanPhong = loaiSanPhamDAO.findAll().stream()
                .filter(l -> l.getTenLoai().equalsIgnoreCase("Máy Văn Phòng"))
                .findFirst()
                .orElseGet(() -> {
                    LoaiSanPham newLoai = new LoaiSanPham();
                    newLoai.setTenLoai("Máy Văn Phòng");
                    return loaiSanPhamDAO.save(newLoai);
                });

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // NẾU GIAO DIỆN KHÔNG CHỌN NGÀY -> VẪN THỬ LẤY TỪ EXCEL CHO CHẮC CÚ
            if (ngayTaoPhieuTuGiaoDien == null) {
                Row dateRow = sheet.getRow(2);
                if (dateRow != null) {
                    String ngayKetThucStr = getStringValue(dateRow.getCell(2));
                    if (ngayKetThucStr != null && !ngayKetThucStr.isEmpty()) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate parsedDate = LocalDate.parse(ngayKetThucStr.trim(), formatter);
                            ngayGiaoDich = parsedDate.atTime(12, 0, 0);
                        } catch (Exception e) {
                            System.err.println("Lỗi parse ngày từ Excel");
                        }
                    }
                }
            }

            int startRow = 5;
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                if (firstCell == null || firstCell.getCellType() == CellType.BLANK) continue;
                if (firstCell.getCellType() == CellType.STRING && firstCell.getStringCellValue().trim().startsWith("Tổng")) break;

                int[] requiredCols = {1, 2, 3, 4, 5, 6, 8};
                for (int col : requiredCols) {
                    Cell cell = row.getCell(col);
                    if (cell == null || cell.getCellType() == CellType.BLANK || cell.toString().trim().isEmpty()) {
                        throw new Exception("Dữ liệu ở Dòng số " + (i + 1) + ", Cột số " + (col + 1) + " đang bị bỏ trống!");
                    }
                }

                long tonDau = getNumericValue(row.getCell(4)).longValue();
                long nhapTrong = getNumericValue(row.getCell(5)).longValue();
                long xuatTrong = getNumericValue(row.getCell(6)).longValue();
                BigDecimal donGia = getBigDecimalValue(row.getCell(8));

                if (tonDau < 0 || nhapTrong < 0 || xuatTrong < 0 || donGia.compareTo(BigDecimal.ZERO) < 0) {
                    throw new Exception("Lỗi ở Dòng số " + (i + 1) + ": Số lượng và Đơn giá KHÔNG ĐƯỢC LÀ SỐ ÂM!");
                }

                if (xuatTrong > (tonDau + nhapTrong)) {
                    throw new Exception("Lỗi ở Dòng số " + (i + 1) + ": Số lượng XUẤT (" + xuatTrong + ") lớn hơn CÓ SẴN (" + (tonDau + nhapTrong) + ").");
                }

                BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
                dto.setStt(getNumericValue(row.getCell(0)).intValue());

                String maExcel = getStringValue(row.getCell(1)).trim();
                String tenExcel = getStringValue(row.getCell(2)).trim();
                String donViTinhExcel = getStringValue(row.getCell(3)).trim();

                String matchedMaSP = null;
                String maExcelUpper = maExcel.toUpperCase();

                for (SanPham sp : danhSachSanPhamDB) {
                    if (maExcelUpper.equals(sp.getMaSP().toUpperCase())) {
                        matchedMaSP = sp.getMaSP();
                        break;
                    }
                }

                if (matchedMaSP == null) {
                    for (SanPham sp : danhSachSanPhamDB) {
                        if (maExcelUpper.contains(sp.getMaSP().toUpperCase())) {
                            matchedMaSP = sp.getMaSP();
                            break;
                        }
                    }
                }

                if (matchedMaSP == null) {
                    SanPham newSp = new SanPham();

                    String maSPSach = maExcel.replaceAll("(?i)-(thu\\s*hoi|new|xac|xác|like\\s*new|cu|cũ|hỏng).*$", "").trim();
                    String tenSPSach = tenExcel.replaceAll("(?i)\\s*-\\s*(thu\\s*hồi|thu\\s*hoi|new|xác|xac|like\\s*new|cu|cũ|hỏng)\\s*$", "").trim();
                    if (tenSPSach.endsWith("-")) {
                        tenSPSach = tenSPSach.substring(0, tenSPSach.length() - 1).trim();
                    }

                    newSp.setMaSP(maSPSach);
                    newSp.setTenSP(tenSPSach);
                    newSp.setSoLuong(0);
                    newSp.setDonViTinh(donViTinhExcel.isEmpty() ? "Cái" : donViTinhExcel);
                    newSp.setLoaiSanPham(loaiMayVanPhong);

                    HangSanXuat hangDuocChon = timTenHangTuTenSanPham(tenSPSach, danhSachHangDB);
                    newSp.setHangSanXuat(hangDuocChon);

                    sanPhamDAO.save(newSp);
                    sanPhamDAO.flush();

                    danhSachSanPhamDB.add(newSp);
                    danhSachSanPhamDB.sort((sp1, sp2) -> Integer.compare(sp2.getMaSP().length(), sp1.getMaSP().length()));

                    matchedMaSP = maSPSach;
                }

                dto.setMaSP(matchedMaSP);
                dto.setTenSP(tenExcel);
                dto.setDonvitinh(donViTinhExcel);

                long tonCuoi = tonDau + nhapTrong - xuatTrong;
                dto.setTonDau(tonDau);
                dto.setNhapTrong(nhapTrong);
                dto.setXuatTrong(xuatTrong);
                dto.setTonCuoi(tonCuoi);
                dto.setGiaBQ(donGia);
                dto.setThanhTien(donGia.multiply(BigDecimal.valueOf(tonCuoi)));
                list.add(dto);

                int trangThaiMatch = 2;
                String chuoiCanDo = (maExcel + " " + tenExcel).toLowerCase();

                for (TrangThai tt : danhSachTrangThaiDB) {
                    String tenTT = tt.getTenTrangThai().toLowerCase().trim();
                    if(tenTT.equals("chưa xác định") || tenTT.equals("bình thường") || tenTT.isEmpty()) continue;

                    if (chuoiCanDo.contains(tenTT)) {
                        trangThaiMatch = tt.getMaTrangThai();
                        break;
                    }
                }

                long slNhapCanTao = tonDau + nhapTrong;
                if (slNhapCanTao > 0) {
                    String keyNhap = matchedMaSP + "_" + trangThaiMatch;
                    if (mapNhap.containsKey(keyNhap)) {
                        ChiTietNhapDTO ext = mapNhap.get(keyNhap);
                        ext.setSoLuong(ext.getSoLuong() + (int) slNhapCanTao);
                    } else {
                        ChiTietNhapDTO ctn = new ChiTietNhapDTO();
                        ctn.setMaSP(matchedMaSP);
                        ctn.setSoLuong((int) slNhapCanTao);
                        ctn.setDonGia(donGia);
                        ctn.setTrangThai(trangThaiMatch);
                        ctn.setGhiChu("Nhập tự động (TĐK + NTK)");
                        mapNhap.put(keyNhap, ctn);
                    }
                }

                long slXuatCanTao = xuatTrong;
                if (slXuatCanTao > 0) {
                    if (mapXuat.containsKey(matchedMaSP)) {
                        ChiTietXuatDTO ext = mapXuat.get(matchedMaSP);
                        for (int k = 0; k < slXuatCanTao; k++) ext.getDanhSachSeri().add("TEMP");
                    } else {
                        ChiTietXuatDTO ctx = new ChiTietXuatDTO();
                        ctx.setMaSP(matchedMaSP);
                        ctx.setDonGia(donGia);
                        List<String> tempQty = new ArrayList<>();
                        for (int k = 0; k < slXuatCanTao; k++) tempQty.add("TEMP");
                        ctx.setDanhSachSeri(tempQty);
                        mapXuat.put(matchedMaSP, ctx);
                    }
                }
            }
        }

        PhieuNhap phieuNhapDaLuu = null;
        if (!mapNhap.isEmpty()) {
            PhieuNhapDTO phieuNhapGiaLap = new PhieuNhapDTO();
            phieuNhapGiaLap.setMaKho(finalMaKho);
            phieuNhapGiaLap.setMaDonVi("NCC_MACDINH");
            phieuNhapGiaLap.setGhiChu("Phiếu nhập tự động từ Import Excel");
            phieuNhapGiaLap.setNgayTaoPhieu(ngayGiaoDich);
            phieuNhapGiaLap.setChiTietPhieuNhap(new ArrayList<>(mapNhap.values()));

            phieuNhapDaLuu = giaoDichKhoService.nhapKho(phieuNhapGiaLap);
            mayInDAO.flush();
        }

        Map<String, List<String>> poolMayVuaTao = new HashMap<>();
        if (phieuNhapDaLuu != null && phieuNhapDaLuu.getDanhSachChiTiet() != null) {
            for (ChiTietPhieuNhap ct : phieuNhapDaLuu.getDanhSachChiTiet()) {
                if (ct.getSanPham() != null && ct.getMayIn() != null) {
                    String sp = ct.getSanPham().getMaSP().trim();
                    String maMay = ct.getMayIn().getMaMay();
                    poolMayVuaTao.computeIfAbsent(sp, k -> new ArrayList<>()).add(maMay);
                }
            }
        }

        if (!mapXuat.isEmpty()) {
            PhieuXuatDTO phieuXuatGiaLap = new PhieuXuatDTO();
            phieuXuatGiaLap.setMaKho(finalMaKho);
            phieuXuatGiaLap.setMaDonVi("KH_MACDINH");
            phieuXuatGiaLap.setGhiChu("Phiếu xuất tự động từ Import Excel");
            phieuXuatGiaLap.setNgayTaoPhieu(ngayGiaoDich);

            List<ChiTietXuatDTO> chiTietXuatThucTeList = new ArrayList<>();

            for (ChiTietXuatDTO yeuCau : mapXuat.values()) {
                int slCanXuat = yeuCau.getDanhSachSeri().size();
                String sp = yeuCau.getMaSP();

                List<String> pool = poolMayVuaTao.getOrDefault(sp, new ArrayList<>());
                List<String> seriDeXuat = new ArrayList<>();

                for (int i = 0; i < slCanXuat && !pool.isEmpty(); i++) {
                    seriDeXuat.add(pool.remove(0));
                }

                yeuCau.setDanhSachSeri(seriDeXuat);

                if (!seriDeXuat.isEmpty()) {
                    chiTietXuatThucTeList.add(yeuCau);
                }
            }

            if (!chiTietXuatThucTeList.isEmpty()) {
                phieuXuatGiaLap.setChiTietPhieuXuat(chiTietXuatThucTeList);
                giaoDichKhoService.xuatKho(phieuXuatGiaLap);
            }
        }

        return list;
    }

    private HangSanXuat timTenHangTuTenSanPham(String tenSP, List<HangSanXuat> danhSachHangDB) {
        if (tenSP == null || tenSP.trim().isEmpty()) {
            return getOrTaoHangKhac(danhSachHangDB);
        }
        String tenSPLower = tenSP.toLowerCase();

        for (HangSanXuat hang : danhSachHangDB) {
            String tenHangDB = hang.getTenHang().toLowerCase().trim();
            if (tenHangDB.equals("khác")) continue;

            if (tenSPLower.contains(tenHangDB)) {
                return hang;
            }
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

    private String getStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private Double getNumericValue(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue().replace(",", "")); } catch (NumberFormatException e) { return 0.0; }
        }
        return 0.0;
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) {
            try { return new BigDecimal(cell.getStringCellValue().replace(",", "")); } catch (Exception e) { return BigDecimal.ZERO; }
        }
        return BigDecimal.ZERO;
    }
}