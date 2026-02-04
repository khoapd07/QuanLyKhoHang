package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PhieuChuyen")
@Data
public class PhieuChuyen {
    @Id
    @Column(name = "SoPhieu")
    private String soPhieu;

    @Column(name = "NgayChuyen")
    private LocalDateTime ngayChuyen;

    @Column(name = "GhiChu")
    private String ghiChu;

    @ManyToOne
    @JoinColumn(name = "MaKhoDi")
    private Kho khoDi;

    @ManyToOne
    @JoinColumn(name = "MaKhoDen")
    private Kho khoDen;

    // Quan hệ 1-N với chi tiết
    @OneToMany(mappedBy = "phieuChuyen", cascade = CascadeType.ALL)
    private List<ChiTietPhieuChuyen> danhSachChiTiet;
}