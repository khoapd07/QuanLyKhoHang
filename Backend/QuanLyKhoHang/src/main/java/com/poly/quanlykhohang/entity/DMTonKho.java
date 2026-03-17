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

    // --- CÁC CỘT DỮ LIỆU ---
    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "GiaTri")
    private BigDecimal giaTri;
}