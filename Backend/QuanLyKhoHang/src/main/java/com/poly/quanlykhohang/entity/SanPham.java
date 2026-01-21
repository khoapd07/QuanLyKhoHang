package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DMSanPham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {

    @Id
    @Column(name = "MaSP", length = 50)
    private String maSP;

    @Column(name = "TenSP")
    private String tenSP;

    @Column(name = "Donvitinh")
    private String donViTinh;

    @Column(name = "Mota")
    private String moTa;

    @Column(name = "MaHang")
    private Integer maHang;

    @Column(name = "SoLuong")
    private Integer soLuong;

    // Đã xóa: giaNiemYet, thuongHieu, tonToiThieu... vì trong SQL không có
}