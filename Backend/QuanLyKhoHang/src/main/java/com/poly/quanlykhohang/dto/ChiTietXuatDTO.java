package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ChiTietXuatDTO {
    private String maSP;
    private Integer soLuong;
    private BigDecimal donGia;
    private List<String> danhSachSeri; // Xuất những seri nào
}