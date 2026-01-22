package com.poly.quanlykhohang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaoCaoXuatNhapTonDTO {
    private Integer stt;          // 1. STT
    private String maSanPham;     // 2. Mã sản phẩm
    private String tenSanPham;    // 3. Tên sản phẩm
    private String dvt;           // 4. ĐVT (Cái/Chiếc...)
    private Long tonDauKy;        // 5. TĐK (Tồn đầu)
    private Long nhapTrongKy;     // 6. NTK (Nhập)
    private Long xuatTrongKy;     // 7. XTK (Xuất)
    private Long tonCuoiKy;       // 8. TCK (Tồn cuối = Đầu + Nhập - Xuất)
    private Double giaBinhQuan;   // 9. Giá/BQ (Mặc định 0)
    private Double thanhTien;     // 10. Thành tiền (Mặc định 0)
}