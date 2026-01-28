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

    // Trong DB là VARCHAR(50) lưu mã phiếu, không phải khóa ngoại cứng trong JPA
    @Column(name = "SoPhieuNhap", length = 50)
    private String soPhieuNhap;

    @Column(name = "SoSeri", unique = true)
    private String soSeri;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    // Map với bảng TrangThai (1: Tồn, 2: Bán...)
    @Column(name = "MaTrangThai")
    private Integer trangThai;

    // Bổ sung thêm MaHang nếu cần thiết theo DB
    @Column(name = "MaHang")
    private Integer maHang;
}