package com.poly.quanlykhohang.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "HinhThucXuat")
public class HinhThucXuat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHT")
    private Integer maHT;

    @Column(name = "TenHT")
    private String tenHT;
}