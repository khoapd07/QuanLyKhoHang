package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.ChiTietPhieuXuat;
import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChiTietPhieuXuatDAO extends JpaRepository<ChiTietPhieuXuat, Integer> {

    // SỬA: Thêm @Query tương tự
    @Query("SELECT c FROM ChiTietPhieuXuat c WHERE c.mayIn = :mayIn")
    Optional<ChiTietPhieuXuat> findByMayIn(@Param("mayIn") MayIn mayIn);
}