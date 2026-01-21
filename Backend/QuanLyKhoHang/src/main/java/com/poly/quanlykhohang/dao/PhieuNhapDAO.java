package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuNhapDAO extends JpaRepository<PhieuNhap, String> {

    // Tìm phiếu nhập theo ngày (Báo cáo)
    List<PhieuNhap> findByNgayNhapBetween(LocalDateTime fromDate, LocalDateTime toDate);

    // Tìm phiếu nhập của 1 nhà cung cấp
    List<PhieuNhap> findByNhaCungCap_MaDonVi(String maDonVi);
}