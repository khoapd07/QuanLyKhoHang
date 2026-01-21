package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.ChiTietXuatSeri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChiTietXuatSeriDAO extends JpaRepository<ChiTietXuatSeri, Long> {

    // Tìm thông tin xuất kho dựa trên Mã Máy (ID)
    // 🔴 CŨ: WHERE ctx.mayIn.maSeri = :maMay
    // 🟢 MỚI: WHERE ctx.mayIn.maMay = :maMay
    @Query("SELECT ctx FROM ChiTietXuatSeri ctx " +
            "JOIN FETCH ctx.chiTietPhieuXuat ct " +
            "JOIN FETCH ct.phieuXuat p " +
            "WHERE ctx.mayIn.maMay = :maMay")
    Optional<ChiTietXuatSeri> findExportInfoByMachineId(String maMay);
}