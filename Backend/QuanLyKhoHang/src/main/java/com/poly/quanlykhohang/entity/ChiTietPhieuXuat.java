package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "CTPhieuXuat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPX") // <--- Quan trọng: Phải khớp SQL
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SoPhieu", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PhieuXuat phieuXuat;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "chiTietPhieuXuat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietXuatSeri> danhSachSeri;
}