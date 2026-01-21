package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.util.List;

@Data
public class PhieuNhapDTO {
    private String maDonVi; // Mã nhà cung cấp
    private Integer maKho;  // Nhập vào kho nào
    private String ghiChu;

    // Danh sách các sản phẩm cần nhập
    private List<ChiTietNhapDTO> chiTietPhieuNhap;
}