package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DMTonKho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DMTonKhoID.class) // [QUAN TRỌNG] Khai báo class khóa chính vừa tạo ở trên
public class DMTonKho {

    // --- 4 CỘT KHÓA CHÍNH (COMPOSITE KEY) ---
    // Phải đánh dấu @Id cho TẤT CẢ 4 cột này

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

    @Column(name = "SoLuongDau")
    private Long soLuongDau; // Dùng Long cho an toàn, SQL Count trả về số lớn

    @Column(name = "GiaTriDau")
    private BigDecimal giaTriDau;
}