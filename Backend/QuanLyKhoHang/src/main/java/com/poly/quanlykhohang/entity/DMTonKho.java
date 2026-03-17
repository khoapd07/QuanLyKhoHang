package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DMTonKho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DMTonKhoID.class)
public class DMTonKho {

    // --- 4 CỘT KHÓA CHÍNH (COMPOSITE KEY) ---

    @Id
    @Column(name = "Nam")
    private Integer nam;

    @Id
    @Column(name = "MaSP", length = 50)
    private String maSP;

    @Id
    @Column(name = "MaKho")
    private Integer maKho;

    @Id
    @Column(name = "MaTrangThai")
    private Integer maTrangThai;

    // --- CÁC CỘT DỮ LIỆU ĐÃ ĐƯỢC ĐỒNG BỘ VỚI SQL ---

    @Column(name = "SoLuong") // Đã đổi từ SoLuongDau -> SoLuong
    private Integer soLuong;  // Đổi thành Integer cho khớp chuẩn kiểu INT trong SQL

    @Column(name = "GiaTri")  // Đã đổi từ GiaTriDau -> GiaTri
    private BigDecimal giaTri;
}