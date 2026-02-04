package com.poly.quanlykhohang.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardCardDTO {
    private Integer totalStock;
    private Integer importMonth;
    private Integer exportMonth;
    private Integer chartYear;
}