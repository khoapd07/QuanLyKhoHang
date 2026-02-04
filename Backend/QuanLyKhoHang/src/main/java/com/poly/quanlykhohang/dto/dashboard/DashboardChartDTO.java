package com.poly.quanlykhohang.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChartDTO {
    private Integer month;
    private Integer importQty;
    private Integer exportQty;
}