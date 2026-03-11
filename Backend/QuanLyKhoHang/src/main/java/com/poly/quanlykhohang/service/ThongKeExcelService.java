package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.KhoDAO;
import com.poly.quanlykhohang.dao.MayInDAO;
import com.poly.quanlykhohang.dto.*;
import com.poly.quanlykhohang.entity.ChiTietPhieuNhap;
import com.poly.quanlykhohang.entity.Kho;
import com.poly.quanlykhohang.entity.PhieuNhap;
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

    @Transactional(rollbackFor = Exception.class)
    public List<BaoCaoXuatNhapTonDTO> processAndImportExcel(MultipartFile file, Integer maKhoTuGiaoDien) throws Exception {
        List<BaoCaoXuatNhapTonDTO> list = new ArrayList<>();

        Map<String, ChiTietNhapDTO> mapNhap = new LinkedHashMap<>();
        Map<String, ChiTietXuatDTO> mapXuat = new LinkedHashMap<>();

        LocalDateTime ngayGiaoDich = LocalDateTime.now();
        int finalMaKho = (maKhoTuGiaoDien != null && maKhoTuGiaoDien > 0) ? maKhoTuGiaoDien : 1;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. ĐỌC NGÀY KẾT THÚC TỪ EXCEL
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

            // 2. ĐỌC DỮ LIỆU
            int startRow = 5;
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell firstCell = row.getCell(0);
                if (firstCell == null || firstCell.getCellType() == CellType.BLANK) continue;
                if (firstCell.getCellType() == CellType.STRING && firstCell.getStringCellValue().trim().startsWith("Tổng")) break;

                // --- VALIDATE 1: KHÔNG ĐƯỢC ĐỂ TRỐNG ---
                int[] requiredCols = {1, 2, 3, 4, 5, 6, 8};
                for (int col : requiredCols) {
                    Cell cell = row.getCell(col);
                    if (cell == null || cell.getCellType() == CellType.BLANK || cell.toString().trim().isEmpty()) {
                        throw new Exception("Dữ liệu ở Dòng số " + (i + 1) + ", Cột số " + (col + 1) + " đang bị bỏ trống! Vui lòng điền đầy đủ.");
                    }
                }

                long tonDau = getNumericValue(row.getCell(4)).longValue();
                long nhapTrong = getNumericValue(row.getCell(5)).longValue();
                long xuatTrong = getNumericValue(row.getCell(6)).longValue();
                BigDecimal donGia = getBigDecimalValue(row.getCell(8));

                // --- VALIDATE 2: KHÔNG ĐƯỢC LÀ SỐ ÂM ---
                if (tonDau < 0 || nhapTrong < 0 || xuatTrong < 0 || donGia.compareTo(BigDecimal.ZERO) < 0) {
                    throw new Exception("Lỗi ở Dòng số " + (i + 1) + ": Số lượng và Đơn giá KHÔNG ĐƯỢC LÀ SỐ ÂM!");
                }

                // --- VALIDATE 3: XUẤT TRONG KỲ KHÔNG ĐƯỢC LỚN HƠN (TỒN ĐẦU + NHẬP) ---
                if (xuatTrong > (tonDau + nhapTrong)) {
                    throw new Exception("Lỗi ở Dòng số " + (i + 1) + " (" + getStringValue(row.getCell(2)) + "): Số lượng XUẤT (" + xuatTrong + ") đang lớn hơn tổng số lượng CÓ SẴN (" + (tonDau + nhapTrong) + "). Không thể xuất âm kho!");
                }

                BaoCaoXuatNhapTonDTO dto = new BaoCaoXuatNhapTonDTO();
                dto.setStt(getNumericValue(row.getCell(0)).intValue());

                String maSanPham = getStringValue(row.getCell(1)).trim();
                dto.setMaSP(maSanPham);
                dto.setTenSP(getStringValue(row.getCell(2)));
                dto.setDonvitinh(getStringValue(row.getCell(3)));

                // TỰ TÍNH TOÁN TỒN CUỐI: TĐK + NTK - XTK
                long tonCuoi = tonDau + nhapTrong - xuatTrong;

                dto.setTonDau(tonDau);
                dto.setNhapTrong(nhapTrong);
                dto.setXuatTrong(xuatTrong);
                dto.setTonCuoi(tonCuoi);
                dto.setGiaBQ(donGia);
                dto.setThanhTien(donGia.multiply(BigDecimal.valueOf(tonCuoi)));
                list.add(dto);

                int trangThai = 1;
                String tenSP = dto.getTenSP().toLowerCase();
                if (tenSP.contains("new") && !tenSP.contains("like")) trangThai = 2;
                else if (tenSP.contains("like new")) trangThai = 3;
                else if (tenSP.contains("thu hồi")) trangThai = 6;

                // GOM SỐ LƯỢNG NHẬP
                long slNhapCanTao = tonDau + nhapTrong;
                if (slNhapCanTao > 0) {
                    String keyNhap = maSanPham + "_" + trangThai;
                    if (mapNhap.containsKey(keyNhap)) {
                        ChiTietNhapDTO ext = mapNhap.get(keyNhap);
                        ext.setSoLuong(ext.getSoLuong() + (int) slNhapCanTao);
                    } else {
                        ChiTietNhapDTO ctn = new ChiTietNhapDTO();
                        ctn.setMaSP(maSanPham);
                        ctn.setSoLuong((int) slNhapCanTao);
                        ctn.setDonGia(donGia);
                        ctn.setTrangThai(trangThai);
                        ctn.setGhiChu("Nhập giả lập (TĐK + NTK)");
                        mapNhap.put(keyNhap, ctn);
                    }
                }

                // GOM SỐ LƯỢNG XUẤT
                long slXuatCanTao = xuatTrong;
                if (slXuatCanTao > 0) {
                    if (mapXuat.containsKey(maSanPham)) {
                        ChiTietXuatDTO ext = mapXuat.get(maSanPham);
                        for (int k = 0; k < slXuatCanTao; k++) ext.getDanhSachSeri().add("TEMP");
                    } else {
                        ChiTietXuatDTO ctx = new ChiTietXuatDTO();
                        ctx.setMaSP(maSanPham);
                        ctx.setDonGia(donGia);
                        List<String> tempQty = new ArrayList<>();
                        for (int k = 0; k < slXuatCanTao; k++) tempQty.add("TEMP");
                        ctx.setDanhSachSeri(tempQty);
                        mapXuat.put(maSanPham, ctx);
                    }
                }
            }
        }

        // ===============================================
        // BƯỚC 1: LƯU PHIẾU NHẬP
        // ===============================================
        PhieuNhap phieuNhapDaLuu = null;
        if (!mapNhap.isEmpty()) {
            PhieuNhapDTO phieuNhapGiaLap = new PhieuNhapDTO();
            phieuNhapGiaLap.setMaKho(finalMaKho);
            phieuNhapGiaLap.setMaDonVi("NCC_MACDINH");
            phieuNhapGiaLap.setGhiChu("Phiếu nhập tự động từ Import Excel");
            phieuNhapGiaLap.setNgayTaoPhieu(ngayGiaoDich);
            phieuNhapGiaLap.setChiTietPhieuNhap(new ArrayList<>(mapNhap.values()));

            phieuNhapDaLuu = giaoDichKhoService.nhapKho(phieuNhapGiaLap);
        }

        // ===============================================
        // BƯỚC 2: TÓM GỌN CÁC SERIAL VỪA SINH RA TRÊN RAM
        // ===============================================
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

        // ===============================================
        // BƯỚC 3: RÚT MÁY RA ĐỂ TẠO PHIẾU XUẤT
        // ===============================================
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