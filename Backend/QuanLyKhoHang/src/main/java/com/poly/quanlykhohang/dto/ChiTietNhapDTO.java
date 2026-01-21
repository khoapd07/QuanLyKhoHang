package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ChiTietNhapDTO {
    private String maSP;        // Mã sản phẩm (VD: SP001)
    private Integer soLuong;    // Số lượng
    private BigDecimal donGia;  // Giá nhập
    private String ghiChu;

    // Quan trọng: Danh sách số Seri đi kèm (nếu quản lý theo seri)
    private List<String> danhSachSeri;
}