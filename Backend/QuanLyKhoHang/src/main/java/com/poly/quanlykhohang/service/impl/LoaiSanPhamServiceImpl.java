package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.LoaiSanPhamDAO;
import com.poly.quanlykhohang.entity.LoaiSanPham;
import com.poly.quanlykhohang.service.LoaiSanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoaiSanPhamServiceImpl implements LoaiSanPhamService {

    @Autowired
    private LoaiSanPhamDAO dao;

    @Override
    public List<LoaiSanPham> getAll() {
        return dao.findAll();
    }

    @Override
    public LoaiSanPham getById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public LoaiSanPham create(LoaiSanPham loaiSP) {
        return dao.save(loaiSP);
    }

    @Override
    public LoaiSanPham update(Integer id, LoaiSanPham loaiDetails) {
        LoaiSanPham existing = dao.findById(id).orElse(null);
        if (existing != null) {
            existing.setTenLoai(loaiDetails.getTenLoai());
            return dao.save(existing);
        }
        return null;
    }

    @Override
    public void delete(Integer id) {
        dao.deleteById(id);
    }
}