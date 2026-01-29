package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.util.List;

@Data
public class PhieuNhapDTO {
    private String maDonVi;
    private Integer maKho;     // Chỉ cần Kho
    private String ghiChu;
    private List<ChiTietNhapDTO> chiTietPhieuNhap;
}