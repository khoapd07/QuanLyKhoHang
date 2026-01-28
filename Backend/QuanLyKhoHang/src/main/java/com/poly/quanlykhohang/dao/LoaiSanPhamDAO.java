package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.LoaiSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiSanPhamDAO extends JpaRepository<LoaiSanPham, Integer> {
}