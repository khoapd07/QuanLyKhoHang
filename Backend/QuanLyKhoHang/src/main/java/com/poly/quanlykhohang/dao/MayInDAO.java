package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MayInDAO extends JpaRepository<MayIn, String> {

    // 1. Tìm máy theo số Seri (Dùng để check trùng khi nhập hoặc tìm để xuất)
    Optional<MayIn> findBySoSeri(String soSeri);

    // 2. Lấy danh sách máy khả dụng theo Mã Sản Phẩm
    @Query("SELECT m FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    List<MayIn> findAvailableMachinesByProduct(@Param("maSP") String maSP);

    // 3. Đếm số lượng tồn kho thực tế của một sản phẩm
    @Query("SELECT COUNT(m) FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    Long countStockByProduct(@Param("maSP") String maSP);

    // 4. Tìm tất cả máy đang nằm trong một Kho cụ thể
    // LƯU Ý: Sửa String -> Integer để khớp với ID của Entity Kho
    List<MayIn> findByKho_MaKho(Integer maKho);

    // 5. Báo cáo: Đếm số lượng máy trong từng kho
    @Query("SELECT m.kho.tenKho, COUNT(m) FROM MayIn m GROUP BY m.kho.tenKho")
    List<Object[]> baoCaoTonKho();

    // 6. Lọc máy theo trạng thái (1: Tồn kho, 2: Đã bán, 3: Hỏng)
    // Cần thiết để hiển thị danh sách quản lý
    List<MayIn> findByTrangThai(Integer trangThai);
}