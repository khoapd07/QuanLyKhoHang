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

    // 1. Lấy tên kho
    @Query(value = "SELECT TenKho FROM Kho WHERE MaKho = :maKho", nativeQuery = true)
    String getTenKhoById(@Param("maKho") Integer maKho);

    /**
     * [LEGACY] Báo cáo cũ (Không phân trang)
     */
    @Query(value = "EXEC sp_BaoCaoXuatNhapTon_TheoTrangThai :maKho, :tuNgay, :denNgay, :loaiLoc", nativeQuery = true)
    List<Object[]> baoCaoTheoTrangThai(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc
    );

    /**
     * [ACTION] Chốt sổ đầu năm
     * Đã khớp với SP mới (tính cả hàng chuyển kho)
     */
    @Modifying
    @Transactional
    @Query(value = "EXEC sp_ChotSoTonDauNam :nam, :maKho", nativeQuery = true)
    void chotSoDauNam(@Param("nam") Integer nam, @Param("maKho") Integer maKho);

    /**
     * [CHECK] Kiểm tra xem năm đã chốt chưa
     */
    @Query(value = "SELECT COUNT(*) FROM DMTonKho WHERE Nam = :namSau AND (:maKho = 0 OR MaKho = :maKho)", nativeQuery = true)
    int demSoLuongBanGhiChotSo(@Param("namSau") int namSau, @Param("maKho") int maKho);

    // =================================================================================
    // PHẦN 1: CÁC HÀM XEM LỊCH SỬ CHỐT SỔ (TAB 2)
    // =================================================================================

    /**
     * 1.1 Lấy danh sách lịch sử (Phân trang)
     * SP đã sửa LEFT JOIN để hiển thị cả "Sản phẩm Ma"
     */
    @Query(value = "EXEC sp_LayLichSuChotSo_PhanTrang :nam, :maKho, :page, :size", nativeQuery = true)
    List<Object[]> getLichSuChotSoPhanTrang(
            @Param("nam") Integer nam,
            @Param("maKho") Integer maKho,
            @Param("page") Integer page,
            @Param("size") Integer size
    );

    /**
     * 1.2 Đếm tổng số dòng lịch sử
     */
    @Query(value = "SELECT COUNT(*) FROM DMTonKho WHERE Nam = :nam AND (:maKho = 0 OR MaKho = :maKho)", nativeQuery = true)
    long countLichSuChotSo(@Param("nam") Integer nam, @Param("maKho") Integer maKho);

    /**
     * [SỬA LẠI RETURN TYPE] 1.3 Lấy tổng cộng cho phần Lịch Sử
     * Trả về List<Object[]> để tránh lỗi ClassCastException
     */
    @Query(value = "EXEC sp_Sum_LichSuChotSo :nam, :maKho", nativeQuery = true)
    List<Object[]> getTongHopLichSu(@Param("nam") Integer nam, @Param("maKho") Integer maKho);

    // =================================================================================
    // PHẦN 2: CÁC HÀM BÁO CÁO XUẤT NHẬP TỒN (LIVE DATA - TAB 1)
    // =================================================================================

    /**
     * 2.1 Lấy báo cáo Xuất Nhập Tồn (Phân trang)
     */
    @Query(value = "EXEC sp_BaoCaoXuatNhapTon_PhanTrang :maKho, :tuNgay, :denNgay, :loaiLoc, :page, :size", nativeQuery = true)
    List<Object[]> baoCaoPhanTrang(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc,
            @Param("page") Integer page,
            @Param("size") Integer size
    );

    /**
     * [SỬA LẠI RETURN TYPE] 2.2 Lấy tổng hợp báo cáo (Grand Total)
     * Trả về List<Object[]> thay vì Object[]
     */
    @Query(value = "EXEC sp_Count_BaoCaoXuatNhapTon :maKho, :tuNgay, :denNgay, :loaiLoc", nativeQuery = true)
    List<Object[]> getTongHopBaoCao(
            @Param("maKho") Integer maKho,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay,
            @Param("loaiLoc") Integer loaiLoc
    );
}