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

    // 2. Tìm mã máy lớn nhất theo prefix
    @Query("SELECT m.maMay FROM MayIn m WHERE m.maMay LIKE :prefix% ORDER BY m.maMay DESC LIMIT 1")
    Optional<String> findLastId(@Param("prefix") String prefix);

    // 3. [QUAN TRỌNG] Lấy danh sách máy khả dụng (Còn trong kho)
    // Logic: Lấy máy có TonKho = true (Bất kể trạng thái vật lý là New hay LikeNew)
    @Query("SELECT m FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.tonKho = true")
    List<MayIn> findAvailableMachinesByProduct(@Param("maSP") String maSP);

    // 4. Đếm số lượng tồn kho thực tế (Dựa trên TonKho)
    @Query("SELECT COUNT(m) FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.tonKho = true")
    Long countStockByProduct(@Param("maSP") String maSP);

    // 5. Tìm tất cả máy đang nằm trong một Kho cụ thể
    List<MayIn> findByKho_MaKho(Integer maKho);

    // 6. Báo cáo: Đếm số lượng máy trong từng kho
    @Query("SELECT m.kho.tenKho, COUNT(m) FROM MayIn m WHERE m.tonKho = true GROUP BY m.kho.tenKho")
    List<Object[]> baoCaoTonKho();

    // 7. Lọc máy theo trạng thái
    List<MayIn> findByTrangThai(Integer trangThai);

    @Query("SELECT m.maMay FROM MayIn m WHERE m.sanPham.maSP = :maSP AND m.kho.maKho = :maKho AND m.tonKho = true")
    List<String> findMaMayTonKho(@Param("maSP") String maSP, @Param("maKho") Integer maKho);
}