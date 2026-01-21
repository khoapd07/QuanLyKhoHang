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
    private String maMay; // <--- ĐÃ SỬA: Đổi từ maSeri thành maMay cho chuẩn

    // Liên kết với bảng Sản Phẩm
    @ManyToOne
    @JoinColumn(name = "MaSP", nullable = false)
    private SanPham sanPham;

    // Liên kết với bảng Kho
    @ManyToOne
    @JoinColumn(name = "MaKho")
    private Kho kho;

    @Column(name = "SoSeri", unique = true)
    private String soSeri; // Số Serial thực tế in trên máy

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "MaTrangThai")
    private Integer trangThai; // 1: Tồn kho, 2: Đã bán, 3: Hỏng
}