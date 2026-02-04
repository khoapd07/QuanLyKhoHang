package com.poly.quanlykhohang.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferChartDTO {
    private Integer month;
    private Integer transferInQty;  // Nhận về
    private Integer transferOutQty; // Chuyển đi
}