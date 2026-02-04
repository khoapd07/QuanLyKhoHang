package com.poly.quanlykhohang.dto.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponse {
    private DashboardCardDTO cards;
    private List<DashboardChartDTO> chart;
}