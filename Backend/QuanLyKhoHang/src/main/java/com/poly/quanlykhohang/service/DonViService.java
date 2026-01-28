package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.entity.DonVi;
import java.util.List;

public interface DonViService {
    List<DonVi> getAllDonVi();
//    DonVi getDonViById(String id);
    DonVi saveDonVi(DonVi donVi);
//    void deleteDonVi(String id);

    // Các hàm bổ trợ lọc
    List<DonVi> getNhaCungCap();
    List<DonVi> getKhachHang();

    DonVi getDonViById(String id);
    DonVi createDonVi(DonVi donVi);
    DonVi updateDonVi(String id, DonVi donVi);
    void deleteDonVi(String id);
}