package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MayInDAO extends JpaRepository<MayIn, String> { // String là đúng vì MaMay (maSeri) là String

    // 1. Tìm máy theo số Seri (Cái này đúng vì trong ảnh có cột soSeri)
    Optional<MayIn> findBySoSeri(String soSeri);

    // 2. Lấy danh sách máy khả dụng (Sửa maSP -> maSanPham nếu cần)
    @Query("SELECT m FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    List<MayIn> findAvailableMachinesByProduct(@Param("maSP") String maSP);

    // 3. Đếm tồn kho
    @Query("SELECT COUNT(m) FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    Long countStockByProduct(@Param("maSP") String maSP);

    // 4. Tìm máy theo kho (Sửa Integer -> String để khớp với KHO01)
    List<MayIn> findByKho_MaKho(String maKho);

    // 5. Báo cáo tồn kho (Chuẩn rồi)
    @Query("SELECT m.kho.tenKho, COUNT(m) FROM MayIn m GROUP BY m.kho.tenKho")
    List<Object[]> baoCaoTonKho();
}