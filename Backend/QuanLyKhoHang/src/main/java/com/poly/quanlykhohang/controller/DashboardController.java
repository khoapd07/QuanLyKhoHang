package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dto.dashboard.DashboardResponse;
import com.poly.quanlykhohang.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestParam(required = false, defaultValue = "0") Integer maKho,
            @RequestParam(required = false) Integer nam
    ) {
        try {
            DashboardResponse data = dashboardService.getDashboardData(maKho, nam);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải dữ liệu dashboard: " + e.getMessage());
        }
    }
}