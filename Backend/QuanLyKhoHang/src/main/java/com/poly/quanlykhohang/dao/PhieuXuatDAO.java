package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhieuXuatDAO extends JpaRepository<PhieuXuat, String> {

    // Tìm mã phiếu xuất lớn nhất theo prefix (VD: PX202601%)
    @Query("SELECT p.soPhieu FROM PhieuXuat p WHERE p.soPhieu LIKE :prefix% ORDER BY p.soPhieu DESC LIMIT 1")
    Optional<String> findLastId(@Param("prefix") String prefix);
}