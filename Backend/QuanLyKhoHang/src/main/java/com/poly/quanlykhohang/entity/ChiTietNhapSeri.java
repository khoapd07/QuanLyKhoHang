package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CTNMaMay") // Tên bảng trong SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietNhapSeri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTNhap") // Tên cột khóa chính trong SQL
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MaCTPN", nullable = false) // FK trỏ về ChiTietPhieuNhap
    private ChiTietPhieuNhap chiTietPhieuNhap;

    @ManyToOne
    @JoinColumn(name = "MaMay", nullable = false)
    private MayIn mayIn;

    @Column(name = "GhiChu")
    private String ghiChu;
}