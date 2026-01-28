package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LoaiSP") // Tên bảng trong DB
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaiSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự tăng (Nếu DB bạn set Identity)
    @Column(name = "MaLoai")
    private Integer maLoai;

    @Column(name = "TenLoai")
    private String tenLoai;
}