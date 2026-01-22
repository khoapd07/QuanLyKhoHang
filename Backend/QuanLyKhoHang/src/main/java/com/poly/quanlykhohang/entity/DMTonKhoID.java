package com.poly.quanlykhohang.entity;

import java.io.Serializable;
import java.util.Objects;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DMTonKhoID implements Serializable {

    private Integer nam;
    private String maSP;
    private Integer maKho;
    private Integer maTrangThai;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DMTonKhoID that = (DMTonKhoID) o;
        return Objects.equals(nam, that.nam) &&
                Objects.equals(maSP, that.maSP) &&
                Objects.equals(maKho, that.maKho) &&
                Objects.equals(maTrangThai, that.maTrangThai);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nam, maSP, maKho, maTrangThai);
    }
}