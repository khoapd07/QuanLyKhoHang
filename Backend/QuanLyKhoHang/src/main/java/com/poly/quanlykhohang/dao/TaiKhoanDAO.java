package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaiKhoanDAO extends JpaRepository<TaiKhoan, Integer> {
    // Tìm user để check đăng nhập
    Optional<TaiKhoan> findByTenTaiKhoan(String tenTaiKhoan);
}