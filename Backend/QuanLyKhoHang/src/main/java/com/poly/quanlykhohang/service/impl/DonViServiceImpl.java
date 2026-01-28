package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.DonViDAO;
import com.poly.quanlykhohang.entity.DonVi;
import com.poly.quanlykhohang.service.DonViService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DonViServiceImpl implements DonViService {

    @Autowired
    private DonViDAO donViDAO;

    @Override
    public List<DonVi> getAllDonVi() {
        return donViDAO.findAll();
    }

//    @Override
//    public DonVi getDonViById(String id) {
//        return donViDAO.findById(id).orElse(null); // Trả về null nếu không thấy
//    }

    @Override
    public DonVi saveDonVi(DonVi donVi) {
        return donViDAO.save(donVi);
    }

//    @Override
//    public void deleteDonVi(String id) {
//        donViDAO.deleteById(id);
//    }

    @Override
    public List<DonVi> getNhaCungCap() {
        return donViDAO.findByLoaiDonVi(1);
    }

    @Override
    public List<DonVi> getKhachHang() {
        return donViDAO.findByLoaiDonVi(2);
    }

//    @Override
//    public List<DonVi> getAllDonVi() {
//        return donViDAO.findAll();
//    }

    @Override
    public DonVi getDonViById(String id) {
        return donViDAO.findByMaDonVi(id);
    }

    @Override
    public DonVi createDonVi(DonVi donVi) {
        // Có thể thêm validate số điện thoại hoặc mã đơn vị tại đây
        return donViDAO.save(donVi);
    }

    @Override
    public DonVi updateDonVi(String id, DonVi donViDetails) {
        DonVi existingDonVi = donViDAO.findById(id).orElse(null);
        if (existingDonVi != null) {
            existingDonVi.setTenDonVi(donViDetails.getTenDonVi());
            existingDonVi.setDiaChi(donViDetails.getDiaChi());
            existingDonVi.setSoDienThoai(donViDetails.getSoDienThoai());
            existingDonVi.setLoaiDonVi(donViDetails.getLoaiDonVi()); // Nhà cung cấp hoặc Khách hàng
            return donViDAO.save(existingDonVi);
        }
        return null;
    }

    @Override
    public void deleteDonVi(String id) {
        donViDAO.deleteById(id);
    }
}