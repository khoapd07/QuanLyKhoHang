package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.entity.SanPham;
import java.util.List;

public interface SanPhamService {
    List<SanPham> layTatCaSanPham();
    SanPham timTheoMa(String maSP);
    SanPham luuSanPham(SanPham sanPham);
    void xoaSanPham(String maSP);

}