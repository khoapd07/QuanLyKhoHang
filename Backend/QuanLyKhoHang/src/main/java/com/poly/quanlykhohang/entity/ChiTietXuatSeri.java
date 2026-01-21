package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CTXMaMay") // Tên bảng trong SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietXuatSeri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTXuat") // Tên cột khóa chính trong SQL
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MaCTPX", nullable = false) // FK trỏ về ChiTietPhieuXuat
    private ChiTietPhieuXuat chiTietPhieuXuat;

    @ManyToOne
    @JoinColumn(name = "MaMay", nullable = false)
    private MayIn mayIn;

    @Column(name = "GhiChu")
    private String ghiChu;
}