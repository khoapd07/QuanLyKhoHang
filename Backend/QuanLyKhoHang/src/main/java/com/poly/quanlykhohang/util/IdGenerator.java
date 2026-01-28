package com.poly.quanlykhohang.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IdGenerator {

    /**
     * Sinh mã kế tiếp: Prefix + YYYYMM + 0001
     * VD: Input ("PN", "PN2026010005") -> Output "PN2026010006"
     */
    public String generateNextId(String prefixBase, String lastId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefixWithDate = prefixBase + datePart;

        if (lastId == null || !lastId.startsWith(prefixWithDate)) {
            // Chưa có mã nào trong tháng này -> Bắt đầu 0001
            return prefixWithDate + "0001";
        }

        try {
            // Lấy 4 số cuối để tăng
            String sequencePart = lastId.substring(lastId.length() - 4);
            int sequence = Integer.parseInt(sequencePart);
            sequence++;
            return prefixWithDate + String.format("%04d", sequence);
        } catch (Exception e) {
            return prefixWithDate + "0001";
        }
    }
}