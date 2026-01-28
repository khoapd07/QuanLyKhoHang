package com.poly.quanlykhohang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaoCaoResponseDTO {
    private String tenKho; // Hiển thị ở Header
    private List<BaoCaoXuatNhapTonDTO> danhSachChiTiet; // Hiển thị ở Table
}