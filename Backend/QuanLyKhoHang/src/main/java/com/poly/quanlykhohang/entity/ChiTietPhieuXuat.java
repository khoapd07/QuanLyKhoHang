package com.poly.quanlykhohang.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore // <--- THÊM CÁI NÀY
    private PhieuXuat phieuXuat;

    // ... các trường khác giữ nguyên ...
    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "MaMay")
    private MayIn mayIn;

    @Column(name = "DonGia")
    private BigDecimal donGia;
}