package com.poly.quanlykhohang.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PhieuXuatDTO {
    private String maDonVi; // Mã Khách Hàng (Bắt buộc)
    private Integer maKho;  // Kho xuất
    private String ghiChu;
    private LocalDateTime ngayTaoPhieu;
    private List<ChiTietXuatDTO> chiTietPhieuXuat;
}