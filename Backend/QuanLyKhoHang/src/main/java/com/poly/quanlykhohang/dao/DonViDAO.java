package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.DonVi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonViDAO extends JpaRepository<DonVi, String> {

    // Lấy danh sách Nhà cung cấp (1) hoặc Khách hàng (2)
    List<DonVi> findByLoaiDonVi(Integer loaiDonVi);

    // Tìm theo SĐT
    Optional<DonVi> findBySoDienThoai(String soDienThoai);


    DonVi findByMaDonVi(String maDonVi);

    @Query("SELECT d FROM DonVi d WHERE " +
            "(:loai IS NULL OR :loai = 0 OR d.loaiDonVi = :loai) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(d.tenDonVi) LIKE LOWER(CONCAT('%', :search, '%')) OR d.soDienThoai LIKE CONCAT('%', :search, '%'))")
    Page<DonVi> searchAndFilter(@Param("loai") Integer loai, @Param("search") String search, Pageable pageable);
}