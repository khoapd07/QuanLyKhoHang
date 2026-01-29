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

    @ManyToOne
    @JoinColumn(name = "MaHang")
    private HangSanXuat hangSanXuat;

    // 2. Loại sản phẩm (Di chuyển từ DMMay sang đây)
    @ManyToOne
    @JoinColumn(name = "MaLoai")
    private LoaiSanPham loaiSanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

}