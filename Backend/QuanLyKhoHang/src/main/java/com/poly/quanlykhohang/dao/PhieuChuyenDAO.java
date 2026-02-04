package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhieuChuyenDAO extends JpaRepository<PhieuChuyen, String> {
    // Tìm mã phiếu cuối cùng để sinh mã tự động (VD: CK202602...)
    @Query("SELECT p.soPhieu FROM PhieuChuyen p WHERE p.soPhieu LIKE :prefix% ORDER BY p.soPhieu DESC LIMIT 1")
    Optional<String> findLastId(String prefix);
}