package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PhieuNhapResponseDTO {
    private String soPhieu;
    private LocalDateTime ngayNhap;
    private String tenKho;
    private String ghiChu;
    private String tenKhachHang; // Tên NCC

    // Tổng ban đầu (Lịch sử)
    private Integer tongSoLuongMay;
    private BigDecimal tongTien;

    // [MỚI] Hiện tại (Thực tế trong kho)
    private Integer soLuongConLai;
    private BigDecimal tienConLai;

    private String tomTatSanPham;
    private String danhSachHang;
}