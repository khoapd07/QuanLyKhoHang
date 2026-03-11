package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.DMTonKho;
import com.poly.quanlykhohang.entity.DMTonKhoID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ThongKeDAO extends JpaRepository<DMTonKho, DMTonKhoID> {

    @Query(value = "SELECT TenKho FROM Kho WHERE MaKho = :maKho", nativeQuery = true)
    String getTenKhoById(@Param("maKho") Integer maKho);

    @Modifying
    @Transactional
    @Query(value = "EXEC sp_ChotSoTonDauNam :nam, :maKho", nativeQuery = true)
    void chotSoDauNam(@Param("nam") Integer nam, @Param("maKho") Integer maKho);

    @Query(value = "SELECT COUNT(*) FROM DMTonKho WHERE Nam = :namSau AND (:maKho = 0 OR MaKho = :maKho)", nativeQuery = true)
    int demSoLuongBanGhiChotSo(@Param("namSau") int namSau, @Param("maKho") int maKho);

    // =================================================================
    // API LỊCH SỬ CHỐT SỔ (CHỈ FETCH DỮ LIỆU TĨNH TỪ DMTonKho)
    // =================================================================

    // 1. Lấy chi tiết phân trang (4 tham số: nam, maKho, page, size)
    @Query(value = "EXEC sp_LayLichSuChotSo_PhanTrang :nam, :maKho, :page, :size", nativeQuery = true)
    List<Object[]> getLichSuChotSoPhanTrang(
            @Param("nam") Integer nam,
            @Param("maKho") Integer maKho,
            @Param("page") Integer page,
            @Param("size") Integer size
    );

    // 2. Hàm đếm tổng số dòng (2 tham số: nam, maKho)
    // Dùng raw SQL COUNT(*) cho nhanh vì không cần logic phức tạp
    @Query(value = "SELECT COUNT(*) FROM DMTonKho WHERE Nam = :nam AND (:maKho = 0 OR MaKho = :maKho)", nativeQuery = true)
    long countLichSuChotSo(
            @Param("nam") Integer nam,
            @Param("maKho") Integer maKho
    );

    // 3. Hàm tính tổng cộng (Đã sửa lại để gọi SP thay vì Raw SQL)
    @Query(value = "EXEC sp_TongHopLichSuChotSo :nam, :maKho", nativeQuery = true)
    List<Object[]> getTongHopLichSu(
            @Param("nam") Integer nam,
            @Param("maKho") Integer maKho
    );

    // =================================================================
    // API BÁO CÁO XUẤT NHẬP TỒN (GIỮ NGUYÊN)
    // =================================================================
    @Query(value = "EXEC sp_BaoCaoXuatNhapTon_PhanTrang :maKho, :tuNgay, :denNgay, :loaiLoc, :page, :size", nativeQuery = true)
    List<Object[]> baoCaoPhanTrang(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc,
            @Param("page") Integer page,
            @Param("size") Integer size
    );

    @Query(value = "EXEC sp_Count_BaoCaoXuatNhapTon :maKho, :tuNgay, :denNgay, :loaiLoc", nativeQuery = true)
    List<Object[]> getTongHopBaoCao(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc
    );

}