package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.entity.HangSanXuat;
import java.util.List;

public interface HangSanXuatService {
    List<HangSanXuat> getAllHang();
    HangSanXuat getHangById(Integer id);
    HangSanXuat createHang(HangSanXuat hang);
    HangSanXuat updateHang(Integer id, HangSanXuat hang);
    void deleteHang(Integer id);
}