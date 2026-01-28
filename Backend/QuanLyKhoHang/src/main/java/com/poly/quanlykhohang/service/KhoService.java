package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.entity.Kho;
import java.util.List;

public interface KhoService {
    List<Kho> layDanhSachKho();
    Kho luuKho(Kho kho);
    void xoaKho(Integer maKho);

    Kho getKhoById(Integer id);
    Kho createKho(Kho kho);
    Kho updateKho(Integer id, Kho kho);
    void deleteKho(Integer id);
}