package com.poly.quanlykhohang.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhieuNhapDTO {
    private String maDonVi;
    private Integer maKho;     // Chỉ cần Kho
    private String ghiChu;
    private LocalDateTime ngayTaoPhieu;
    private List<ChiTietNhapDTO> chiTietPhieuNhap;
    private Integer maHT;
    private String soPhieuXuatNoiBo;
    private String soPhieu;
}