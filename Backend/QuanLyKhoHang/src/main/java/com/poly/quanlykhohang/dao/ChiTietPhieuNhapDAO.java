package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.ChiTietPhieuNhap;
import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChiTietPhieuNhapDAO extends JpaRepository<ChiTietPhieuNhap, Integer> {

    // SỬA: Thêm @Query để tránh lỗi hiểu nhầm từ khóa "In"
    @Query("SELECT c FROM ChiTietPhieuNhap c WHERE c.mayIn = :mayIn")
    Optional<ChiTietPhieuNhap> findByMayIn(@Param("mayIn") MayIn mayIn);
}