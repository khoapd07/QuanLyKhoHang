package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonViDAO extends JpaRepository<DonVi, String> {

    // Lấy danh sách Nhà cung cấp (1) hoặc Khách hàng (2)
    List<DonVi> findByLoaiDonVi(Integer loaiDonVi);

    // Tìm theo SĐT
    Optional<DonVi> findBySoDienThoai(String soDienThoai);


}