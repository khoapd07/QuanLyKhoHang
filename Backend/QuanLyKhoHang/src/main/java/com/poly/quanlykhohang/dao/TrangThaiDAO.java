package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.TrangThai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangThaiDAO extends JpaRepository<TrangThai, Integer> {
}