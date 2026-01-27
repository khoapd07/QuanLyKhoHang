package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DMDonVi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonVi {

    @Id
    @Column(name = "MaDonVi", length = 50)
    private String maDonVi;

    @Column(name = "TenDonVi", nullable = false)
    private String tenDonVi;

    @Column(name = "LoaiDonVi")
    private Integer loaiDonVi; // 1: Nhà cung cấp, 2: Khách hàng

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "Email")
    private String email;

    @Column(name = "MaSoThue")
    private String maSoThue;
}