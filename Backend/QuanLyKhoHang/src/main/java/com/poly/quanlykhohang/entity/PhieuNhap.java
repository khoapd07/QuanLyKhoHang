package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PhieuNhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhap {

    @Id
    @Column(name = "SoPhieu", length = 50)
    private String soPhieu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaDonVi")
    private DonVi nhaCungCap;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaKho")
    private Kho khoNhap;

    @Column(name = "NgayNhap")
    private LocalDateTime ngayNhap;

    // [Cần verify] Trong DB bạn có cột SoLuong ở bảng PhieuNhap
    @Column(name = "SoLuong")
    private Integer tongSoLuong;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "VAT")
    private BigDecimal thueVAT;

    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuNhap> danhSachChiTiet;
}