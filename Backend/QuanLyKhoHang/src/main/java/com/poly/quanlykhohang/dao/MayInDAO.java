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

    // 1. Tìm máy theo số Seri
    Optional<MayIn> findBySoSeri(String soSeri);

    // 2. [QUAN TRỌNG] Tìm mã máy lớn nhất theo prefix để tự tăng (VD: R-202601%)
    // Hàm này sửa lỗi "Cannot resolve method findLastId"
    @Query("SELECT m.maMay FROM MayIn m WHERE m.maMay LIKE :prefix% ORDER BY m.maMay DESC LIMIT 1")
    Optional<String> findLastId(@Param("prefix") String prefix);

    // 3. Lấy danh sách máy khả dụng theo Mã Sản Phẩm
    @Query("SELECT m FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    List<MayIn> findAvailableMachinesByProduct(@Param("maSP") String maSP);

    // 4. Đếm số lượng tồn kho thực tế của một sản phẩm
    @Query("SELECT COUNT(m) FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.trangThai = 1")
    Long countStockByProduct(@Param("maSP") String maSP);

    // 5. Tìm tất cả máy đang nằm trong một Kho cụ thể
    List<MayIn> findByKho_MaKho(Integer maKho);

    // 6. Báo cáo: Đếm số lượng máy trong từng kho
    @Query("SELECT m.kho.tenKho, COUNT(m) FROM MayIn m GROUP BY m.kho.tenKho")
    List<Object[]> baoCaoTonKho();

    // 7. Lọc máy theo trạng thái
    List<MayIn> findByTrangThai(Integer trangThai);
}