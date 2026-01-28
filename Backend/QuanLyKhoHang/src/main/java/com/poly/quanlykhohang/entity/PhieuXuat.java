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

    // Sửa lỗi dòng 221 trong Service (Thiếu biến này)
    @ManyToOne
    @JoinColumn(name = "MaDonVi")
    private DonVi khachHang;

    @ManyToOne
    @JoinColumn(name = "MaKho")
    private Kho khoXuat;

    @Column(name = "NgayXuat")
    private LocalDateTime ngayXuat;

    @Column(name = "SoLuong")
    private Integer tongSoLuong;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "VAT")
    private BigDecimal thueVAT;

    @Column(name = "GhiChu")
    private String ghiChu;

    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuXuat> danhSachChiTiet;
}