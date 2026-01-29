package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ChiTietXuatDTO {
    private String maSP;
    private BigDecimal donGia; // Giá bán

    // Danh sách mã máy cần xuất (người dùng chọn từ tồn kho)
    private List<String> danhSachSeri;
}