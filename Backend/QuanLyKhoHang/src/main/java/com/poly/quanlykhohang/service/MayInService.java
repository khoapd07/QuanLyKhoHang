package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dto.MayInResponseDTO;
import com.poly.quanlykhohang.entity.MayIn;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface MayInService {
    // Tìm kiếm cơ bản
    MayIn timTheoSeri(String soSeri);
    List<MayIn> timMayTonKhoTheoSanPham(String maSP);

    // Nghiệp vụ Truy xuất nguồn gốc (Traceability)
    // Trả về Map chứa thông tin: Ngày nhập, NCC, Ngày xuất, Khách hàng...
    Map<String, Object> traCuuLichSuMay(String soSeri);

    // Cập nhật trạng thái (VD: Chuyển kho, báo hỏng)
    void capNhatTrangThai(String soSeri, Integer trangThaiMoi);
    void chuyenKho(String soSeri, Integer maKhoMoi);

//    Page<MayInResponseDTO> layDanhSachMayIn(int page, int size, Integer maKho);

    Page<MayInResponseDTO> layDanhSachMayIn(int page, int size, Integer maKho, String maSP, Integer trangThai);
}