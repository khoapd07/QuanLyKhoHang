package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.HangSanXuatDAO;
import com.poly.quanlykhohang.entity.HangSanXuat;
import com.poly.quanlykhohang.service.HangSanXuatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HangSanXuatServiceImpl implements HangSanXuatService {

    @Autowired
    private HangSanXuatDAO dao;

    @Override
    public List<HangSanXuat> getAllHang() {
        return dao.findAll();
    }

    @Override
    public HangSanXuat getHangById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public HangSanXuat createHang(HangSanXuat hang) {
        return dao.save(hang);
    }

    @Override
    public HangSanXuat updateHang(Integer id, HangSanXuat hangDetails) {
        HangSanXuat existingHang = dao.findById(id).orElse(null);
        if (existingHang != null) {
            existingHang.setTenHang(hangDetails.getTenHang());
            return dao.save(existingHang);
        }
        return null;
    }

    @Override
    public void deleteHang(Integer id) {
        dao.deleteById(id);
    }
}