package com.poly.quanlykhohang.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ChiTietPhieuChuyen")
@Data
public class ChiTietPhieuChuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTPC")
    private Integer maCTPC;

    @ManyToOne
    @JoinColumn(name = "SoPhieu")
    @JsonIgnore
    private PhieuChuyen phieuChuyen;

    @ManyToOne
    @JoinColumn(name = "MaSP")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "MaMay")
    private MayIn mayIn;
}