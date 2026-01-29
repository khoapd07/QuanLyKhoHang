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
    

    @Column(name = "SoPhieuNhap", length = 50)
    private String soPhieuNhap;

    @Column(name = "SoSeri")
    private String soSeri;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    // Trạng thái vật lý: 1=New, 2=LikeNew...
    @Column(name = "MaTrangThai")
    private Integer trangThai;

    // [QUAN TRỌNG] true = Tồn kho, false = Đã xuất
    @Column(name = "TonKho")
    private Boolean tonKho;
}