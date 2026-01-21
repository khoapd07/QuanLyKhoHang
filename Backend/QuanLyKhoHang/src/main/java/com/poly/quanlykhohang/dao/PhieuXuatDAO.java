package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuXuatDAO extends JpaRepository<PhieuXuat, String> {

    // Báo cáo doanh thu xuất kho theo khoảng thời gian
    List<PhieuXuat> findByNgayXuatBetween(LocalDateTime start, LocalDateTime end);

    // Lịch sử mua hàng của khách
    List<PhieuXuat> findByKhachHang_MaDonVi(String maKhachHang);
}