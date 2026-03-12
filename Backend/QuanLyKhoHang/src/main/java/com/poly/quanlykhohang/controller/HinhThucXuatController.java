package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.HinhThucXuatDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hinh-thuc-xuat")
public class HinhThucXuatController {

    @Autowired
    private HinhThucXuatDAO hinhThucXuatDAO;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(hinhThucXuatDAO.findAll());
    }
}