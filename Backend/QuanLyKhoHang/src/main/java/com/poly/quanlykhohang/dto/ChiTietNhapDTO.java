package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ChiTietNhapDTO {
    private String maSP;        // Mã sản phẩm (VD: SP001)
    private Integer soLuong;    // Số lượng nhập
    private BigDecimal donGia;  // Giá nhập
    private String ghiChu;
    private List<String> danhSachSeri; // List seri (VD: ["SN001", "SN002"])
    private Integer trangThai;
}