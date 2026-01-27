package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PhieuXuat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuXuat {

    @Id
    @Column(name = "SoPhieu", length = 50)
    private String soPhieu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaDonVi")
    private DonVi khachHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKho")
    private Kho khoXuat;

    @Column(name = "NgayXuat")
    private LocalDateTime ngayXuat;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "VAT")
    private BigDecimal thueVAT;

    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuXuat> danhSachChiTiet;
}