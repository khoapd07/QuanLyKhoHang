package com.poly.quanlykhohang.dto;
import java.math.BigDecimal;


public class SyncTonKhoDTO {
    private Integer maKho;
    private Integer nam;
    private String maSP;
    private Integer soLuong;
    private Integer maTrangThai; // Chốt chặn trạng thái ở đây
    private BigDecimal giaTri;

    // Getters and Setters
    public Integer getMaKho() { return maKho; }
    public void setMaKho(Integer maKho) { this.maKho = maKho; }

    public Integer getNam() { return nam; }
    public void setNam(Integer nam) { this.nam = nam; }

    public String getMaSP() { return maSP; }
    public void setMaSP(String maSP) { this.maSP = maSP; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public Integer getMaTrangThai() { return maTrangThai; }
    public void setMaTrangThai(Integer maTrangThai) { this.maTrangThai = maTrangThai; }

    public BigDecimal getGiaTri() { return giaTri; }
    public void setGiaTri(BigDecimal giaTri) { this.giaTri = giaTri; }
}