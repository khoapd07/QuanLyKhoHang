package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DMMay")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MayIn {

    @Id
    @Column(name = "MaMay", length = 50)
    private String maMay;

    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "MaKho")
    private Kho kho;


    @ManyToOne
    @JoinColumn(name = "MaHang")
    private HangSanXuat hangSanXuat; // Đây là thằng "Chủ" của cột MaHang

    // SỬA DÒNG NÀY: Thêm insertable = false, updatable = false
    @Column(name = "MaHang", insertable = false, updatable = false)
    private Integer maHang;
    // ---------------------------

    @Column(name = "SoPhieuNhap", length = 50)
    private String soPhieuNhap;

    @Column(name = "SoSeri")
    private String soSeri;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "MaTrangThai")
    private Integer trangThai;
}