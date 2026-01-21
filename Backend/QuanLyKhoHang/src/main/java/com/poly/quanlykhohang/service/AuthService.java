package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dao.TaiKhoanDAO;
import com.poly.quanlykhohang.entity.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private TaiKhoanDAO taiKhoanDAO;

    public TaiKhoan login(String tenTaiKhoan, String password) {
        // 1. Tìm user trong DB
        TaiKhoan user = taiKhoanDAO.findByTenTaiKhoan(tenTaiKhoan)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        // 2. Check pass (Nếu chưa mã hóa thì so sánh chuỗi thường)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Sai mật khẩu!");
        }

        // 3. Trả về user (Frontend sẽ lấy maTaiKhoan & maKho từ đây)
        return user;
    }
}