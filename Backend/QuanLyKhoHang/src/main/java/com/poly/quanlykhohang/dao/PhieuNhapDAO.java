package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuNhapDAO extends JpaRepository<PhieuNhap, String> {


    // Tìm phiếu nhập theo ngày (Báo cáo)
    List<PhieuNhap> findByNgayNhapBetween(LocalDateTime fromDate, LocalDateTime toDate);

    @Query("SELECT p FROM PhieuNhap p WHERE p.khoNhap.maKho = :maKho ORDER BY p.ngayNhap DESC")
    List<PhieuNhap> findByMaKhoOrderByNgayNhapDesc(@Param("maKho") Integer maKho);

    // Hàm này sửa lỗi dòng 86 trong Service
    @Query("SELECT p.soPhieu FROM PhieuNhap p WHERE p.soPhieu LIKE :prefix% ORDER BY p.soPhieu DESC LIMIT 1")
    Optional<String> findLastId(@Param("prefix") String prefix);
}