package com.poly.quanlykhohang.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data; // Nếu dùng Lombok, không thì tự tạo Getter/Setter
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MayInResponseDTO {
    private String maMay;
    private String soSeri;
    private Integer trangThai;
    private Boolean tonKho;
    private LocalDateTime ngayTao;
    private String soPhieuNhap;

    // Các trường "làm phẳng" từ bảng cha
    private String maSP;
    private String tenSP;
    private String tenHang;  // Từ SanPham -> HangSanXuat
    private String tenLoai;  // Từ SanPham -> LoaiSanPham (Cái bạn đang cần)

    private Integer maKho;
    private String tenKho;
}