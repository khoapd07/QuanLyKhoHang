package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CTPhieuXuat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPX")
    private Integer maCTPX;

    @ManyToOne
    @JoinColumn(name = "SoPhieu")
    private PhieuXuat phieuXuat;

    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    // [MỚI] Link trực tiếp tới Máy xuất đi
    @ManyToOne
    @JoinColumn(name = "MaMay")
    private MayIn mayIn;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    // XÓA: private Integer soLuong;
}