package com.poly.quanlykhohang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaoCaoXuatNhapTonDTO {

    private Integer stt;          // Xử lý ở Frontend (vòng lặp) hoặc Service
    private String maSP;          // SQL trả về: MaSP (Sửa từ maSanPham)
    private String tenSP;         // SQL trả về: TenSP (Sửa từ tenSanPham)
    private String donvitinh;     // SQL trả về: Donvitinh (Sửa từ dvt)
    private Long tonDau;          // SQL trả về: TonDau (Sửa từ tonDauKy)
    private Long nhapTrong;       // SQL trả về: NhapTrong
    private Long xuatTrong;       // SQL trả về: XuatTrong
    private Long tonCuoi;         // SQL trả về: TonCuoi
    private BigDecimal giaBQ;     // SQL trả về: GiaBQ
    private BigDecimal thanhTien; // SQL trả về: ThanhTien
}