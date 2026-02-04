package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PhieuChuyenResponseDTO {
    private String soPhieu;
    private LocalDateTime ngayChuyen;
    private String tenKhoDi;
    private String tenKhoDen;
    private String ghiChu;

    // Hiển thị tóm tắt: "IP15 x2, Canon 2900 x5"
    private String tomTatSanPham;
    private Integer tongSoLuong;
}