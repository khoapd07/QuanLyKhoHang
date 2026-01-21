package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamDAO extends JpaRepository<SanPham, String> {
    List<SanPham> findByTenSPContainingIgnoreCase(String tenSP);

}