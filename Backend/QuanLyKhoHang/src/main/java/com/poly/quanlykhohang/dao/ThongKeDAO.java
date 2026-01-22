package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.DMTonKho;
import com.poly.quanlykhohang.entity.DMTonKhoID; // Import class khóa chính
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
// SỬA 1: Đổi từ SanPham sang DMTonKho và thêm ID
public interface ThongKeDAO extends JpaRepository<DMTonKho, DMTonKhoID> {

    /**
     * Gọi Stored Procedure: sp_BaoCaoXuatNhapTon_TheoTrangThai
     */
    // SỬA 2: Viết gọn câu lệnh SQL Native
    @Query(value = "EXEC sp_BaoCaoXuatNhapTon_TheoTrangThai :maKho, :tuNgay, :denNgay, :loaiLoc", nativeQuery = true)
    List<Object[]> baoCaoTheoTrangThai(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc
    );

    /**
     * Gọi Stored Procedure: sp_ChotSoTonDauNam
     * Hàm này làm thay đổi dữ liệu (INSERT/DELETE) nên cần @Modifying và @Transactional
     */
    @Modifying
    @Transactional
    @Query(value = "EXEC sp_ChotSoTonDauNam :nam, :maKho", nativeQuery = true)
    void chotSoDauNam(@Param("nam") Integer nam, @Param("maKho") Integer maKho);
}