package com.poly.quanlykhohang.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IdGenerator {

    /**
     * Quy tắc sinh mã: Prefix + YYYYMM + 0001
     * VD Input: Prefix="IP15", LastID="IP152026010005"
     * => Output: "IP152026010006"
     */
    public String generateNextId(String prefixBase, String lastId) {
        // Lấy tháng năm hiện tại: 202601
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        // Prefix đầy đủ: IP15202601
        String prefixWithDate = prefixBase + datePart;

        // Nếu chưa có mã nào bắt đầu bằng cụm này -> Trả về số 0001
        if (lastId == null || !lastId.startsWith(prefixWithDate)) {
            return prefixWithDate + "0001";
        }

        try {
            // Lấy 4 số cuối cùng của mã cũ để tăng lên 1
            // VD: IP152026010005 -> Lấy "0005"
            String sequencePart = lastId.substring(lastId.length() - 4);
            int sequence = Integer.parseInt(sequencePart);
            sequence++;

            // Format lại thành 4 chữ số: 6 -> "0006"
            return prefixWithDate + String.format("%04d", sequence);
        } catch (Exception e) {
            // Phòng trường hợp lỗi parse, reset về 0001
            return prefixWithDate + "0001";
        }
    }
}