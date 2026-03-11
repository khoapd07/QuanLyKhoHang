package com.poly.quanlykhohang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TrangThai")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrangThai {
    @Id
    @Column(name = "MaTrangThai")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maTrangThai;

    @Column(name = "TenTrangThai")
    private String tenTrangThai;
}