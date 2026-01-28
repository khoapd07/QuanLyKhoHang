package com.poly.quanlykhohang.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @JsonIgnore
    private PhieuXuat phieuXuat;

    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    // [QUAN TRỌNG] Link trực tiếp tới Máy
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