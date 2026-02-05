package com.poly.quanlykhohang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Integer maTrangThai;

    @Column(name = "TenTrangThai")
    private String tenTrangThai;
}