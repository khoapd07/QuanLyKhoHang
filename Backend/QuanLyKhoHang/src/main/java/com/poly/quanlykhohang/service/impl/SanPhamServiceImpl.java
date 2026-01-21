package com.poly.quanlykhohang.service.impl;

import com.poly.quanlykhohang.dao.SanPhamDAO;
import com.poly.quanlykhohang.entity.SanPham;
import com.poly.quanlykhohang.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor // Tự động Inject DAO (thay cho @Autowired)
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamDAO sanPhamDAO;

    @Override
    public List<SanPham> layTatCaSanPham() {
        return sanPhamDAO.findAll();
    }

    @Override
    public SanPham timTheoMa(String maSP) {
        return sanPhamDAO.findById(maSP)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maSP));
    }

    @Override
    public SanPham luuSanPham(SanPham sanPham) {
        return sanPhamDAO.save(sanPham);
    }

    @Override
    public void xoaSanPham(String maSP) {
        sanPhamDAO.deleteById(maSP);
    }

}