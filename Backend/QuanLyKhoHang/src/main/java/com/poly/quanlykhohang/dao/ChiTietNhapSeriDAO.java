package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.ChiTietNhapSeri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChiTietNhapSeriDAO extends JpaRepository<ChiTietNhapSeri, Long> {

    // 🔴 CŨ (Sẽ lỗi): findByMayIn_MaSeri
    // 🟢 MỚI (Sửa thành): findByMayIn_MaMay
    // Giải thích: Tìm trong MayIn, lấy thuộc tính maMay
    Optional<ChiTietNhapSeri> findByMayIn_MaMay(String maMay);
}