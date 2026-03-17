package com.poly.quanlykhohang.service;

import com.poly.quanlykhohang.dto.BaoCaoXuatNhapTonDTO;
import org.springframework.web.multipart.MultipartFile;
import com.poly.quanlykhohang.dto.SyncTonKhoDTO;
import java.util.List;


public interface ThongKeExcelService {



    void syncToDMTonKho(List<SyncTonKhoDTO> payload) throws Exception;
    void xoaTonKhoTheoNam(Integer nam, Integer maKho) throws Exception;
    // Đã thay đổi tham số cuối từ LocalDateTime sang Integer namLuuTon
    List<BaoCaoXuatNhapTonDTO> processAndImportExcel(MultipartFile file, Integer maKhoTuGiaoDien, Integer namLuuTon) throws Exception;
}