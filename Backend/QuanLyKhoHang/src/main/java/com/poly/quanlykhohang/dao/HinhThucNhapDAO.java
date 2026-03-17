package com.poly.quanlykhohang.dao;
import com.poly.quanlykhohang.entity.HinhThucNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HinhThucNhapDAO extends JpaRepository<HinhThucNhap, Integer> {
    boolean existsByTenHT(String tenHT);

    HinhThucNhap findByTenHT(String tenHT);
}