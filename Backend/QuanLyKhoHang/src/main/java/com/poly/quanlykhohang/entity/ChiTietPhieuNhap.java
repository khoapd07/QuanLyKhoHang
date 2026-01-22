package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "CTPhieuNhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPN")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SoPhieu", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PhieuNhap phieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "chiTietPhieuNhap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietNhapSeri> danhSachSeri;
}