package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PhieuXuatResponseDTO {
    private String soPhieu;
    private LocalDateTime ngayXuat;
    private String tenKho;
    private String tenKhachHang; // Thay cho nhaCungCap bên nhập
    private String ghiChu;

    private Integer tongSoLuong;
    private BigDecimal tongTien;

    private String tomTatSanPham; // VD: "iPhone 15 x2"
    private String danhSachHang;  // VD: "Apple"
}