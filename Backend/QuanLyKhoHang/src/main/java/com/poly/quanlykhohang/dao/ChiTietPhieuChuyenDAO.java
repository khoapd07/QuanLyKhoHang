package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.ChiTietPhieuChuyen;
import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietPhieuChuyenDAO extends JpaRepository<ChiTietPhieuChuyen, Integer> {

    // SỬA LỖI: Dùng @Query để tránh Spring hiểu nhầm từ khóa "In" trong "mayIn"
    @Query("SELECT c FROM ChiTietPhieuChuyen c WHERE c.mayIn = :mayIn")
    List<ChiTietPhieuChuyen> findByMayIn(@Param("mayIn") MayIn mayIn);
}