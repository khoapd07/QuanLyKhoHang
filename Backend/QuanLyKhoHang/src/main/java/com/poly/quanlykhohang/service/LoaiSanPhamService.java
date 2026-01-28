package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.entity.LoaiSanPham;
import java.util.List;

public interface LoaiSanPhamService {
    List<LoaiSanPham> getAll();
    LoaiSanPham getById(Integer id);
    LoaiSanPham create(LoaiSanPham loaiSP);
    LoaiSanPham update(Integer id, LoaiSanPham loaiSP);
    void delete(Integer id);
}