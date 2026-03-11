package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamDAO extends JpaRepository<SanPham, String> {
    List<SanPham> findByTenSPContainingIgnoreCase(String tenSP);

    @Query("SELECT s FROM SanPham s WHERE " +
            "(:maHang IS NULL OR s.hangSanXuat.maHang = :maHang) AND " +
            "(:maLoai IS NULL OR s.loaiSanPham.maLoai = :maLoai)")
    Page<SanPham> filterSanPham(@Param("maHang") Integer maHang,
                                @Param("maLoai") Integer maLoai,
                                Pageable pageable);

}