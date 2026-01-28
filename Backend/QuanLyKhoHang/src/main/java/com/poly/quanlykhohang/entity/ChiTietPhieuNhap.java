package com.poly.quanlykhohang.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @JoinColumn(name = "SoPhieu")
    @JsonIgnore
    private PhieuNhap phieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    // [QUAN TRỌNG] Link trực tiếp tới Máy (1-1 với dòng chi tiết)
    @OneToOne
    @JoinColumn(name = "MaMay")
    private MayIn mayIn;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "GhiChu")
    private String ghiChu;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}