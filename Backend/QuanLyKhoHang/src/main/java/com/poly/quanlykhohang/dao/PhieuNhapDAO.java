package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhieuNhapDAO extends JpaRepository<PhieuNhap, String> {
    // Hàm này sửa lỗi dòng 86 trong Service
    @Query("SELECT p.soPhieu FROM PhieuNhap p WHERE p.soPhieu LIKE :prefix% ORDER BY p.soPhieu DESC LIMIT 1")
    Optional<String> findLastId(@Param("prefix") String prefix);
}