package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.util.List;

@Data
public class PhieuNhapDTO {
    private String maDonVi; // Mã Nhà Cung Cấp
    private Integer maKho;  // Mã Kho (Integer)
    private String ghiChu;
    private List<ChiTietNhapDTO> chiTietPhieuNhap;
}