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

    private Integer tongSoLuongMay;
    private BigDecimal tongTien;

    // Hai trường quan trọng để hiển thị
    private String tomTatSanPham; // VD: "Canon 2900 x3"
    private String danhSachHang;  // VD: "Canon, HP" (Thay cho Nhà cung cấp)
}