package com.poly.quanlykhohang.dao;
import com.poly.quanlykhohang.entity.HinhThucXuat;
import org.springframework.data.jpa.repository.JpaRepository;
public interface HinhThucXuatDAO extends JpaRepository<HinhThucXuat, Integer> {

    boolean existsByTenHT(String tenHT);

    HinhThucXuat findByTenHT(String tenHT);
}