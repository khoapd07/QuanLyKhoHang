package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CTPhieuNhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPN")
    private Integer maCTPN;

    @ManyToOne
    @JoinColumn(name = "SoPhieu") // Link về phiếu cha
    private PhieuNhap phieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaSP")    // Nhập sản phẩm gì
    private SanPham sanPham;

    // [QUAN TRỌNG] Thay đổi theo DB mới: Link trực tiếp tới Máy
    @ManyToOne
    @JoinColumn(name = "MaMay")
    private MayIn mayIn;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "GhiChu")
    private String ghiChu;

    // XÓA: private Integer soLuong; (Không còn dùng)
}