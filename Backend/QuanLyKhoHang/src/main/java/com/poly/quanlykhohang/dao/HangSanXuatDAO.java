package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.HangSanXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HangSanXuatDAO extends JpaRepository<HangSanXuat, Integer> {
    // Có thể thêm hàm tìm kiếm theo tên nếu cần
    // List<HangSanXuat> findByTenHangContaining(String keyword);
}