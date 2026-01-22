package com.poly.quanlykhohang.dao;

import com.poly.quanlykhohang.entity.DMTonKho;
 import com.poly.quanlykhohang.entity.DMTonKhoID; // Nếu cần dùng ID thì mở dòng này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// --- THÊM 2 DÒNG NÀY ĐỂ SỬA LỖI ---
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// QUAN TRỌNG: Tham số thứ 2 của JpaRepository phải là class khóa chính (DMTonKhoID)
public interface DMTonKhoDAO extends JpaRepository<DMTonKho, DMTonKhoID> {

    // 1. Tìm kiếm nhanh theo Năm và Kho (Ví dụ: Lấy toàn bộ tồn đầu năm 2026 kho 1)
    List<DMTonKho> findByNamAndMaKho(Integer nam, Integer maKho);

    // 2. Tìm kiếm cụ thể một sản phẩm trong năm (JPA tự hiểu dựa trên tên biến)
    // Spring Data JPA thông minh sẽ tự mapping các trường trong ID
    List<DMTonKho> findByNamAndMaKhoAndMaSP(Integer nam, Integer maKho, String maSP);

    // 3. Gọi Stored Procedure Chốt Sổ (Cập nhật dữ liệu vào bảng này)
    // Lưu ý: Hàm này làm thay đổi dữ liệu nên cần @Modifying và @Transactional
    @Modifying
    @Transactional
    @Query(value = "EXEC sp_ChotSoTonDauNam :nam, :maKho", nativeQuery = true)
    void chotSoDauNam(@Param("nam") Integer nam, @Param("maKho") Integer maKho);
}