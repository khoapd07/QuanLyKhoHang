package com.poly.quanlykhohang.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChuyenKhoDTO {
    private Integer maKhoDi;
    private Integer maKhoDen;
    private String ghiChu;

    // Danh sách các mã máy (Serial) được chọn để chuyển
    private List<String> danhSachSerial;
}