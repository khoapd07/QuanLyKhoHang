package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DMHangSX") // Tên bảng trong DB
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HangSanXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHang")
    private Integer maHang;

    @Column(name = "TenHang")
    private String tenHang;
}