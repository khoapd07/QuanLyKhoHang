package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TaiKhoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "TenTaiKhoan", nullable = false, unique = true)
    private String tenTaiKhoan;

    @Column(name = "Password", nullable = false)
    private String password;

    // Lưu ý: Tạm thời để Int để Mem 2 dễ test (truyền ID kho vào là được)
    // Sau này sẽ @ManyToOne sang entity Kho sau nếu cần
    @Column(name = "MaKho")
    private Integer maKho;

    @Column(name = "MaVaitro")
    private Integer maVaitro;
}